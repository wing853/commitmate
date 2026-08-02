import 'package:flutter/material.dart';

void main() => runApp(const CommitMateApp());

class AppColors {
  static const purple = Color(0xFF6551E8);
  static const purpleDark = Color(0xFF5138D7);
  static const lavender = Color(0xFFF1EEFF);
  static const yellow = Color(0xFFFFC83D);
  static const ink = Color(0xFF17203A);
  static const muted = Color(0xFF7B8194);
  static const background = Color(0xFFF8F8FC);
  static const danger = Color(0xFFF06472);
  static const success = Color(0xFF43B884);
}

class CommitMateApp extends StatelessWidget {
  const CommitMateApp({super.key});

  @override
  Widget build(BuildContext context) {
    final scheme = ColorScheme.fromSeed(
      seedColor: AppColors.purple,
      primary: AppColors.purple,
      surface: Colors.white,
    );
    return MaterialApp(
      title: 'CommitMate',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: scheme,
        scaffoldBackgroundColor: AppColors.background,
        fontFamilyFallback: const ['Pretendard', 'Noto Sans KR'],
        textTheme: ThemeData.light().textTheme.apply(
          bodyColor: AppColors.ink,
          displayColor: AppColors.ink,
        ),
        cardTheme: CardThemeData(
          color: Colors.white,
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(22),
          ),
        ),
      ),
      home: const AppShell(),
    );
  }
}

class AppShell extends StatefulWidget {
  const AppShell({super.key});

  @override
  State<AppShell> createState() => _AppShellState();
}

class _AppShellState extends State<AppShell> {
  int _index = 0;

  static const _screens = [
    HomeScreen(),
    TodoScreen(),
    FineScreen(),
    StatsScreen(),
    ProfileScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: IndexedStack(index: _index, children: _screens),
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (value) => setState(() => _index = value),
        height: 72,
        backgroundColor: Colors.white,
        indicatorColor: AppColors.lavender,
        labelBehavior: NavigationDestinationLabelBehavior.alwaysShow,
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.home_outlined),
            selectedIcon: Icon(Icons.home),
            label: '홈',
          ),
          NavigationDestination(
            icon: Icon(Icons.task_alt_outlined),
            selectedIcon: Icon(Icons.task_alt),
            label: '할 일',
          ),
          NavigationDestination(
            icon: Icon(Icons.savings_outlined),
            selectedIcon: Icon(Icons.savings),
            label: '벌금',
          ),
          NavigationDestination(
            icon: Icon(Icons.bar_chart_outlined),
            selectedIcon: Icon(Icons.bar_chart),
            label: '통계',
          ),
          NavigationDestination(
            icon: Icon(Icons.person_outline),
            selectedIcon: Icon(Icons.person),
            label: '마이',
          ),
        ],
      ),
    );
  }
}

class AppHeader extends StatelessWidget {
  const AppHeader({super.key, required this.title, this.subtitle, this.action});

  final String title;
  final String? subtitle;
  final Widget? action;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 18, 20, 12),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (subtitle != null)
                  Text(
                    subtitle!,
                    style: const TextStyle(
                      color: AppColors.muted,
                      fontSize: 13,
                    ),
                  ),
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.w800,
                    letterSpacing: -1,
                  ),
                ),
              ],
            ),
          ),
          action ??
              const CircleAvatar(
                backgroundColor: AppColors.lavender,
                child: Icon(Icons.notifications_none),
              ),
        ],
      ),
    );
  }
}

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.only(bottom: 24),
      children: [
        const AppHeader(title: '안녕하세요, 민준님 👋', subtitle: '오늘도 약속을 지켜볼까요?'),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20),
          child: Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [AppColors.purple, AppColors.purpleDark],
              ),
              borderRadius: BorderRadius.circular(24),
              boxShadow: const [
                BoxShadow(
                  color: Color(0x336551E8),
                  blurRadius: 24,
                  offset: Offset(0, 12),
                ),
              ],
            ),
            child: const Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        '이번 달 내 벌금',
                        style: TextStyle(
                          color: Color(0xFFDCD7FF),
                          fontSize: 13,
                        ),
                      ),
                      SizedBox(height: 6),
                      Text(
                        '18,000원',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 28,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                      SizedBox(height: 12),
                      Text(
                        '지난달보다 6,000원 줄었어요!',
                        style: TextStyle(color: Colors.white, fontSize: 12),
                      ),
                    ],
                  ),
                ),
                _PigBadge(),
              ],
            ),
          ),
        ),
        const SizedBox(height: 22),
        const _SectionTitle(title: '오늘의 미션', action: '전체보기'),
        const _MissionCard(
          title: '운동 인증하기',
          group: '건강한 우리',
          time: '오늘 오후 9:00',
          progress: .7,
        ),
        const _MissionCard(
          title: '스터디 회고 작성',
          group: '개발 스터디',
          time: '오늘 오후 11:59',
          progress: .35,
        ),
        const SizedBox(height: 12),
        const _SectionTitle(title: '내 그룹', action: '그룹 추가'),
        SizedBox(
          height: 112,
          child: ListView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 20),
            children: const [
              _GroupCard(
                icon: '🏃',
                title: '건강한 우리',
                members: '5명',
                color: Color(0xFFECE8FF),
              ),
              _GroupCard(
                icon: '💻',
                title: '개발 스터디',
                members: '4명',
                color: Color(0xFFE8F4FF),
              ),
              _GroupCard(
                icon: '📚',
                title: '독서 모임',
                members: '7명',
                color: Color(0xFFFFF3D7),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class _PigBadge extends StatelessWidget {
  const _PigBadge();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 80,
      height: 80,
      decoration: const BoxDecoration(
        color: Color(0x337E6AEC),
        shape: BoxShape.circle,
      ),
      alignment: Alignment.center,
      child: const Text('🐷', style: TextStyle(fontSize: 46)),
    );
  }
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle({required this.title, required this.action});
  final String title;
  final String action;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 8, 20, 10),
      child: Row(
        children: [
          Expanded(
            child: Text(
              title,
              style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w800),
            ),
          ),
          Text(
            action,
            style: const TextStyle(
              color: AppColors.purple,
              fontSize: 13,
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ),
    );
  }
}

class _MissionCard extends StatelessWidget {
  const _MissionCard({
    required this.title,
    required this.group,
    required this.time,
    required this.progress,
  });
  final String title;
  final String group;
  final String time;
  final double progress;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.fromLTRB(20, 0, 20, 10),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Container(
              width: 42,
              height: 42,
              decoration: BoxDecoration(
                color: AppColors.lavender,
                borderRadius: BorderRadius.circular(13),
              ),
              child: const Icon(Icons.check_rounded, color: AppColors.purple),
            ),
            const SizedBox(width: 13),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(fontWeight: FontWeight.w700),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '$group · $time',
                    style: const TextStyle(
                      color: AppColors.muted,
                      fontSize: 11,
                    ),
                  ),
                  const SizedBox(height: 9),
                  LinearProgressIndicator(
                    value: progress,
                    minHeight: 5,
                    borderRadius: BorderRadius.circular(3),
                    backgroundColor: const Color(0xFFE9E9F1),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 10),
            const Icon(Icons.chevron_right, color: AppColors.muted),
          ],
        ),
      ),
    );
  }
}

class _GroupCard extends StatelessWidget {
  const _GroupCard({
    required this.icon,
    required this.title,
    required this.members,
    required this.color,
  });
  final String icon;
  final String title;
  final String members;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 150,
      margin: const EdgeInsets.only(right: 10),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(icon, style: const TextStyle(fontSize: 24)),
          const Spacer(),
          Text(title, style: const TextStyle(fontWeight: FontWeight.w700)),
          Text(
            members,
            style: const TextStyle(color: AppColors.muted, fontSize: 11),
          ),
        ],
      ),
    );
  }
}

class TodoScreen extends StatelessWidget {
  const TodoScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ListView(
      children: [
        AppHeader(
          title: '할 일',
          subtitle: '2026년 8월 2일',
          action: IconButton.filled(
            onPressed: () {},
            icon: const Icon(Icons.add),
          ),
        ),
        const Padding(
          padding: EdgeInsets.fromLTRB(20, 8, 20, 16),
          child: Row(
            children: [
              _CountChip(label: '전체', count: 6, selected: true),
              SizedBox(width: 8),
              _CountChip(label: '진행', count: 3),
              SizedBox(width: 8),
              _CountChip(label: '완료', count: 3),
            ],
          ),
        ),
        ...const [
          _TodoTile(
            title: '아침 8시까지 기상 인증',
            group: '건강한 우리',
            fine: '2,000원',
            done: true,
          ),
          _TodoTile(title: '알고리즘 문제 1개 풀기', group: '개발 스터디', fine: '3,000원'),
          _TodoTile(title: '책 20페이지 읽기', group: '독서 모임', fine: '1,000원'),
          _TodoTile(title: '주간 회고 작성하기', group: '개발 스터디', fine: '5,000원'),
        ],
      ],
    );
  }
}

class _CountChip extends StatelessWidget {
  const _CountChip({
    required this.label,
    required this.count,
    this.selected = false,
  });
  final String label;
  final int count;
  final bool selected;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 9),
      decoration: BoxDecoration(
        color: selected ? AppColors.purple : Colors.white,
        borderRadius: BorderRadius.circular(18),
      ),
      child: Text(
        '$label $count',
        style: TextStyle(
          color: selected ? Colors.white : AppColors.muted,
          fontWeight: FontWeight.w700,
          fontSize: 12,
        ),
      ),
    );
  }
}

class _TodoTile extends StatelessWidget {
  const _TodoTile({
    required this.title,
    required this.group,
    required this.fine,
    this.done = false,
  });
  final String title;
  final String group;
  final String fine;
  final bool done;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.fromLTRB(20, 0, 20, 10),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        leading: Icon(
          done ? Icons.check_circle : Icons.radio_button_unchecked,
          color: done ? AppColors.success : AppColors.purple,
        ),
        title: Text(
          title,
          style: TextStyle(
            fontWeight: FontWeight.w700,
            decoration: done ? TextDecoration.lineThrough : null,
            color: done ? AppColors.muted : null,
          ),
        ),
        subtitle: Text(
          group,
          style: const TextStyle(color: AppColors.muted, fontSize: 12),
        ),
        trailing: Text(
          fine,
          style: const TextStyle(
            color: AppColors.danger,
            fontWeight: FontWeight.w700,
            fontSize: 12,
          ),
        ),
      ),
    );
  }
}

class FineScreen extends StatelessWidget {
  const FineScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ListView(
      children: [
        const AppHeader(title: '벌금 내역', subtitle: '약속을 지키는 작은 동기부여'),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20),
          child: Row(
            children: const [
              Expanded(
                child: _SummaryBox(
                  label: '이번 달',
                  value: '18,000원',
                  color: AppColors.purple,
                ),
              ),
              SizedBox(width: 10),
              Expanded(
                child: _SummaryBox(
                  label: '미납 금액',
                  value: '9,000원',
                  color: AppColors.danger,
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 20),
        const _SectionTitle(title: '최근 내역', action: '전체보기'),
        ...const [
          _FineTile(
            icon: '💻',
            title: '알고리즘 문제 미완료',
            date: '개발 스터디 · 8월 1일',
            amount: '-3,000원',
            unpaid: true,
          ),
          _FineTile(
            icon: '🏃',
            title: '운동 인증 시간 초과',
            date: '건강한 우리 · 7월 29일',
            amount: '-2,000원',
            unpaid: true,
          ),
          _FineTile(
            icon: '📚',
            title: '독서 기록 미작성',
            date: '독서 모임 · 7월 26일',
            amount: '-1,000원',
          ),
        ],
      ],
    );
  }
}

class _SummaryBox extends StatelessWidget {
  const _SummaryBox({
    required this.label,
    required this.value,
    required this.color,
  });
  final String label;
  final String value;
  final Color color;
  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.all(18),
    decoration: BoxDecoration(
      color: Colors.white,
      borderRadius: BorderRadius.circular(20),
    ),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(color: AppColors.muted, fontSize: 12),
        ),
        const SizedBox(height: 7),
        Text(
          value,
          style: TextStyle(
            color: color,
            fontWeight: FontWeight.w800,
            fontSize: 20,
          ),
        ),
      ],
    ),
  );
}

class _FineTile extends StatelessWidget {
  const _FineTile({
    required this.icon,
    required this.title,
    required this.date,
    required this.amount,
    this.unpaid = false,
  });
  final String icon;
  final String title;
  final String date;
  final String amount;
  final bool unpaid;
  @override
  Widget build(BuildContext context) => Card(
    margin: const EdgeInsets.fromLTRB(20, 0, 20, 10),
    child: ListTile(
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      leading: CircleAvatar(
        backgroundColor: AppColors.lavender,
        child: Text(icon),
      ),
      title: Text(title, style: const TextStyle(fontWeight: FontWeight.w700)),
      subtitle: Text(
        date,
        style: const TextStyle(color: AppColors.muted, fontSize: 11),
      ),
      trailing: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          Text(
            amount,
            style: const TextStyle(
              color: AppColors.danger,
              fontWeight: FontWeight.w800,
            ),
          ),
          Text(
            unpaid ? '미납' : '납부 완료',
            style: TextStyle(
              color: unpaid ? AppColors.danger : AppColors.success,
              fontSize: 10,
            ),
          ),
        ],
      ),
    ),
  );
}

class StatsScreen extends StatelessWidget {
  const StatsScreen({super.key});
  @override
  Widget build(BuildContext context) => ListView(
    children: [
      const AppHeader(title: '나의 통계', subtitle: '8월 활동 리포트'),
      Padding(
        padding: const EdgeInsets.symmetric(horizontal: 20),
        child: Container(
          padding: const EdgeInsets.all(22),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(24),
          ),
          child: Column(
            children: [
              const Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    '약속 달성률',
                    style: TextStyle(fontWeight: FontWeight.w800, fontSize: 17),
                  ),
                  Text(
                    '78%',
                    style: TextStyle(
                      color: AppColors.purple,
                      fontWeight: FontWeight.w900,
                      fontSize: 24,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 18),
              LinearProgressIndicator(
                value: .78,
                minHeight: 12,
                borderRadius: BorderRadius.circular(8),
                backgroundColor: AppColors.lavender,
              ),
              const SizedBox(height: 22),
              const Row(
                mainAxisAlignment: MainAxisAlignment.spaceAround,
                children: [
                  _Stat(label: '완료', value: '25', color: AppColors.success),
                  _Stat(label: '실패', value: '7', color: AppColors.danger),
                  _Stat(label: '연속 달성', value: '6일', color: AppColors.yellow),
                ],
              ),
            ],
          ),
        ),
      ),
      const SizedBox(height: 20),
      const _SectionTitle(title: '그룹별 달성률', action: '이번 달'),
      const _RateTile(name: '건강한 우리', icon: '🏃', rate: .86),
      const _RateTile(name: '개발 스터디', icon: '💻', rate: .74),
      const _RateTile(name: '독서 모임', icon: '📚', rate: .62),
    ],
  );
}

class _Stat extends StatelessWidget {
  const _Stat({required this.label, required this.value, required this.color});
  final String label;
  final String value;
  final Color color;
  @override
  Widget build(BuildContext context) => Column(
    children: [
      Text(
        value,
        style: TextStyle(
          color: color,
          fontSize: 20,
          fontWeight: FontWeight.w900,
        ),
      ),
      Text(label, style: const TextStyle(color: AppColors.muted, fontSize: 11)),
    ],
  );
}

class _RateTile extends StatelessWidget {
  const _RateTile({required this.name, required this.icon, required this.rate});
  final String name;
  final String icon;
  final double rate;
  @override
  Widget build(BuildContext context) => Card(
    margin: const EdgeInsets.fromLTRB(20, 0, 20, 10),
    child: Padding(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Text(icon, style: const TextStyle(fontSize: 25)),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      name,
                      style: const TextStyle(fontWeight: FontWeight.w700),
                    ),
                    Text(
                      '${(rate * 100).round()}%',
                      style: const TextStyle(
                        color: AppColors.purple,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                LinearProgressIndicator(
                  value: rate,
                  minHeight: 6,
                  borderRadius: BorderRadius.circular(3),
                  backgroundColor: AppColors.lavender,
                ),
              ],
            ),
          ),
        ],
      ),
    ),
  );
}

class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});
  @override
  Widget build(BuildContext context) => ListView(
    children: [
      const AppHeader(title: '마이페이지', subtitle: '내 정보와 앱 설정'),
      const SizedBox(height: 8),
      const CircleAvatar(
        radius: 46,
        backgroundColor: AppColors.lavender,
        child: Text('🙂', style: TextStyle(fontSize: 42)),
      ),
      const SizedBox(height: 12),
      const Center(
        child: Text(
          '김민준',
          style: TextStyle(fontSize: 20, fontWeight: FontWeight.w800),
        ),
      ),
      const Center(
        child: Text(
          'minjun@example.com',
          style: TextStyle(color: AppColors.muted, fontSize: 12),
        ),
      ),
      const SizedBox(height: 24),
      const _ProfileItem(icon: Icons.groups_outlined, title: '내 그룹 관리'),
      const _ProfileItem(
        icon: Icons.account_balance_wallet_outlined,
        title: '벌금 및 결제 관리',
      ),
      const _ProfileItem(icon: Icons.notifications_outlined, title: '알림 설정'),
      const _ProfileItem(icon: Icons.lock_outline, title: '계정 및 보안'),
      const _ProfileItem(icon: Icons.help_outline, title: '도움말'),
    ],
  );
}

class _ProfileItem extends StatelessWidget {
  const _ProfileItem({required this.icon, required this.title});
  final IconData icon;
  final String title;
  @override
  Widget build(BuildContext context) => Card(
    margin: const EdgeInsets.fromLTRB(20, 0, 20, 8),
    child: ListTile(
      leading: Icon(icon, color: AppColors.purple),
      title: Text(title, style: const TextStyle(fontWeight: FontWeight.w600)),
      trailing: const Icon(Icons.chevron_right, color: AppColors.muted),
    ),
  );
}
