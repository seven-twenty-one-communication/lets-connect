class ListenerModel {
  final String id;
  final String userId;
  final String nickname;
  final String avatarUrl;
  final String? bio;
  final String? voiceIntroUrl;
  final int pricePerMinuteCoins;
  final bool isOnline;
  final bool isOnCall;
  final double ratingAvg;
  final int totalReviews;
  final int totalCallsCompleted;
  final List<String> languages;

  ListenerModel({
    required this.id,
    required this.userId,
    required this.nickname,
    required this.avatarUrl,
    this.bio,
    this.voiceIntroUrl,
    required this.pricePerMinuteCoins,
    required this.isOnline,
    required this.isOnCall,
    required this.ratingAvg,
    required this.totalReviews,
    required this.totalCallsCompleted,
    required this.languages,
  });

  factory ListenerModel.fromJson(Map<String, dynamic> json) {
    final user = json['user'] as Map<String, dynamic>? ?? {};
    final languagesList = (user['preferredLanguages'] as List<dynamic>?)
            ?.map((e) => e.toString())
            .toList() ??
        ['Hindi', 'English'];

    return ListenerModel(
      id: json['id'] as String,
      userId: user['id'] as String? ?? json['userId'] as String? ?? '',
      nickname: user['nickname'] as String? ?? 'Empathetic Companion',
      avatarUrl: user['avatarUrl'] as String? ?? 'https://api.dicebear.com/7.x/bottts/png?seed=listener',
      bio: json['bio'] as String?,
      voiceIntroUrl: json['voiceIntroUrl'] as String?,
      pricePerMinuteCoins: json['pricePerMinuteCoins'] as int? ?? 10,
      isOnline: json['isOnline'] as bool? ?? false,
      isOnCall: json['isOnCall'] as bool? ?? false,
      ratingAvg: (json['ratingAvg'] as num?)?.toDouble() ?? 5.0,
      totalReviews: json['totalReviews'] as int? ?? 0,
      totalCallsCompleted: json['totalCallsCompleted'] as int? ?? 0,
      languages: languagesList,
    );
  }
}
