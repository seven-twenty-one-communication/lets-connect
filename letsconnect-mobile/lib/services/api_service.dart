import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/category_model.dart';
import '../models/listener_model.dart';
import '../models/call_session_model.dart';
import 'session_store.dart';

class ApiService {
  // Use 10.0.2.2 for Android Emulator, or your local machine IP for physical device
  static const String baseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8080/api',
  );
  static final SessionStore _sessionStore = SessionStore();

  static Future<Map<String, String>> _authenticatedHeaders() async {
    final session = await _sessionStore.readSession();
    if (session == null) {
      throw StateError('Authentication is required for this request');
    }

    return {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ${session.accessToken}',
    };
  }

  static Future<AppSession?> currentSession() => _sessionStore.readSession();

  static Future<void> clearSession() => _sessionStore.clearSession();

  static Future<bool> sendOtp(String phoneNumber) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/auth/send-otp'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'phoneNumber': phoneNumber}),
      );
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }

  static Future<AppSession?> verifyOtp(String phoneNumber, String otp) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/auth/verify-otp'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'phoneNumber': phoneNumber,
          'otp': otp,
        }),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body) as Map<String, dynamic>;
        final session = AppSession.fromAuthResponse(data);
        await _sessionStore.saveSession(session);
        return session;
      }
      return null;
    } catch (e) {
      return null;
    }
  }

  static Future<List<CategoryModel>> getCategories() async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/categories'),
        headers: await _authenticatedHeaders(),
      );
      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((json) => CategoryModel.fromJson(json)).toList();
      }
      return [];
    } catch (e) {
      return [];
    }
  }

  static Future<List<ListenerModel>> getListeners({int? categoryId, bool? isOnline}) async {
    try {
      String url = '$baseUrl/listeners?';
      if (categoryId != null) url += 'categoryId=$categoryId&';
      if (isOnline != null) url += 'isOnline=$isOnline&';

      final response = await http.get(
        Uri.parse(url),
        headers: await _authenticatedHeaders(),
      );
      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((json) => ListenerModel.fromJson(json)).toList();
      }
      return [];
    } catch (e) {
      return [];
    }
  }

  static Future<CallTokenModel?> initiateCall({
    required String callerId,
    required String listenerId,
    int? categoryId,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/calls/initiate'),
        headers: await _authenticatedHeaders(),
        body: jsonEncode({
          'callerId': callerId,
          'listenerId': listenerId,
          'categoryId': categoryId,
        }),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return CallTokenModel.fromJson(data);
      }
      return null;
    } catch (e) {
      return null;
    }
  }

  static Future<CallTokenModel?> acceptCall(String callId) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/calls/$callId/accept'),
        headers: await _authenticatedHeaders(),
      );
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return CallTokenModel.fromJson(data);
      }
      return null;
    } catch (e) {
      return null;
    }
  }

  static Future<bool> rejectCall(String callId) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/calls/$callId/reject'),
        headers: await _authenticatedHeaders(),
      );
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }

  static Future<bool> endCall(String callId) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/calls/$callId/end'),
        headers: await _authenticatedHeaders(),
      );
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }

  static Future<int> getCoinBalance(String userId) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/wallet/$userId/balance'),
        headers: await _authenticatedHeaders(),
      );
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return data['coinBalance'] as int? ?? 0;
      }
      return 0;
    } catch (e) {
      return 0;
    }
  }

  static Future<bool> verifyPlayPurchase({
    required String userId,
    required String signedData,
    required String signature,
    required String productId,
    required String orderId,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/wallet/verify-play-purchase'),
        headers: await _authenticatedHeaders(),
        body: jsonEncode({
          'userId': userId,
          'signedData': signedData,
          'signature': signature,
          'productId': productId,
          'orderId': orderId,
        }),
      );
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }

  static Future<void> addTestCoins(String userId, int coins) async {
    try {
      await http.post(
        Uri.parse('$baseUrl/wallet/$userId/add-test-coins?coins=$coins'),
        headers: await _authenticatedHeaders(),
      );
    } catch (_) {}
  }
}
