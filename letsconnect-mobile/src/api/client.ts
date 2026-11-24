import axios from 'axios';
import Config from 'react-native-config';
import * as Keychain from 'react-native-keychain';

const API_BASE_URL =
  Config.REACT_APP_API_BASE_URL ??
  'http://10.0.2.2:8080/api/v1';
const ACCESS_TOKEN_SERVICE = 'letsconnect.access-token';
const REFRESH_TOKEN_SERVICE = 'letsconnect.refresh-token';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

apiClient.interceptors.request.use(async config => {
  const credentials = await Keychain.getGenericPassword({
    service: ACCESS_TOKEN_SERVICE,
  });
  const token = credentials ? credentials.password : null;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export async function saveAccessToken(token: string): Promise<void> {
  await Keychain.setGenericPassword('letsconnect', token, {
    service: ACCESS_TOKEN_SERVICE,
  });
}

export async function getAccessToken(): Promise<string | null> {
  const credentials = await Keychain.getGenericPassword({
    service: ACCESS_TOKEN_SERVICE,
  });
  return credentials ? credentials.password : null;
}

export async function clearAccessToken(): Promise<void> {
  await Keychain.resetGenericPassword({
    service: ACCESS_TOKEN_SERVICE,
  });
  await Keychain.resetGenericPassword({
    service: REFRESH_TOKEN_SERVICE,
  });
}

export async function getRefreshToken(): Promise<string | null> {
  const credentials = await Keychain.getGenericPassword({
    service: REFRESH_TOKEN_SERVICE,
  });
  return credentials ? credentials.password : null;
}

export async function refreshSession(): Promise<AuthSession> {
  const refreshToken = await getRefreshToken();
  if (!refreshToken) {
    throw new Error('No refresh session');
  }
  const response = await apiClient.post<{
    accessToken: string;
    refreshToken: string;
    expiresAt: string;
  }>('/auth/sessions/refresh', {refreshToken});
  await saveAccessToken(response.data.accessToken);
  await Keychain.setGenericPassword('letsconnect', response.data.refreshToken, {
    service: REFRESH_TOKEN_SERVICE,
  });
  return {
    ...response.data,
    user: {id: '', nickname: '', avatarUrl: '', profileState: 'ACTIVE'},
    isNewUser: false,
    coinBalance: 0,
  };
}

export interface AuthUser {
  id: string;
  nickname: string;
  avatarUrl: string;
  profileState: 'INCOMPLETE' | 'ACTIVE';
}

export interface AuthSession {
  accessToken: string;
  refreshToken: string;
  expiresAt: string;
  user: AuthUser;
  isNewUser: boolean;
  coinBalance: number;
}

export async function sendOtp(phoneNumber: string): Promise<void> {
  await apiClient.post('/auth/send-otp', {phoneNumber});
}

export async function verifyOtp(
  phoneNumber: string,
  otp: string,
): Promise<AuthSession> {
  const response = await apiClient.post<AuthSession>('/auth/verify-otp', {
    phoneNumber,
    otp,
  });
  await saveAccessToken(response.data.accessToken);
  await Keychain.setGenericPassword('letsconnect', response.data.refreshToken, {
    service: REFRESH_TOKEN_SERVICE,
  });
  return response.data;
}

export interface MeProfile {
  userId: string;
  nickname: string;
  avatarUrl: string;
  gender?: string;
  ageGroup?: string;
  preferredLanguages: string[];
  role: 'USER' | 'LISTENER' | 'ADMIN';
}

export async function getMe(): Promise<MeProfile> {
  const response = await apiClient.get<MeProfile>('/me');
  return response.data;
}

export interface DiscoverHost {
  userId: string;
  nickname: string;
  avatarUrl: string;
  languages: string[];
  bio: string;
  pricePerMinuteCoins: number;
  isOnline: boolean;
  isOnCall: boolean;
  ratingAvg: number;
  categories: Array<{id: number; name: string; slug: string}>;
}

export async function discoverHosts(
  topic: string = 'all',
  language?: string,
): Promise<DiscoverHost[]> {
  const response = await apiClient.get<DiscoverHost[]>('/discover', {
    params: {topic, language},
  });
  return response.data;
}

export interface CallToken {
  callSessionId: string;
  channelName: string;
  agoraAppId: string;
  agoraToken: string;
  ratePerMinuteCoins: number;
  callerBalance?: number;
}

export async function initiateCall(
  callerId: string,
  listenerId: string,
  categoryId?: number,
): Promise<CallToken> {
  const response = await apiClient.post<CallToken>('/calls', {
    callerId,
    listenerId,
    categoryId,
  });
  return response.data;
}

export async function acceptCall(callId: string): Promise<CallToken> {
  const response = await apiClient.post<CallToken>(`/calls/${callId}/accept`);
  return response.data;
}

export async function rejectCall(callId: string): Promise<void> {
  await apiClient.post(`/calls/${callId}/reject`);
}

export async function endCall(callId: string): Promise<void> {
  await apiClient.post(`/calls/${callId}/end`);
}
