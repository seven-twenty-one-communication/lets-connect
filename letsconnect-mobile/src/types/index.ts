export type UserRole = 'USER' | 'LISTENER' | 'ADMIN';

export type UserStatus = 'ACTIVE' | 'SUSPENDED' | 'BANNED';

export type CallStatus =
  | 'INITIATED'
  | 'RINGING'
  | 'CONNECTED'
  | 'COMPLETED'
  | 'REJECTED'
  | 'MISSED'
  | 'BUSY'
  | 'FAILED';

export interface UserProfile {
  id: string;
  phoneNumber: string;
  nickname: string;
  avatarUrl: string;
  gender?: string;
  ageGroup?: string;
  preferredLanguages: string[];
  role: UserRole;
  status: UserStatus;
  coinBalance: number;
}

export interface ListenerProfile {
  listenerId: string;
  userId: string;
  nickname: string;
  avatarUrl: string;
  languages: string[];
  bio?: string;
  voiceIntroUrl?: string;
  pricePerMinuteCoins: number;
  isOnline: boolean;
  isOnCall: boolean;
  ratingAvg: number;
  totalReviews: number;
  totalCallsCompleted: number;
  categories: Category[];
}

export interface Category {
  id: number;
  name: string;
  slug: string;
  description?: string;
}

export interface CallSessionToken {
  callSessionId: string;
  channelName: string;
  agoraAppId: string;
  agoraToken: string;
  ratePerMinuteCoins: number;
  callerBalance?: number;
}
