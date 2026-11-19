import 'package:lets_connect_app/services/session_store.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late SessionStore store;

  setUp(() {
    FlutterSecureStorage.setMockInitialValues({});
    store = SessionStore();
  });

  test('saves and restores the authenticated session', () async {
    const session = AppSession(
      accessToken: 'signed-token',
      userId: 'user-id',
    );

    await store.saveSession(session);

    final restored = await store.readSession();
    expect(restored?.accessToken, session.accessToken);
    expect(restored?.userId, session.userId);
  });

  test('clears the session', () async {
    const session = AppSession(
      accessToken: 'signed-token',
      userId: 'user-id',
    );
    await store.saveSession(session);

    await store.clearSession();

    expect(await store.readSession(), isNull);
  });

  test('rejects incomplete authentication responses', () {
    expect(
      () => AppSession.fromAuthResponse({'token': 'signed-token'}),
      throwsFormatException,
    );
  });
}
