import React, { useEffect, useState } from 'react';
import {
  SafeAreaView,
  StatusBar,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { Colors } from './src/theme/colors';
import {
  clearAccessToken,
  getAccessToken,
  getMe,
  discoverHosts,
  initiateCall,
  type DiscoverHost,
  sendOtp,
  verifyOtp,
  type AuthSession,
} from './src/api/client';

export default function App(): React.JSX.Element {
  const [phoneNumber, setPhoneNumber] = useState('');
  const [otp, setOtp] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [session, setSession] = useState<AuthSession | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [hosts, setHosts] = useState<DiscoverHost[]>([]);
  const [callStatus, setCallStatus] = useState<string | null>(null);

  useEffect(() => {
    getAccessToken()
      .then(async token => {
        if (!token) {
          return;
        }
        try {
          const profile = await getMe();
          setSession({
            accessToken: token,
            refreshToken: '',
            expiresAt: '',
            user: {
              id: profile.userId,
              nickname: profile.nickname,
              avatarUrl: profile.avatarUrl,
              profileState: 'ACTIVE',
            },
            isNewUser: false,
            coinBalance: 0,
          });
          discoverHosts().then(setHosts).catch(() => setHosts([]));
        } catch {
          await clearAccessToken();
        }
      })
      .catch(() => setError('Unable to restore the saved session'));
  }, []);

  const handleSendOtp = async () => {
    setError(null);
    try {
      await sendOtp(phoneNumber.trim());
      setOtpSent(true);
    } catch {
      setError('Unable to send OTP');
    }
  };

  const handleVerifyOtp = async () => {
    setError(null);
    try {
      const nextSession = await verifyOtp(phoneNumber.trim(), otp.trim());
      setSession(nextSession);
    } catch {
      setError('Invalid or expired OTP');
    }
  };

  const handleLogout = async () => {
    await clearAccessToken();
    setSession(null);
    setOtp('');
    setOtpSent(false);
  };

  const handleCall = async (host: DiscoverHost) => {
    if (!session) {
      return;
    }
    setCallStatus('Starting call...');
    try {
      await initiateCall(
        session.user.id,
        host.userId,
        host.categories[0]?.id,
      );
      setCallStatus('Call invitation sent');
    } catch {
      setCallStatus('Unable to start call');
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor={Colors.background} />
      <View style={styles.content}>
        <Text style={styles.title}>LetsConnect</Text>
        <Text style={styles.subtitle}>Online Friendship & 1-on-1 Voice Calling</Text>
        {session ? (
          <View style={styles.card}>
            <Text style={styles.cardTitle}>{session.user.nickname}</Text>
            <Text style={styles.cardText}>Session restored</Text>
            {hosts.map(host => (
              <View key={host.userId} style={styles.hostRow}>
                <View style={styles.hostInfo}>
                  <Text style={styles.hostName}>{host.nickname}</Text>
                  <Text style={styles.hostMeta}>
                    {host.pricePerMinuteCoins} coins/min · {host.ratingAvg}
                  </Text>
                </View>
                <TouchableOpacity
                  style={styles.callButton}
                  onPress={() => handleCall(host)}
                  disabled={host.isOnCall}>
                  <Text style={styles.buttonText}>
                    {host.isOnCall ? 'Busy' : 'Call'}
                  </Text>
                </TouchableOpacity>
              </View>
            ))}
            {hosts.length === 0 && (
              <Text style={styles.cardText}>No verified hosts are currently available</Text>
            )}
            {callStatus && <Text style={styles.cardText}>{callStatus}</Text>}
            <TouchableOpacity style={styles.button} onPress={handleLogout}>
              <Text style={styles.buttonText}>Sign out</Text>
            </TouchableOpacity>
          </View>
        ) : (
          <View style={styles.card}>
            <TextInput
              style={styles.input}
              value={phoneNumber}
              onChangeText={setPhoneNumber}
              placeholder="+91XXXXXXXXXX"
              placeholderTextColor={Colors.textSecondary}
              keyboardType="phone-pad"
              editable={!otpSent}
            />
            {otpSent && (
              <TextInput
                style={styles.input}
                value={otp}
                onChangeText={setOtp}
                placeholder="6-digit OTP"
                placeholderTextColor={Colors.textSecondary}
                keyboardType="number-pad"
                maxLength={6}
              />
            )}
            <TouchableOpacity
              style={styles.button}
              onPress={otpSent ? handleVerifyOtp : handleSendOtp}>
              <Text style={styles.buttonText}>{otpSent ? 'Verify OTP' : 'Send OTP'}</Text>
            </TouchableOpacity>
          </View>
        )}
        {error && <Text style={styles.error}>{error}</Text>}
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  title: {
    fontSize: 32,
    fontWeight: '800',
    color: Colors.textPrimary,
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 15,
    color: Colors.textSecondary,
    textAlign: 'center',
    marginBottom: 24,
  },
  card: {
    width: '100%',
    maxWidth: 360,
    padding: 20,
    borderRadius: 16,
    backgroundColor: Colors.surfaceLight,
  },
  cardTitle: {
    color: Colors.textPrimary,
    fontSize: 18,
    fontWeight: '700',
    marginBottom: 8,
  },
  cardText: {
    color: Colors.textSecondary,
    marginBottom: 16,
  },
  input: {
    width: '100%',
    height: 48,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: Colors.primary,
    color: Colors.textPrimary,
    paddingHorizontal: 14,
    marginBottom: 12,
  },
  button: {
    height: 48,
    borderRadius: 10,
    backgroundColor: Colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  buttonText: {
    color: Colors.textPrimary,
    fontWeight: '700',
  },
  error: {
    color: '#ff8080',
    marginTop: 12,
  },
  hostRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 10,
    borderTopWidth: 1,
    borderTopColor: Colors.primary,
  },
  hostInfo: {
    flex: 1,
  },
  hostName: {
    color: Colors.textPrimary,
    fontWeight: '700',
  },
  hostMeta: {
    color: Colors.textSecondary,
    marginTop: 4,
  },
  callButton: {
    height: 36,
    minWidth: 64,
    borderRadius: 8,
    backgroundColor: Colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
