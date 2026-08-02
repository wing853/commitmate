import 'package:commit_mate/main.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('앱의 기본 탭과 홈 화면을 표시한다', (tester) async {
    await tester.pumpWidget(const CommitMateApp());

    expect(find.text('홈'), findsOneWidget);
    expect(find.text('할 일'), findsOneWidget);
    expect(find.text('벌금'), findsOneWidget);
    expect(find.text('통계'), findsOneWidget);
    expect(find.text('마이'), findsOneWidget);
    expect(find.text('안녕하세요, 민준님 👋'), findsOneWidget);
  });
}
