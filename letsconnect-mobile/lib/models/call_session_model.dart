class CallTokenModel {
  final String callSessionId;
  final String channelName;
  final String agoraAppId;
  final String agoraToken;
  final int ratePerMinuteCoins;
  final int? callerBalance;

  CallTokenModel({
    required this.callSessionId,
    required this.channelName,
    required this.agoraAppId,
    required this.agoraToken,
    required this.ratePerMinuteCoins,
    this.callerBalance,
  });

  factory CallTokenModel.fromJson(Map<String, dynamic> json) {
    return CallTokenModel(
      callSessionId: json['callSessionId'] as String,
      channelName: json['channelName'] as String,
      agoraAppId: json['agoraAppId'] as String,
      agoraToken: json['agoraToken'] as String,
      ratePerMinuteCoins: json['ratePerMinuteCoins'] as int,
      callerBalance: json['callerBalance'] as int?,
    );
  }
}
