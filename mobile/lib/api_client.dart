import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class ApiException implements Exception {
  const ApiException(this.message, {this.statusCode});
  final String message;
  final int? statusCode;
  @override
  String toString() => message;
}

class ApiClient {
  ApiClient({http.Client? client}) : _client = client ?? http.Client();

  static const baseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'https://commitmate-l75c.onrender.com/api/app',
  );

  final http.Client _client;
  String? _cookie;
  bool get hasSession => _cookie != null && _cookie!.isNotEmpty;

  Future<void> initialize() async {
    _cookie = (await SharedPreferences.getInstance()).getString(
      'sessionCookie',
    );
  }

  Future<dynamic> get(String path) => _request('GET', path);
  Future<dynamic> post(String path, [Map<String, dynamic>? body]) =>
      _request('POST', path, body);
  Future<dynamic> patch(String path, Map<String, dynamic> body) =>
      _request('PATCH', path, body);
  Future<dynamic> delete(String path) => _request('DELETE', path);

  Future<dynamic> _request(
    String method,
    String path, [
    Map<String, dynamic>? body,
  ]) async {
    final request = http.Request(method, Uri.parse('$baseUrl$path'));
    request.headers['Accept'] = 'application/json';
    request.headers['Content-Type'] = 'application/json; charset=utf-8';
    if (_cookie != null) request.headers['Cookie'] = _cookie!;
    if (body != null) request.body = jsonEncode(body);

    final streamed = await _client.send(request);
    final response = await http.Response.fromStream(streamed);
    final setCookie = response.headers['set-cookie'];
    if (setCookie != null) {
      _cookie = setCookie.split(';').first;
      final preferences = await SharedPreferences.getInstance();
      await preferences.setString('sessionCookie', _cookie!);
    }

    dynamic decoded;
    if (response.body.isNotEmpty) {
      try {
        decoded = jsonDecode(utf8.decode(response.bodyBytes));
      } catch (_) {
        decoded = response.body;
      }
    }
    if (response.statusCode < 200 || response.statusCode >= 300) {
      if (response.statusCode == 401) await clearSession();
      final message = decoded is Map ? decoded['message']?.toString() : null;
      throw ApiException(
        message ?? '서버 요청에 실패했습니다. (${response.statusCode})',
        statusCode: response.statusCode,
      );
    }
    return decoded;
  }

  Future<void> clearSession() async {
    _cookie = null;
    await (await SharedPreferences.getInstance()).remove('sessionCookie');
  }
}
