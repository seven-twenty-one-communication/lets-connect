import 'package:flutter/material.dart';
import '../models/listener_model.dart';
import '../models/call_session_model.dart';
import '../services/api_service.dart';
import '../services/session_store.dart';
import '../theme/app_colors.dart';
import 'call_screen.dart';
import 'chat_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key, required this.session});

  final AppSession session;

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  List<ListenerModel> _listeners = [];
  int? _selectedCategoryId;
  int _coinBalance = 0;
  bool _isLoading = true;
  String? _currentlyPlayingVoiceIntro;

  @override
  void initState() {
    super.initState();
    _loadInitialData();
  }

  Future<void> _loadInitialData() async {
    setState(() => _isLoading = true);
    final listeners = await ApiService.getListeners();

    setState(() {
      _listeners = listeners;
      _isLoading = false;
    });
  }

  void _toggleVoicePreview(ListenerModel listener) {
    setState(() {
      if (_currentlyPlayingVoiceIntro == listener.id) {
        _currentlyPlayingVoiceIntro = null;
      } else {
        _currentlyPlayingVoiceIntro = listener.id;
      }
    });

    if (_currentlyPlayingVoiceIntro != null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text("Playing voice intro of ${listener.nickname}..."),
          duration: const Duration(seconds: 4),
          backgroundColor: AppColors.surfaceLight,
        ),
      );
      // Auto-stop preview simulation after 4 seconds
      Future.delayed(const Duration(seconds: 4), () {
        if (mounted && _currentlyPlayingVoiceIntro == listener.id) {
          setState(() => _currentlyPlayingVoiceIntro = null);
        }
      });
    }
  }

  void _openChat(ListenerModel listener) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => ChatScreen(
          listener: listener,
          currentUserId: widget.session.userId,
        ),
      ),
    );
  }

  void _startCall(ListenerModel listener) async {
    if (_coinBalance < listener.pricePerMinuteCoins) {
      _showRechargeDialog(listener.pricePerMinuteCoins);
      return;
    }

    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (_) => const Center(
        child: CircularProgressIndicator(color: AppColors.primary),
      ),
    );

    final callToken = await ApiService.initiateCall(
      callerId: widget.session.userId,
      listenerId: listener.userId,
      categoryId: _selectedCategoryId,
    );

    Navigator.pop(context); // Dismiss loader

    final sessionData = callToken ??
        CallTokenModel(
          callSessionId: 'demo-session',
          channelName: 'demo_channel_${DateTime.now().millisecondsSinceEpoch}',
          agoraAppId: 'sample_agora_app_id',
          agoraToken: 'sample_agora_token',
          ratePerMinuteCoins: listener.pricePerMinuteCoins,
          callerBalance: _coinBalance,
        );

    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => CallScreen(
          listener: listener,
          callToken: sessionData,
          initialBalance: _coinBalance,
        ),
      ),
    );
  }

  void _showRechargeDialog(int requiredCoins) {
    showModalBottomSheet(
      context: context,
      backgroundColor: AppColors.surface,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (ctx) => Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              "Insufficient Balance",
              style: TextStyle(color: Colors.white, fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            Text(
              "You need at least $requiredCoins coins to start this call. Choose a coin package to continue:",
              style: const TextStyle(color: AppColors.textSecondary, fontSize: 14),
            ),
            const SizedBox(height: 20),
            _buildCoinPackTile("Starter Pack", 50, "₹49", () {
              setState(() => _coinBalance += 50);
              Navigator.pop(ctx);
            }),
            _buildCoinPackTile("Popular Pack", 120, "₹99", () {
              setState(() => _coinBalance += 120);
              Navigator.pop(ctx);
            }),
            _buildCoinPackTile("Value Pack", 350, "₹249", () {
              setState(() => _coinBalance += 350);
              Navigator.pop(ctx);
            }),
          ],
        ),
      ),
    );
  }

  Widget _buildCoinPackTile(String title, int coins, String price, VoidCallback onTap) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      decoration: BoxDecoration(
        color: AppColors.surfaceLight,
        borderRadius: BorderRadius.circular(12),
      ),
      child: ListTile(
        title: Text(title, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w600)),
        subtitle: Text("$coins Coins", style: const TextStyle(color: AppColors.coins)),
        trailing: ElevatedButton(
          style: ElevatedButton.styleFrom(backgroundColor: AppColors.primary),
          onPressed: onTap,
          child: Text(price, style: const TextStyle(color: Colors.white)),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        backgroundColor: AppColors.background,
        elevation: 0,
        title: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(2),
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                border: Border.all(color: AppColors.primary, width: 2),
              ),
              child: const CircleAvatar(
                radius: 16,
                backgroundColor: AppColors.surfaceLight,
                child: Icon(Icons.person, color: Colors.white, size: 20),
              ),
            ),
            const SizedBox(width: 10),
            const Text(
              "Anonymous Friend",
              style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold),
            ),
          ],
        ),
        actions: [
          Container(
            margin: const EdgeInsets.only(right: 16, top: 10, bottom: 10),
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
            decoration: BoxDecoration(
              color: AppColors.surface,
              borderRadius: BorderRadius.circular(20),
              border: Border.all(color: AppColors.coins.withValues(alpha: 0.5)),
            ),
            child: Row(
              children: [
                const Icon(Icons.monetization_on, color: AppColors.coins, size: 18),
                const SizedBox(width: 6),
                Text(
                  "$_coinBalance",
                  style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                ),
                const SizedBox(width: 4),
                GestureDetector(
                  onTap: () => _showRechargeDialog(10),
                  child: const Icon(Icons.add_circle, color: AppColors.coins, size: 18),
                ),
              ],
            ),
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator(color: AppColors.primary))
          : RefreshIndicator(
              onRefresh: _loadInitialData,
              child: ListView(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                children: [
                  const SizedBox(height: 8),
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: AppColors.surfaceLight.withValues(alpha: 0.4),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: AppColors.surfaceLight),
                    ),
                    child: const Row(
                      children: [
                        Icon(Icons.shield_outlined, color: AppColors.primaryLight, size: 20),
                        SizedBox(width: 10),
                        Expanded(
                          child: Text(
                            "Safe space for peer venting. For psychiatric emergencies, dial 14416 (Tele-MANAS).",
                            style: TextStyle(color: AppColors.textSecondary, fontSize: 11),
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 16),

                  const Text(
                    "What is on your mind?",
                    style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 12),
                  SizedBox(
                    height: 38,
                    child: ListView(
                      scrollDirection: Axis.horizontal,
                      children: [
                        _buildCategoryChip("All Topics", null),
                        _buildCategoryChip("Heartbreak & Breakup", 1),
                        _buildCategoryChip("Late Night Venting", 2),
                        _buildCategoryChip("Career & Exams", 3),
                        _buildCategoryChip("Loneliness", 4),
                      ],
                    ),
                  ),

                  const SizedBox(height: 20),
                  const Text(
                    "Available Listeners",
                    style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 12),

                  ..._listeners.map((listener) => _buildListenerCard(listener)),
                  const SizedBox(height: 30),
                ],
              ),
            ),
    );
  }

  Widget _buildCategoryChip(String label, int? id) {
    final isSelected = _selectedCategoryId == id;
    return GestureDetector(
      onTap: () {
        setState(() => _selectedCategoryId = id);
      },
      child: Container(
        margin: const EdgeInsets.only(right: 8),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        decoration: BoxDecoration(
          color: isSelected ? AppColors.primary : AppColors.surface,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: isSelected ? AppColors.primaryLight : AppColors.surfaceLight,
          ),
        ),
        child: Text(
          label,
          style: TextStyle(
            color: isSelected ? Colors.white : AppColors.textSecondary,
            fontSize: 13,
            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
          ),
        ),
      ),
    );
  }

  Widget _buildListenerCard(ListenerModel listener) {
    final isPlayingVoice = _currentlyPlayingVoiceIntro == listener.id;

    return Container(
      margin: const EdgeInsets.only(bottom: 14),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: listener.isOnline ? AppColors.online.withValues(alpha: 0.3) : Colors.transparent,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Avatar with status indicator
              Stack(
                children: [
                  CircleAvatar(
                    radius: 28,
                    backgroundColor: AppColors.surfaceLight,
                    backgroundImage: NetworkImage(listener.avatarUrl),
                  ),
                  Positioned(
                    right: 0,
                    bottom: 0,
                    child: Container(
                      width: 14,
                      height: 14,
                      decoration: BoxDecoration(
                        color: listener.isOnline ? AppColors.online : AppColors.textMuted,
                        shape: BoxShape.circle,
                        border: Border.all(color: AppColors.surface, width: 2),
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(width: 14),

              // Info
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          listener.nickname,
                          style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold),
                        ),
                        Row(
                          children: [
                            const Icon(Icons.star, color: AppColors.coins, size: 16),
                            const SizedBox(width: 2),
                            Text(
                              "${listener.ratingAvg}",
                              style: const TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.bold),
                            ),
                          ],
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      listener.languages.join(" • "),
                      style: const TextStyle(color: AppColors.primaryLight, fontSize: 12),
                    ),
                    if (listener.bio != null) ...[
                      const SizedBox(height: 6),
                      Text(
                        listener.bio!,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(color: AppColors.textSecondary, fontSize: 12),
                      ),
                    ],
                  ],
                ),
              ),
            ],
          ),

          const SizedBox(height: 12),

          // Voice Bio Snippet Player
          GestureDetector(
            onTap: () => _toggleVoicePreview(listener),
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              decoration: BoxDecoration(
                color: isPlayingVoice ? AppColors.primary.withValues(alpha: 0.2) : AppColors.surfaceLight,
                borderRadius: BorderRadius.circular(10),
                border: Border.all(color: isPlayingVoice ? AppColors.primary : Colors.transparent),
              ),
              child: Row(
                children: [
                  Icon(
                    isPlayingVoice ? Icons.stop_circle : Icons.play_circle_fill,
                    color: isPlayingVoice ? AppColors.primary : AppColors.primaryLight,
                    size: 20,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    isPlayingVoice ? "Playing Voice Intro..." : "Hear Voice Intro (5s)",
                    style: TextStyle(
                      color: isPlayingVoice ? Colors.white : AppColors.textSecondary,
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 14),

          // Actions: Rate, Anonymous Text Chat, Call Now
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  const Icon(Icons.monetization_on, color: AppColors.coins, size: 16),
                  const SizedBox(width: 4),
                  Text(
                    "${listener.pricePerMinuteCoins} coins/min",
                    style: const TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.w600),
                  ),
                ],
              ),
              Row(
                children: [
                  // Anonymous Chat Button
                  OutlinedButton.icon(
                    style: OutlinedButton.styleFrom(
                      foregroundColor: AppColors.primaryLight,
                      side: const BorderSide(color: AppColors.primaryLight),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    ),
                    icon: const Icon(Icons.chat_bubble_outline, size: 15),
                    label: const Text("Chat", style: TextStyle(fontSize: 12)),
                    onPressed: () => _openChat(listener),
                  ),
                  const SizedBox(width: 8),

                  // Call Now Button
                  ElevatedButton.icon(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: listener.isOnline ? AppColors.primary : AppColors.surfaceLight,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
                      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                    ),
                    icon: const Icon(Icons.call, size: 15, color: Colors.white),
                    label: Text(
                      listener.isOnline ? "Call Now" : "Offline",
                      style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.bold),
                    ),
                    onPressed: listener.isOnline ? () => _startCall(listener) : null,
                  ),
                ],
              ),
            ],
          ),
        ],
      ),
    );
  }
}
