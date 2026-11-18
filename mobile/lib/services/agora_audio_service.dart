import 'package:agora_rtc_engine/agora_rtc_engine.dart';
import 'package:permission_handler/permission_handler.dart';

class AgoraAudioService {
  RtcEngine? _engine;
  bool _isMuted = false;
  bool _isSpeakerOn = true;

  bool get isMuted => _isMuted;
  bool get isSpeakerOn => _isSpeakerOn;

  Future<void> initializeAndJoin({
    required String appId,
    required String channelName,
    required String token,
    required Function(int uid) onUserJoined,
    required Function(int uid) onUserOffline,
  }) async {
    // 1. Request Microphone permission
    await Permission.microphone.request();

    // 2. Create and initialize Agora RTC Engine
    _engine = createAgoraRtcEngine();
    await _engine!.initialize(RtcEngineContext(
      appId: appId,
      channelProfile: ChannelProfileType.channelProfileCommunication,
    ));

    // 3. Register Call Event Handlers
    _engine!.registerEventHandler(
      RtcEngineEventHandler(
        onJoinChannelSuccess: (RtcConnection connection, int elapsed) {},
        onUserJoined: (RtcConnection connection, int remoteUid, int elapsed) {
          onUserJoined(remoteUid);
        },
        onUserOffline: (RtcConnection connection, int remoteUid, UserOfflineReasonType reason) {
          onUserOffline(remoteUid);
        },
      ),
    );

    // 4. Configure Audio settings optimized for high-clarity voice calls
    await _engine!.enableAudio();
    await _engine!.setAudioProfile(
      profile: AudioProfileType.audioProfileSpeechStandard,
      scenario: AudioScenarioType.audioScenarioGameStreaming,
    );
    await _engine!.setEnableSpeakerphone(_isSpeakerOn);

    // 5. Join the Agora RTC Channel
    await _engine!.joinChannel(
      token: token,
      channelId: channelName,
      uid: 0,
      options: const ChannelMediaOptions(
        clientRoleType: ClientRoleType.clientRoleBroadcaster,
        autoSubscribeAudio: true,
        publishMicrophoneTrack: true,
      ),
    );
  }

  Future<void> toggleMute() async {
    if (_engine == null) return;
    _isMuted = !_isMuted;
    await _engine!.muteLocalAudioStream(_isMuted);
  }

  Future<void> toggleSpeaker() async {
    if (_engine == null) return;
    _isSpeakerOn = !_isSpeakerOn;
    await _engine!.setEnableSpeakerphone(_isSpeakerOn);
  }

  Future<void> leaveAndDispose() async {
    if (_engine != null) {
      await _engine!.leaveChannel();
      await _engine!.release();
      _engine = null;
    }
  }
}
