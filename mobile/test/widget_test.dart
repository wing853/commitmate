import 'package:commit_mate/main.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  testWidgets('저장된 세션이 없으면 로그인 화면을 표시한다', (tester) async {
    SharedPreferences.setMockInitialValues({});

    await tester.pumpWidget(const CommitMateApp());
    await tester.pumpAndSettle();

    expect(find.text('CommitMate'), findsOneWidget);
    expect(find.text('로그인'), findsOneWidget);
    expect(find.text('처음이신가요? 회원가입'), findsOneWidget);
  });
}
