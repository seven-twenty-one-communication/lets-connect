import 'package:material_ui/material_ui.dart';
import '../models/call_session_model.dart';
import '../models/listener_model.dart';
import '../services/api_service.dart';
import '../theme/app_colors.dart';
import 'call_screen.dart';

class IncomingCallScreen extends StatelessWidget {
  final String callSessionId;
  final String channelName;
  final String callerNickname;
  final String callerAvatarUrl;
  final String categoryName;
  final int ratePerMinuteCoins;

  const IncomingCallScreen({
    super.key,
    required this.callSessionId,
    required this.channelName,
    required this.callerNickname,
    required this.callerAvatarUrl,
    required this.categoryName,
    required this.ratePerMinuteCoins,
  });

  void _acceptCall(BuildContext context) async {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (_) => const Center(
        child: CircularProgressIndicator(color: AppColors.primary),
      ),
    );

    // Call backend accept API
    final callToken = await ApiService.acceptCall(callSessionId);
    Navigator.pop(context); // Dismiss loader

    if (callToken != null) {
      final listenerDummy = ListenerModel(
        id: 'self',
        userId: 'self',
        nickname: callerNickname,
        avatarUrl: callerAvatarUrl,
        pricePerMinuteCoins: ratePerMinuteCoins,
        isOnline: true,
        isOnCall: true,
        ratingAvg: 5.0,
        totalReviews: 0,
        totalCallsCompleted: 0,
        languages: ['Hindi', 'English'],
      );

      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) => CallScreen(
            listener: listenerDummy,
            callToken: callToken,
            initialBalance: 99999, // Listener is receiving coins, balance is not deducted
          ),
        ),
      );
    }
  }

  void _declineCall(BuildContext context) async {
    await ApiService.rejectCall(callSessionId);
    Navigator.pop(context);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      body: SafeArea(
        child: Column(
          children: [
            const Spacer(flex: 1),

            // Category Badge
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
              decoration: BoxDecoration(
                color: AppColors.surface,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: AppColors.primaryLight.withValues(alpha: 0.4)),
              ),
              child: Text(
                categoryName,
                style: const TextStyle(
                  color: AppColors.primaryLight,
                  fontSize: 13,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
            const SizedBox(height: 30),

            // Animated Caller Avatar Ring
            Center(
              child: Stack(
                alignment: Alignment.center,
                children: [
                  Container(
                    width: 170,
                    height: 170,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: AppColors.primary.withValues(alpha: 0.15),
                    ),
                  ),
                  Container(
                    width: 140,
                    height: 140,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: AppColors.primary.withValues(alpha: 0.25),
                    ),
                  ),
                  CircleAvatar(
                    radius: 54,
                    backgroundColor: AppColors.surfaceLight,
                    backgroundImage: NetworkImage(callerAvatarUrl),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            // Caller Name & Earning Tag
            Text(
              callerNickname,
              style: const TextStyle(
                color: Colors.white,
                fontSize: 24,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              "Earn +$ratePerMinuteCoins Coins / minute",
              style: const TextStyle(
                color: AppColors.coins,
                fontSize: 15,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 6),
            const Text(
              "Incoming Audio Call...",
              style: TextStyle(color: AppColors.textSecondary, fontSize: 13),
            ),

            const Spacer(flex: 2),

            // Accept & Decline Buttons
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 50, vertical: 40),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  // Decline
                  Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      GestureDetector(
                        onTap: () => _declineCall(context),
                        child: Container(
                          width: 68,
                          height: 68,
                          decoration: const BoxDecoration(
                            color: AppColors.busy,
                            shape: BoxShape.circle,
                            boxShadow: [
                              BoxShadow(
                                color: Color(0x66EF4444),
                                blurRadius: 16,
                                spreadRadius: 2,
                              ),
                            ],
                          ),
                          child: const Icon(Icons.call_end, color: Colors.white, size: 30),
                        ),
                      ),
                      const SizedBox(height: 10),
                      const Text("Decline", style: TextStyle(color: AppColors.textSecondary, fontSize: 13)),
                    ],
                  ),

                  // Accept
                  Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      GestureDetector(
                        onTap: () => _acceptCall(context),
                        child: Container(
                          width: 68,
                          height: 68,
                          decoration: const BoxDecoration(
                            color: AppColors.online,
                            shape: BoxShape.circle,
                            boxShadow: [
                              BoxShadow(
                                color: Color(0x6610B981),
                                blurRadius: 16,
                                spreadRadius: 2,
                              ),
                            ],
                          ),
                          child: const Icon(Icons.call, color: Colors.white, size: 30),
                        ),
                      ),
                      const SizedBox(height: 10),
                      const Text("Accept", style: TextStyle(color: AppColors.online, fontSize: 13, fontWeight: FontWeight.bold)),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
