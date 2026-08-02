import 'package:flutter/material.dart';

import 'api_client.dart';

void main() => runApp(const CommitMateApp());

class AppColors {
  static const purple = Color(0xFF6551E8);
  static const lavender = Color(0xFFF1EEFF);
  static const yellow = Color(0xFFFFC83D);
  static const ink = Color(0xFF17203A);
  static const muted = Color(0xFF7B8194);
  static const background = Color(0xFFF8F8FC);
  static const danger = Color(0xFFF06472);
  static const success = Color(0xFF43B884);
}

class CommitMateApp extends StatefulWidget {
  const CommitMateApp({super.key});
  @override
  State<CommitMateApp> createState() => _CommitMateAppState();
}

class _CommitMateAppState extends State<CommitMateApp> {
  final api = ApiClient();
  Map<String, dynamic>? user;
  bool loading = true;

  @override
  void initState() {
    super.initState();
    _restore();
  }

  Future<void> _restore() async {
    await api.initialize();
    if (!api.hasSession) {
      if (mounted) setState(() => loading = false);
      return;
    }
    try {
      user = Map<String, dynamic>.from(await api.get('/me'));
    } catch (_) {
      user = null;
    }
    if (mounted) setState(() => loading = false);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'CommitMate',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: AppColors.purple),
        scaffoldBackgroundColor: AppColors.background,
        fontFamilyFallback: const ['Pretendard', 'Noto Sans KR'],
        cardTheme: CardThemeData(
          elevation: 0,
          color: Colors.white,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(22),
          ),
        ),
        inputDecorationTheme: InputDecorationTheme(
          filled: true,
          fillColor: Colors.white,
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(14),
            borderSide: BorderSide.none,
          ),
        ),
      ),
      home: loading
          ? const Scaffold(body: Center(child: CircularProgressIndicator()))
          : user == null
          ? AuthScreen(
              api: api,
              onAuthenticated: (value) => setState(() => user = value),
            )
          : AppShell(
              api: api,
              user: user!,
              onUserChanged: (value) => setState(() => user = value),
              onLogout: () => setState(() => user = null),
            ),
    );
  }
}

class AuthScreen extends StatefulWidget {
  const AuthScreen({
    super.key,
    required this.api,
    required this.onAuthenticated,
  });
  final ApiClient api;
  final ValueChanged<Map<String, dynamic>> onAuthenticated;
  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
  final email = TextEditingController();
  final password = TextEditingController();
  bool busy = false;

  Future<void> login() async {
    setState(() => busy = true);
    try {
      final result = await widget.api.post('/auth/login', {
        'email': email.text.trim(),
        'password': password.text,
      });
      widget.onAuthenticated(Map<String, dynamic>.from(result));
    } catch (error) {
      if (mounted) _message(context, error.toString());
    } finally {
      if (mounted) setState(() => busy = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    body: SafeArea(
      child: ListView(
        padding: const EdgeInsets.all(28),
        children: [
          const SizedBox(height: 48),
          const CircleAvatar(
            radius: 42,
            backgroundColor: AppColors.lavender,
            child: Icon(
              Icons.fact_check_rounded,
              color: AppColors.purple,
              size: 44,
            ),
          ),
          const SizedBox(height: 18),
          const Center(
            child: Text(
              'CommitMate',
              style: TextStyle(fontSize: 28, fontWeight: FontWeight.w900),
            ),
          ),
          const Center(
            child: Text(
              '약속을 지키고, 벌금은 줄이고',
              style: TextStyle(color: AppColors.muted),
            ),
          ),
          const SizedBox(height: 36),
          TextField(
            controller: email,
            keyboardType: TextInputType.emailAddress,
            decoration: const InputDecoration(
              labelText: '이메일',
              prefixIcon: Icon(Icons.email_outlined),
            ),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: password,
            obscureText: true,
            decoration: const InputDecoration(
              labelText: '비밀번호',
              prefixIcon: Icon(Icons.lock_outline),
            ),
          ),
          const SizedBox(height: 20),
          FilledButton(
            onPressed: busy ? null : login,
            child: Padding(
              padding: const EdgeInsets.all(14),
              child: Text(busy ? '로그인 중...' : '로그인'),
            ),
          ),
          TextButton(
            onPressed: busy ? null : () => _forgotPassword(context),
            child: const Text('비밀번호를 잊으셨나요?'),
          ),
          TextButton(
            onPressed: busy ? null : () => _showSignup(context),
            child: const Text('처음이신가요? 회원가입'),
          ),
          const SizedBox(height: 16),
          Text(
            '서버: ${ApiClient.baseUrl}',
            textAlign: TextAlign.center,
            style: const TextStyle(color: AppColors.muted, fontSize: 10),
          ),
        ],
      ),
    ),
  );

  Future<void> _showSignup(BuildContext context) async {
    final signupEmail = TextEditingController();
    final signupPassword = TextEditingController();
    final nickname = TextEditingController();
    final phone = TextEditingController();
    final code = TextEditingController();
    String? token;
    await showDialog<void>(
      context: context,
      builder: (dialogContext) => StatefulBuilder(
        builder: (context, setDialogState) {
          Future<void> sendCode() async {
            try {
              await widget.api.post('/phone-verifications/send', {
                'phoneNumber': phone.text,
              });
              if (context.mounted) _message(context, '인증번호를 발송했습니다.');
            } catch (e) {
              if (context.mounted) _message(context, e.toString());
            }
          }

          Future<void> verify() async {
            try {
              final result = await widget.api.post(
                '/phone-verifications/verify',
                {'phoneNumber': phone.text, 'code': code.text},
              );
              setDialogState(
                () => token = result['verificationToken']?.toString(),
              );
            } catch (e) {
              if (context.mounted) _message(context, e.toString());
            }
          }

          Future<void> signup() async {
            if (token == null) {
              _message(context, '휴대폰 인증을 완료해 주세요.');
              return;
            }
            try {
              final result = await widget.api.post('/auth/signup', {
                'email': signupEmail.text.trim(),
                'password': signupPassword.text,
                'nickname': nickname.text.trim(),
                'phoneNumber': phone.text,
                'phoneVerificationToken': token,
              });
              if (dialogContext.mounted) Navigator.pop(dialogContext);
              widget.onAuthenticated(Map<String, dynamic>.from(result));
            } catch (e) {
              if (context.mounted) _message(context, e.toString());
            }
          }

          return AlertDialog(
            title: const Text('회원가입'),
            content: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  TextField(
                    controller: signupEmail,
                    decoration: const InputDecoration(labelText: '이메일'),
                  ),
                  const SizedBox(height: 8),
                  TextField(
                    controller: signupPassword,
                    obscureText: true,
                    decoration: const InputDecoration(labelText: '비밀번호'),
                  ),
                  const SizedBox(height: 8),
                  TextField(
                    controller: nickname,
                    decoration: const InputDecoration(labelText: '닉네임'),
                  ),
                  const SizedBox(height: 8),
                  TextField(
                    controller: phone,
                    keyboardType: TextInputType.phone,
                    decoration: InputDecoration(
                      labelText: '휴대폰 번호',
                      suffixIcon: TextButton(
                        onPressed: sendCode,
                        child: const Text('발송'),
                      ),
                    ),
                  ),
                  const SizedBox(height: 8),
                  TextField(
                    controller: code,
                    decoration: InputDecoration(
                      labelText: '인증번호',
                      suffixIcon: TextButton(
                        onPressed: verify,
                        child: Text(token == null ? '확인' : '완료'),
                      ),
                    ),
                  ),
                ],
              ),
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('취소'),
              ),
              FilledButton(onPressed: signup, child: const Text('가입')),
            ],
          );
        },
      ),
    );
  }

  Future<void> _forgotPassword(BuildContext context) async {
    final resetEmail = TextEditingController(text: email.text);
    final ok = await _formDialog(context, '비밀번호 찾기', [
      TextField(
        controller: resetEmail,
        keyboardType: TextInputType.emailAddress,
        decoration: const InputDecoration(labelText: '가입 이메일'),
      ),
    ]);
    if (!ok) return;
    try {
      await widget.api.post('/auth/forgot-password', {
        'email': resetEmail.text.trim(),
      });
      if (context.mounted) _message(context, '재설정 메일을 확인해 주세요.');
    } catch (error) {
      if (context.mounted) _message(context, error.toString());
    }
  }
}

class AppShell extends StatefulWidget {
  const AppShell({
    super.key,
    required this.api,
    required this.user,
    required this.onUserChanged,
    required this.onLogout,
  });
  final ApiClient api;
  final Map<String, dynamic> user;
  final ValueChanged<Map<String, dynamic>> onUserChanged;
  final VoidCallback onLogout;
  @override
  State<AppShell> createState() => _AppShellState();
}

class _AppShellState extends State<AppShell> {
  int tab = 0;
  bool loading = true;
  List<Map<String, dynamic>> groups = [];
  int? selectedGroupId;
  List<Map<String, dynamic>> todos = [];
  Map<String, dynamic> fines = {'items': []};

  @override
  void initState() {
    super.initState();
    refresh();
  }

  Future<void> refresh() async {
    setState(() => loading = true);
    try {
      groups = List<Map<String, dynamic>>.from(
        (await widget.api.get(
          '/groups',
        )).map((e) => Map<String, dynamic>.from(e)),
      );
      if (groups.isNotEmpty) {
        if (!groups.any((g) => g['id'] == selectedGroupId)) {
          selectedGroupId = groups.first['id'] as int;
        }
        final results = await Future.wait([
          widget.api.get('/groups/$selectedGroupId/todos'),
          widget.api.get('/groups/$selectedGroupId/fines'),
        ]);
        todos = List<Map<String, dynamic>>.from(
          results[0].map((e) => Map<String, dynamic>.from(e)),
        );
        fines = Map<String, dynamic>.from(results[1]);
      } else {
        selectedGroupId = null;
        todos = [];
        fines = {'items': []};
      }
    } catch (e) {
      if (mounted) _message(context, e.toString());
    }
    if (mounted) setState(() => loading = false);
  }

  Future<void> chooseGroup(int? value) async {
    selectedGroupId = value;
    await refresh();
  }

  @override
  Widget build(BuildContext context) {
    final pages = [
      HomePage(
        user: widget.user,
        groups: groups,
        todos: todos,
        fines: fines,
        selectGroup: chooseGroup,
        selectedGroupId: selectedGroupId,
      ),
      TodoPage(
        api: widget.api,
        todos: todos,
        groupId: selectedGroupId,
        refresh: refresh,
      ),
      FinePage(
        api: widget.api,
        fines: fines,
        userId: widget.user['id'] as int,
        onUserChanged: widget.onUserChanged,
        refresh: refresh,
      ),
      StatsPage(todos: todos, fines: fines),
      ProfilePage(
        api: widget.api,
        user: widget.user,
        onUserChanged: widget.onUserChanged,
        onLogout: widget.onLogout,
      ),
    ];
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'CommitMate',
          style: TextStyle(fontWeight: FontWeight.w900),
        ),
        actions: [
          IconButton(onPressed: refresh, icon: const Icon(Icons.refresh)),
        ],
      ),
      body: loading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(onRefresh: refresh, child: pages[tab]),
      floatingActionButton: tab == 0
          ? FloatingActionButton.extended(
              onPressed: _groupActions,
              icon: const Icon(Icons.group_add),
              label: const Text('그룹 관리'),
            )
          : null,
      bottomNavigationBar: NavigationBar(
        selectedIndex: tab,
        onDestinationSelected: (value) => setState(() => tab = value),
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

  Future<void> _groupActions() async {
    await showModalBottomSheet<void>(
      context: context,
      showDragHandle: true,
      builder: (context) => SafeArea(
        child: Wrap(
          children: [
            ListTile(
              leading: const Icon(Icons.add),
              title: const Text('그룹 만들기'),
              onTap: () {
                Navigator.pop(context);
                _createGroup();
              },
            ),
            ListTile(
              leading: const Icon(Icons.login),
              title: const Text('참여 코드로 가입'),
              onTap: () {
                Navigator.pop(context);
                _joinGroup();
              },
            ),
            if (selectedGroupId != null)
              ListTile(
                leading: const Icon(Icons.info_outline),
                title: const Text('초대 및 멤버 보기'),
                onTap: () {
                  Navigator.pop(context);
                  _showGroupInfo();
                },
              ),
            if (selectedGroupId != null)
              ListTile(
                leading: const Icon(Icons.exit_to_app),
                title: const Text('현재 그룹 나가기'),
                onTap: () async {
                  Navigator.pop(context);
                  try {
                    await widget.api.post('/groups/$selectedGroupId/leave');
                    await refresh();
                  } catch (e) {
                    if (mounted) _message(this.context, e.toString());
                  }
                },
              ),
          ],
        ),
      ),
    );
  }

  Future<void> _createGroup() async {
    final name = TextEditingController();
    final description = TextEditingController();
    final ok = await _formDialog(context, '그룹 만들기', [
      TextField(
        controller: name,
        decoration: const InputDecoration(labelText: '그룹 이름'),
      ),
      const SizedBox(height: 8),
      TextField(
        controller: description,
        decoration: const InputDecoration(labelText: '설명'),
      ),
    ]);
    if (ok) {
      try {
        await widget.api.post('/groups', {
          'roomName': name.text,
          'description': description.text,
        });
        await refresh();
      } catch (e) {
        if (mounted) _message(context, e.toString());
      }
    }
  }

  Future<void> _joinGroup() async {
    final code = TextEditingController();
    final ok = await _formDialog(context, '그룹 참여', [
      TextField(
        controller: code,
        textCapitalization: TextCapitalization.characters,
        decoration: const InputDecoration(labelText: '참여 코드'),
      ),
    ]);
    if (ok) {
      try {
        await widget.api.post('/groups/join', {'joinCode': code.text.trim()});
        await refresh();
      } catch (e) {
        if (mounted) _message(context, e.toString());
      }
    }
  }

  Future<void> _showGroupInfo() async {
    try {
      final results = await Future.wait([
        widget.api.get('/groups/$selectedGroupId/invite'),
        widget.api.get('/groups/$selectedGroupId/members'),
      ]);
      final invite = Map<String, dynamic>.from(results[0]);
      final members = List<dynamic>.from(results[1]);
      if (!mounted) return;
      await showDialog<void>(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('그룹 정보'),
          content: SizedBox(
            width: 360,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                SelectableText(
                  '참여 코드: ${invite['joinCode']}\n초대 코드: ${invite['inviteCode']}',
                ),
                const Divider(),
                ...members.map(
                  (m) => ListTile(
                    contentPadding: EdgeInsets.zero,
                    leading: const Icon(Icons.person),
                    title: Text(m['nickname'].toString()),
                    trailing: Text(m['role'].toString()),
                  ),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('닫기'),
            ),
          ],
        ),
      );
    } catch (e) {
      if (mounted) _message(context, e.toString());
    }
  }
}

class HomePage extends StatelessWidget {
  const HomePage({
    super.key,
    required this.user,
    required this.groups,
    required this.todos,
    required this.fines,
    required this.selectGroup,
    required this.selectedGroupId,
  });
  final Map<String, dynamic> user;
  final List<Map<String, dynamic>> groups;
  final List<Map<String, dynamic>> todos;
  final Map<String, dynamic> fines;
  final ValueChanged<int?> selectGroup;
  final int? selectedGroupId;
  @override
  Widget build(BuildContext context) => ListView(
    padding: const EdgeInsets.all(20),
    children: [
      Text(
        '안녕하세요, ${user['nickname']}님 👋',
        style: const TextStyle(fontSize: 23, fontWeight: FontWeight.w900),
      ),
      const Text('오늘도 약속을 지켜볼까요?', style: TextStyle(color: AppColors.muted)),
      const SizedBox(height: 18),
      Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          gradient: const LinearGradient(
            colors: [AppColors.purple, Color(0xFF5138D7)],
          ),
          borderRadius: BorderRadius.circular(24),
        ),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    '현재 그룹 미납 벌금',
                    style: TextStyle(color: Colors.white70),
                  ),
                  Text(
                    '${fines['unpaidAmount'] ?? 0}원',
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 27,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                  Text(
                    '보유 포인트 ${user['point'] ?? 0}P',
                    style: const TextStyle(color: Colors.white70),
                  ),
                ],
              ),
            ),
            const Text('🐷', style: TextStyle(fontSize: 52)),
          ],
        ),
      ),
      const SizedBox(height: 18),
      DropdownButtonFormField<int>(
        initialValue: selectedGroupId,
        decoration: const InputDecoration(labelText: '현재 그룹'),
        items: groups
            .map(
              (g) => DropdownMenuItem(
                value: g['id'] as int,
                child: Text(g['roomName'].toString()),
              ),
            )
            .toList(),
        onChanged: selectGroup,
      ),
      const SizedBox(height: 18),
      const _Title('진행 중인 할 일'),
      if (todos.isEmpty) const _Empty('등록된 할 일이 없습니다.'),
      ...todos
          .where((t) => t['status'] == 'READY')
          .take(3)
          .map((t) => _TodoCard(todo: t)),
    ],
  );
}

class TodoPage extends StatelessWidget {
  const TodoPage({
    super.key,
    required this.api,
    required this.todos,
    required this.groupId,
    required this.refresh,
  });
  final ApiClient api;
  final List<Map<String, dynamic>> todos;
  final int? groupId;
  final Future<void> Function() refresh;
  @override
  Widget build(BuildContext context) => ListView(
    padding: const EdgeInsets.all(20),
    children: [
      Row(
        children: [
          const Expanded(child: _Title('할 일')),
          FilledButton.icon(
            onPressed: groupId == null ? null : () => _add(context),
            icon: const Icon(Icons.add),
            label: const Text('추가'),
          ),
        ],
      ),
      if (groupId == null) const _Empty('먼저 그룹을 만들거나 참여해 주세요.'),
      ...todos.map(
        (todo) => Dismissible(
          key: ValueKey(todo['id']),
          direction: DismissDirection.endToStart,
          background: Container(
            color: AppColors.danger,
            alignment: Alignment.centerRight,
            padding: const EdgeInsets.all(20),
            child: const Icon(Icons.delete, color: Colors.white),
          ),
          confirmDismiss: (_) async {
            try {
              await api.delete('/todos/${todo['id']}');
              await refresh();
              return true;
            } catch (e) {
              if (context.mounted) _message(context, e.toString());
              return false;
            }
          },
          child: GestureDetector(
            onTap: () async {
              try {
                await api.post('/todos/${todo['id']}/toggle');
                await refresh();
              } catch (e) {
                if (context.mounted) _message(context, e.toString());
              }
            },
            child: _TodoCard(todo: todo),
          ),
        ),
      ),
    ],
  );
  Future<void> _add(BuildContext context) async {
    final work = TextEditingController();
    final amount = TextEditingController(text: '1000');
    final deadline = TextEditingController();
    final ok = await _formDialog(context, '할 일 추가', [
      TextField(
        controller: work,
        decoration: const InputDecoration(labelText: '할 일'),
      ),
      const SizedBox(height: 8),
      TextField(
        controller: amount,
        keyboardType: TextInputType.number,
        decoration: const InputDecoration(labelText: '벌금'),
      ),
      const SizedBox(height: 8),
      TextField(
        controller: deadline,
        decoration: const InputDecoration(
          labelText: '마감 (예: 2026-08-05T21:00)',
        ),
      ),
    ]);
    if (ok) {
      try {
        await api.post('/groups/$groupId/todos', {
          'work': work.text,
          'amount': int.tryParse(amount.text) ?? 0,
          'deadline': deadline.text,
        });
        await refresh();
      } catch (e) {
        if (context.mounted) _message(context, e.toString());
      }
    }
  }
}

class FinePage extends StatelessWidget {
  const FinePage({
    super.key,
    required this.api,
    required this.fines,
    required this.userId,
    required this.onUserChanged,
    required this.refresh,
  });
  final ApiClient api;
  final Map<String, dynamic> fines;
  final int userId;
  final ValueChanged<Map<String, dynamic>> onUserChanged;
  final Future<void> Function() refresh;
  @override
  Widget build(BuildContext context) {
    final items = List<Map<String, dynamic>>.from(
      (fines['items'] ?? []).map((e) => Map<String, dynamic>.from(e)),
    );
    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        const _Title('벌금 내역'),
        Row(
          children: [
            Expanded(
              child: _Metric(
                '미납',
                '${fines['unpaidAmount'] ?? 0}원',
                AppColors.danger,
              ),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: _Metric(
                '납부',
                '${fines['paidAmount'] ?? 0}원',
                AppColors.success,
              ),
            ),
          ],
        ),
        const SizedBox(height: 16),
        if (items.isEmpty) const _Empty('부과된 벌금이 없습니다.'),
        ...items.map(
          (fine) => Card(
            child: ListTile(
              leading: const CircleAvatar(
                backgroundColor: AppColors.lavender,
                child: Icon(Icons.savings, color: AppColors.purple),
              ),
              title: Text(fine['work'].toString()),
              subtitle: Text('${fine['ownerNickname']} · ${fine['status']}'),
              trailing: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    '${fine['amount']}원',
                    style: const TextStyle(
                      color: AppColors.danger,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  if (fine['status'] == 'UNPAID' && fine['ownerId'] == userId)
                    TextButton(
                      onPressed: () => _pay(context, fine['id'] as int),
                      child: const Text('납부'),
                    ),
                ],
              ),
            ),
          ),
        ),
      ],
    );
  }

  Future<void> _pay(BuildContext context, int id) async {
    try {
      final user = await api.post('/fines/$id/pay', {'memo': '앱에서 납부'});
      onUserChanged(Map<String, dynamic>.from(user));
      await refresh();
    } catch (e) {
      if (context.mounted) _message(context, e.toString());
    }
  }
}

class StatsPage extends StatelessWidget {
  const StatsPage({super.key, required this.todos, required this.fines});
  final List<Map<String, dynamic>> todos;
  final Map<String, dynamic> fines;
  @override
  Widget build(BuildContext context) {
    final complete = todos.where((t) => t['status'] == 'FINISH').length;
    final rate = todos.isEmpty ? 0 : (complete * 100 / todos.length).round();
    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        const _Title('활동 통계'),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(22),
            child: Column(
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text(
                      '약속 달성률',
                      style: TextStyle(fontWeight: FontWeight.w800),
                    ),
                    Text(
                      '$rate%',
                      style: const TextStyle(
                        color: AppColors.purple,
                        fontSize: 25,
                        fontWeight: FontWeight.w900,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                LinearProgressIndicator(
                  value: rate / 100,
                  minHeight: 12,
                  borderRadius: BorderRadius.circular(8),
                ),
                const SizedBox(height: 20),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: [
                    _SmallStat('전체', '${todos.length}'),
                    _SmallStat('완료', '$complete'),
                    _SmallStat('미납', '${fines['unpaidCount'] ?? 0}'),
                  ],
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

class ProfilePage extends StatelessWidget {
  const ProfilePage({
    super.key,
    required this.api,
    required this.user,
    required this.onUserChanged,
    required this.onLogout,
  });
  final ApiClient api;
  final Map<String, dynamic> user;
  final ValueChanged<Map<String, dynamic>> onUserChanged;
  final VoidCallback onLogout;
  @override
  Widget build(BuildContext context) => ListView(
    padding: const EdgeInsets.all(20),
    children: [
      const CircleAvatar(
        radius: 45,
        backgroundColor: AppColors.lavender,
        child: Text('🙂', style: TextStyle(fontSize: 40)),
      ),
      const SizedBox(height: 10),
      Text(
        user['nickname'].toString(),
        textAlign: TextAlign.center,
        style: const TextStyle(fontSize: 21, fontWeight: FontWeight.w900),
      ),
      Text(
        user['email'].toString(),
        textAlign: TextAlign.center,
        style: const TextStyle(color: AppColors.muted),
      ),
      const SizedBox(height: 20),
      _Metric('보유 포인트', '${user['point'] ?? 0}P', AppColors.purple),
      const SizedBox(height: 12),
      FilledButton.tonalIcon(
        onPressed: () => _charge(context),
        icon: const Icon(Icons.add_card),
        label: const Text('포인트 충전'),
      ),
      OutlinedButton.icon(
        onPressed: () => _edit(context),
        icon: const Icon(Icons.edit),
        label: const Text('프로필 수정'),
      ),
      TextButton.icon(
        onPressed: () async {
          try {
            await api.post('/auth/logout');
          } finally {
            await api.clearSession();
            onLogout();
          }
        },
        icon: const Icon(Icons.logout),
        label: const Text('로그아웃'),
      ),
    ],
  );
  Future<void> _charge(BuildContext context) async {
    final amount = TextEditingController(text: '10000');
    final ok = await _formDialog(context, '포인트 충전', [
      TextField(
        controller: amount,
        keyboardType: TextInputType.number,
        decoration: const InputDecoration(labelText: '충전 금액'),
      ),
    ]);
    if (ok) {
      try {
        final result = await api.post('/me/points', {
          'amount': int.tryParse(amount.text) ?? 0,
        });
        onUserChanged(Map<String, dynamic>.from(result));
      } catch (e) {
        if (context.mounted) _message(context, e.toString());
      }
    }
  }

  Future<void> _edit(BuildContext context) async {
    final nickname = TextEditingController(text: user['nickname'].toString());
    final current = TextEditingController();
    final password = TextEditingController();
    final confirm = TextEditingController();
    final ok = await _formDialog(context, '프로필 수정', [
      TextField(
        controller: nickname,
        decoration: const InputDecoration(labelText: '닉네임'),
      ),
      const SizedBox(height: 8),
      TextField(
        controller: current,
        obscureText: true,
        decoration: const InputDecoration(labelText: '현재 비밀번호'),
      ),
      const SizedBox(height: 8),
      TextField(
        controller: password,
        obscureText: true,
        decoration: const InputDecoration(labelText: '새 비밀번호'),
      ),
      const SizedBox(height: 8),
      TextField(
        controller: confirm,
        obscureText: true,
        decoration: const InputDecoration(labelText: '새 비밀번호 확인'),
      ),
    ]);
    if (ok) {
      try {
        final result = await api.patch('/me', {
          'nickname': nickname.text,
          'currentPassword': current.text,
          'newPassword': password.text,
          'newPasswordConfirm': confirm.text,
        });
        onUserChanged(Map<String, dynamic>.from(result));
      } catch (e) {
        if (context.mounted) _message(context, e.toString());
      }
    }
  }
}

class _TodoCard extends StatelessWidget {
  const _TodoCard({required this.todo});
  final Map<String, dynamic> todo;
  @override
  Widget build(BuildContext context) {
    final status = todo['status'];
    return Card(
      child: ListTile(
        leading: Icon(
          status == 'FINISH'
              ? Icons.check_circle
              : status == 'EXPIRED'
              ? Icons.cancel
              : Icons.radio_button_unchecked,
          color: status == 'FINISH'
              ? AppColors.success
              : status == 'EXPIRED'
              ? AppColors.danger
              : AppColors.purple,
        ),
        title: Text(
          todo['work'].toString(),
          style: TextStyle(
            fontWeight: FontWeight.w700,
            decoration: status == 'FINISH' ? TextDecoration.lineThrough : null,
          ),
        ),
        subtitle: Text(
          '${todo['ownerNickname']} · ${todo['deadline'] ?? '마감 없음'}',
        ),
        trailing: Text(
          '${todo['fineAmount']}원',
          style: const TextStyle(
            color: AppColors.danger,
            fontWeight: FontWeight.w700,
          ),
        ),
      ),
    );
  }
}

class _Title extends StatelessWidget {
  const _Title(this.text);
  final String text;
  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.only(bottom: 12),
    child: Text(
      text,
      style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w900),
    ),
  );
}

class _Empty extends StatelessWidget {
  const _Empty(this.text);
  final String text;
  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.all(28),
    alignment: Alignment.center,
    child: Text(text, style: const TextStyle(color: AppColors.muted)),
  );
}

class _Metric extends StatelessWidget {
  const _Metric(this.label, this.value, this.color);
  final String label;
  final String value;
  final Color color;
  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.all(18),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: const TextStyle(color: AppColors.muted)),
          Text(
            value,
            style: TextStyle(
              color: color,
              fontSize: 21,
              fontWeight: FontWeight.w900,
            ),
          ),
        ],
      ),
    ),
  );
}

class _SmallStat extends StatelessWidget {
  const _SmallStat(this.label, this.value);
  final String label;
  final String value;
  @override
  Widget build(BuildContext context) => Column(
    children: [
      Text(
        value,
        style: const TextStyle(fontWeight: FontWeight.w900, fontSize: 20),
      ),
      Text(label, style: const TextStyle(color: AppColors.muted, fontSize: 11)),
    ],
  );
}

Future<bool> _formDialog(
  BuildContext context,
  String title,
  List<Widget> children,
) async =>
    await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(title),
        content: SingleChildScrollView(
          child: Column(mainAxisSize: MainAxisSize.min, children: children),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('취소'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('확인'),
          ),
        ],
      ),
    ) ??
    false;
void _message(BuildContext context, String text) =>
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(text)));
