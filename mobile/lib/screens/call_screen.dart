import 'dart:async';
import 'package:flutter/material.dart';
import '../models/call_session_model.dart';
import '../models/listener_model.dart';
import '../services/agora_audio_service.dart';
import '../services/api_service.dart';
import '../theme/app_colors.dart';

class CallScreen extends StatefulWidget {
  final ListenerModel listener;
  final CallTokenModel callToken;
  final int initialBalance;

  const CallScreen({
    super.key,
    required this.listener,
    required this.callToken,
    required this.initialBalance,
  });

  @override
  State<CallScreen> createState() => _CallScreenState();
}

class _CallScreenState extends State<CallScreen> {
  final AgoraAudioService _audioService = AgoraAudioService();
  bool _isConnected = false;
  bool _isMuted = false;
  bool _isSpeakerOn = true;
  int _callDurationSeconds = 0;
  late int _remainingCoins;
  Timer? _callTimer;
  String? _activeGiftAnimation;

  final List<Map<String, dynamic>> _virtualGifts = [
    {'name': 'Red Rose', 'emoji': '🌹', 'coins': 5},
    {'name': 'Warm Chai', 'emoji': '☕', 'coins': 10},
    {'name': 'Sweet Chocolate', 'emoji': '🍫', 'coins': 25},
    {'name': 'Big Heart', 'emoji': '💖', 'coins': 50},
    {'name': 'Golden Crown', 'emoji': '👑', 'coins': 100},
  ];

  @override
  void initState() {
    super.initState();
    _remainingCoins = widget.initialBalance;
    _initializeCall();
  }

  Future<void> _initializeCall() async {
    await _audioService.initializeAndJoin(
      appId: widget.callToken.agoraAppId,
      channelName: widget.callToken.channelName,
      token: widget.callToken.agoraToken,
      onUserJoined: (uid) {
        setState(() => _isConnected = true);
        _startTimer();
      },
      onUserOffline: (uid) {
        _endCall();
      },
    );

    // Fallback simulation for local dev testing
    Future.delayed(const Duration(seconds: 2), () {
      if (mounted && !_isConnected) {
        setState(() => _isConnected = true);
        _startTimer();
      }
    });
  }

  void _startTimer() {
    _callTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (!mounted) return;
      setState(() {
        _callDurationSeconds++;

        // Deduct coins every 60 seconds
        if (_callDurationSeconds % 60 == 0) {
          _remainingCoins -= widget.listener.pricePerMinuteCoins;
          if (_remainingCoins <= 0) {
            _remainingCoins = 0;
            _endCall(reason: "Balance exhausted");
          }
        }
      });
    });
  }

  void _sendGift(Map<String, dynamic> gift) {
    final int cost = gift['coins'] as int;
    if (_remainingCoins < cost) {
      Navigator.pop(context);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text("Insufficient coins to send this gift"), backgroundColor: AppColors.busy),
      );
      return;
    }

    setState(() {
      _remainingCoins -= cost;
      _activeGiftAnimation = "${gift['emoji']} Sent ${gift['name']}!";
    });

    Navigator.pop(context); // Close gift sheet

    // Auto-dismiss floating celebration after 3 seconds
    Future.delayed(const Duration(seconds: 3), () {
      if (mounted) setState(() => _activeGiftAnimation = null);
    });
  }

  void _openGiftSheet() {
    showModalBottomSheet(
      context: context,
      backgroundColor: AppColors.surface,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (ctx) => Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  "Send Virtual Gift 🎁",
                  style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
                ),
                Text(
                  "Balance: $_remainingCoins Coins",
                  style: const TextStyle(color: AppColors.coins, fontSize: 13, fontWeight: FontWeight.w600),
                ),
              ],
            ),
            const SizedBox(height: 6),
            const Text(
              "Express appreciation and support to your listener in real-time.",
              style: TextStyle(color: AppColors.textSecondary, fontSize: 12),
            ),
            const SizedBox(height: 20),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: _virtualGifts.map((gift) {
                return GestureDetector(
                  onTap: () => _sendGift(gift),
                  child: Column(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: AppColors.surfaceLight,
                          shape: BoxShape.circle,
                          border: Border.all(color: AppColors.primary.withValues(alpha: 0.4)),
                        ),
                        child: Text(gift['emoji'] as String, style: const TextStyle(fontSize: 28)),
                      ),
                      const SizedBox(height: 6),
                      Text(gift['name'] as String, style: const TextStyle(color: Colors.white, fontSize: 11)),
                      Text("${gift['coins']} Coins", style: const TextStyle(color: AppColors.coins, fontSize: 10, fontWeight: FontWeight.bold)),
                    ],
                  ),
                );
              }).toList(),
            ),
            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }

  Future<void> _endCall({String? reason}) async {
    _callTimer?.cancel();
    await _audioService.leaveAndDispose();
    await ApiService.endCall(widget.callToken.callSessionId);

    if (mounted) {
      if (reason != null) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(reason), backgroundColor: AppColors.busy),
        );
      }
      Navigator.pop(context);
    }
  }

  void _showReportDialog() {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppColors.surface,
        title: const Text("Report or Block User", style: TextStyle(color: Colors.white)),
        content: const Text(
          "Do you want to report this listener for inappropriate behavior and block further contact?",
          style: TextStyle(color: AppColors.textSecondary),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text("Cancel", style: TextStyle(color: AppColors.textMuted)),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: AppColors.busy),
            onPressed: () {
              Navigator.pop(ctx);
              _endCall(reason: "User reported and blocked");
            },
            child: const Text("Report & Hang Up", style: TextStyle(color: Colors.white)),
          ),
        ],
      ),
    );
  }

  String _formatDuration(int totalSeconds) {
    final minutes = (totalSeconds ~/ 60).toString().padLeft(2, '0');
    final seconds = (totalSeconds % 60).toString().padLeft(2, '0');
    return '$minutes:$seconds';
  }

  @override
  void dispose() {
    _callTimer?.cancel();
    _audioService.leaveAndDispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final estimatedMinutesLeft = _remainingCoins ~/ widget.listener.pricePerMinuteCoins;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios, color: Colors.white, size: 20),
          onPressed: () => _endCall(),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.card_giftcard, color: AppColors.coins),
            tooltip: "Send Gift",
            onPressed: _openGiftSheet,
          ),
          IconButton(
            icon: const Icon(Icons.shield_outlined, color: AppColors.busy),
            tooltip: "Report & Block",
            onPressed: _showReportDialog,
          ),
        ],
      ),
      body: SafeArea(
        child: Column(
          children: [
            const Spacer(flex: 1),

            // In-Call Gift Animation Banner (if sent)
            if (_activeGiftAnimation != null)
              Container(
                margin: const EdgeInsets.only(bottom: 20),
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
                decoration: BoxDecoration(
                  color: AppColors.primary,
                  borderRadius: BorderRadius.circular(24),
                  boxShadow: [
                    BoxShadow(color: AppColors.primary.withValues(alpha: 0.5), blurRadius: 16),
                  ],
                ),
                child: Text(
                  _activeGiftAnimation!,
                  style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold),
                ),
              ),

            // Listener Avatar with Animated Pulsing Soundwave Ring
            Center(
              child: Stack(
                alignment: Alignment.center,
                children: [
                  Container(
                    width: 170,
                    height: 170,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: AppColors.primary.withValues(alpha: _isConnected ? 0.2 : 0.05),
                    ),
                  ),
                  Container(
                    width: 140,
                    height: 140,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: AppColors.primary.withValues(alpha: _isConnected ? 0.35 : 0.1),
                    ),
                  ),
                  CircleAvatar(
                    radius: 54,
                    backgroundColor: AppColors.surfaceLight,
                    backgroundImage: NetworkImage(widget.listener.avatarUrl),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            // Listener Name & Status
            Text(
              widget.listener.nickname,
              style: const TextStyle(color: Colors.white, fontSize: 22, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            Text(
              _isConnected ? _formatDuration(_callDurationSeconds) : "Connecting...",
              style: TextStyle(
                color: _isConnected ? AppColors.online : AppColors.textSecondary,
                fontSize: 16,
                fontWeight: FontWeight.w600,
              ),
            ),

            const SizedBox(height: 20),

            // Live Coin Deduction HUD
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              decoration: BoxDecoration(
                color: AppColors.surface,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: AppColors.coins.withValues(alpha: 0.4)),
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Icon(Icons.monetization_on, color: AppColors.coins, size: 18),
                  const SizedBox(width: 8),
                  Text(
                    "$_remainingCoins Coins (~$estimatedMinutesLeft mins remaining)",
                    style: const TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.w600),
                  ),
                ],
              ),
            ),

            const Spacer(flex: 2),

            // In-Call Controls (Mute, End Call, Speaker)
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 40, vertical: 30),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                children: [
                  // Mute
                  _buildCallActionButton(
                    icon: _isMuted ? Icons.mic_off : Icons.mic,
                    isActive: _isMuted,
                    label: _isMuted ? "Unmute" : "Mute",
                    onTap: () async {
                      await _audioService.toggleMute();
                      setState(() => _isMuted = _audioService.isMuted);
                    },
                  ),

                  // End Call
                  GestureDetector(
                    onTap: () => _endCall(),
                    child: Container(
                      width: 70,
                      height: 70,
                      decoration: const BoxDecoration(
                        color: AppColors.busy,
                        shape: BoxShape.circle,
                        boxShadow: [
                          BoxShadow(
                            color: Color(0x66EF4444),
                            blurRadius: 20,
                            spreadRadius: 2,
                          ),
                        ],
                      ),
                      child: const Icon(Icons.call_end, color: Colors.white, size: 34),
                    ),
                  ),

                  // Speaker
                  _buildCallActionButton(
                    icon: _isSpeakerOn ? Icons.volume_up : Icons.volume_off,
                    isActive: _isSpeakerOn,
                    label: "Speaker",
                    onTap: () async {
                      await _audioService.toggleSpeaker();
                      setState(() => _isSpeakerOn = _audioService.isSpeakerOn);
                    },
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCallActionButton({
    required IconData icon,
    required bool isActive,
    required String label,
    required VoidCallback onTap,
  }) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        GestureDetector(
          onTap: onTap,
          child: Container(
            width: 54,
            height: 54,
            decoration: BoxDecoration(
              color: isActive ? AppColors.primary : AppColors.surfaceLight,
              shape: BoxShape.circle,
            ),
            child: Icon(icon, color: Colors.white, size: 24),
          ),
        ),
        const SizedBox(height: 8),
        Text(label, style: const TextStyle(color: AppColors.textSecondary, fontSize: 12)),
      ],
    );
  }
}
