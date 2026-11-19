import 'dart:convert';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class AppSession {
  const AppSession({
    required this.accessToken,
    required this.userId,
  });

  final String accessToken;
  final String userId;

  factory AppSession.fromAuthResponse(Map<String, dynamic> response) {
    final token = response['token'];
    final userId = response['userId'];
    if (token is! String ||
        token.isEmpty ||
        userId is! String ||
        userId.isEmpty) {
      throw const FormatException('Authentication response is missing session fields');
    }

    return AppSession(
      accessToken: token,
      userId: userId,
    );
  }
}

class SessionStore {
  SessionStore({FlutterSecureStorage? storage})
      : _storage = storage ?? const FlutterSecureStorage();

  static const _sessionKey = 'connecto.session';

  final FlutterSecureStorage _storage;

  Future<AppSession?> readSession() async {
    final storedSession = await _storage.read(key: _sessionKey);
    if (storedSession == null) {
      return null;
    }

    final value = jsonDecode(storedSession);
    if (value is! Map<String, dynamic>) {
      throw const FormatException('Stored session is invalid');
    }

    final token = value['accessToken'];
    final userId = value['userId'];
    if (token is! String || token.isEmpty || userId is! String || userId.isEmpty) {
      throw const FormatException('Stored session is incomplete');
    }

    return AppSession(accessToken: token, userId: userId);
  }

  Future<void> saveSession(AppSession session) async {
    if (session.accessToken.isEmpty || session.userId.isEmpty) {
      throw ArgumentError('A session requires a token and user ID');
    }

    await _storage.write(
      key: _sessionKey,
      value: jsonEncode({
        'accessToken': session.accessToken,
        'userId': session.userId,
      }),
    );
  }

  Future<void> clearSession() => _storage.delete(key: _sessionKey);
}
