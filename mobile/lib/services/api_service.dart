import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/category_model.dart';
import '../models/listener_model.dart';
import '../models/call_session_model.dart';

class ApiService {
  // Use 10.0.2.2 for Android Emulator, or your local machine IP for physical device
  static const String baseUrl = 'http://10.0.2.2:8080/api';

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

  static Future<Map<String, dynamic>?> verifyOtp(String phoneNumber, String otp) async {
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
        return jsonDecode(response.body) as Map<String, dynamic>;
      }
      return null;
    } catch (e) {
      return null;
    }
  }

  static Future<List<CategoryModel>> getCategories() async {
    try {
      final response = await http.get(Uri.parse('$baseUrl/categories'));
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

      final response = await http.get(Uri.parse(url));
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
        headers: {'Content-Type': 'application/json'},
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
      final response = await http.post(Uri.parse('$baseUrl/calls/$callId/accept'));
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
      final response = await http.post(Uri.parse('$baseUrl/calls/$callId/reject'));
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }

  static Future<bool> endCall(String callId) async {
    try {
      final response = await http.post(Uri.parse('$baseUrl/calls/$callId/end'));
      return response.statusCode == 200;
    } catch (e) {
      return false;
    }
  }

  static Future<int> getCoinBalance(String userId) async {
    try {
      final response = await http.get(Uri.parse('$baseUrl/wallet/$userId/balance'));
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
        headers: {'Content-Type': 'application/json'},
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
      await http.post(Uri.parse('$baseUrl/wallet/$userId/add-test-coins?coins=$coins'));
    } catch (_) {}
  }
}
