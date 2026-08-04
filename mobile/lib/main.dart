import 'package:flutter/material.dart';
import 'api_client.dart';

void main() => runApp(const CommitMateApp());

abstract final class C {
  static const primary = Color(0xff5b4ae8),
      dark = Color(0xff352a9e),
      coral = Color(0xffff765f),
      gold = Color(0xffffc85a),
      ink = Color(0xff17182c),
      muted = Color(0xff777a91),
      bg = Color(0xfff7f7fc),
      soft = Color(0xffefedff),
      green = Color(0xff28a876),
      red = Color(0xffe9556b);
}

class CommitMateApp extends StatefulWidget {
  const CommitMateApp({super.key});
  @override
  State<CommitMateApp> createState() => _AppState();
}

class _AppState extends State<CommitMateApp> {
  final api = ApiClient();
  Map<String, dynamic>? user;
  bool loading = true;
  @override
  void initState() {
    super.initState();
    restore();
  }

  Future<void> restore() async {
    await api.initialize();
    if (api.hasSession) {
      try {
        user = Map<String, dynamic>.from(await api.get('/me'));
      } catch (_) {
        await api.clearSession();
      }
    }
    if (mounted) setState(() => loading = false);
  }

  @override
  Widget build(BuildContext context) => MaterialApp(
    title: 'CommitMate',
    debugShowCheckedModeBanner: false,
    theme: ThemeData(
      useMaterial3: true,
      colorScheme: ColorScheme.fromSeed(
        seedColor: C.primary,
        primary: C.primary,
      ),
      scaffoldBackgroundColor: C.bg,
      fontFamilyFallback: const ['Pretendard', 'Noto Sans KR'],
      appBarTheme: const AppBarTheme(
        backgroundColor: C.bg,
        surfaceTintColor: Colors.transparent,
      ),
      cardTheme: CardThemeData(
        elevation: 0,
        color: Colors.white,
        margin: const EdgeInsets.only(bottom: 12),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(22)),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: Colors.white,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: Color(0xffe6e5f0)),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: Color(0xffe6e5f0)),
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          minimumSize: const Size(0, 52),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
        ),
      ),
      navigationBarTheme: const NavigationBarThemeData(
        height: 72,
        backgroundColor: Colors.white,
        indicatorColor: C.soft,
      ),
    ),
    home: loading
        ? const Splash()
        : user == null
        ? Auth(api: api, done: (v) => setState(() => user = v))
        : Shell(
            api: api,
            user: user!,
            changed: (v) => setState(() => user = v),
            logout: () => setState(() => user = null),
          ),
  );
}

class Splash extends StatelessWidget {
  const Splash({super.key});
  @override
  Widget build(BuildContext context) => const Scaffold(
    backgroundColor: C.primary,
    body: Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Image(image: AssetImage('assets/branding/app_icon.png'), width: 128),
          SizedBox(height: 22),
          Text(
            'CommitMate',
            style: TextStyle(
              color: Colors.white,
              fontSize: 30,
              fontWeight: FontWeight.w900,
            ),
          ),
          SizedBox(height: 7),
          Text('함께라서 끝까지 해내는 약속', style: TextStyle(color: Colors.white70)),
          SizedBox(height: 34),
          CircularProgressIndicator(color: Colors.white),
        ],
      ),
    ),
  );
}

class Auth extends StatefulWidget {
  const Auth({super.key, required this.api, required this.done});
  final ApiClient api;
  final ValueChanged<Map<String, dynamic>> done;
  @override
  State<Auth> createState() => _AuthState();
}

class _AuthState extends State<Auth> {
  final email = TextEditingController(), pw = TextEditingController();
  bool busy = false, hide = true;
  Future<void> login() async {
    if (email.text.trim().isEmpty || pw.text.isEmpty) {
      return msg(context, '이메일과 비밀번호를 입력해 주세요.');
    }
    setState(() => busy = true);
    try {
      widget.done(
        Map<String, dynamic>.from(
          await widget.api.post('/auth/login', {
            'email': email.text.trim(),
            'password': pw.text,
          }),
        ),
      );
    } catch (e) {
      if (mounted) msg(context, e.toString());
    } finally {
      if (mounted) setState(() => busy = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    body: SafeArea(
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 480),
          child: ListView(
            padding: const EdgeInsets.all(28),
            children: [
              const SizedBox(height: 20),
              Center(
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(30),
                  child: Image.asset(
                    'assets/branding/app_icon.png',
                    width: 105,
                  ),
                ),
              ),
              const SizedBox(height: 12),
              const Text(
                'CommitMate',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: C.primary,
                  fontSize: 18,
                  fontWeight: FontWeight.w900,
                ),
              ),
              const SizedBox(height: 22),
              const Text(
                '다시 만나 반가워요!',
                textAlign: TextAlign.center,
                style: TextStyle(fontSize: 28, fontWeight: FontWeight.w900),
              ),
              const SizedBox(height: 7),
              const Text(
                '친구들과 만든 약속을 오늘도 이어가세요.',
                textAlign: TextAlign.center,
                style: TextStyle(color: C.muted),
              ),
              const SizedBox(height: 34),
              TextField(
                controller: email,
                keyboardType: TextInputType.emailAddress,
                textInputAction: TextInputAction.next,
                decoration: const InputDecoration(
                  labelText: '이메일',
                  prefixIcon: Icon(Icons.alternate_email),
                ),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: pw,
                obscureText: hide,
                onSubmitted: (_) => login(),
                decoration: InputDecoration(
                  labelText: '비밀번호',
                  prefixIcon: const Icon(Icons.lock_outline),
                  suffixIcon: IconButton(
                    onPressed: () => setState(() => hide = !hide),
                    icon: Icon(
                      hide
                          ? Icons.visibility_outlined
                          : Icons.visibility_off_outlined,
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 20),
              FilledButton(
                onPressed: busy ? null : login,
                child: Text(busy ? '로그인 중...' : '로그인'),
              ),
              TextButton(
                onPressed: () => forgot(context),
                child: const Text('비밀번호를 잊으셨나요?'),
              ),
              const Divider(height: 32),
              OutlinedButton(
                onPressed: () => signup(context),
                style: OutlinedButton.styleFrom(minimumSize: const Size(0, 52)),
                child: const Text('처음이신가요? 회원가입'),
              ),
            ],
          ),
        ),
      ),
    ),
  );
  Future<void> forgot(BuildContext c) async {
    final x = TextEditingController(text: email.text);
    if (!await form(c, '비밀번호 찾기', [
      TextField(
        controller: x,
        decoration: const InputDecoration(labelText: '가입한 이메일'),
      ),
    ])) {
      return;
    }
    try {
      await widget.api.post('/auth/forgot-password', {'email': x.text.trim()});
      if (c.mounted) msg(c, '재설정 메일을 확인해 주세요.');
    } catch (e) {
      if (c.mounted) msg(c, e.toString());
    }
  }

  Future<void> signup(BuildContext c) async {
    final e = TextEditingController(),
        p = TextEditingController(),
        n = TextEditingController(),
        phone = TextEditingController(),
        code = TextEditingController();
    String? token;
    await showDialog<void>(
      context: c,
      builder: (dc) => StatefulBuilder(
        builder: (c, setS) => AlertDialog(
          title: const Text('회원가입'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: e,
                  decoration: const InputDecoration(labelText: '이메일'),
                ),
                gap,
                TextField(
                  controller: p,
                  obscureText: true,
                  decoration: const InputDecoration(labelText: '비밀번호'),
                ),
                gap,
                TextField(
                  controller: n,
                  decoration: const InputDecoration(labelText: '닉네임'),
                ),
                gap,
                TextField(
                  controller: phone,
                  keyboardType: TextInputType.phone,
                  decoration: InputDecoration(
                    labelText: '휴대폰 번호',
                    suffixIcon: TextButton(
                      onPressed: () async {
                        try {
                          await widget.api.post('/phone-verifications/send', {
                            'phoneNumber': phone.text,
                          });
                          if (c.mounted) msg(c, '인증번호를 발송했습니다.');
                        } catch (x) {
                          if (c.mounted) msg(c, x.toString());
                        }
                      },
                      child: const Text('발송'),
                    ),
                  ),
                ),
                gap,
                TextField(
                  controller: code,
                  decoration: InputDecoration(
                    labelText: '인증번호',
                    suffixIcon: TextButton(
                      onPressed: () async {
                        try {
                          final r = await widget.api.post(
                            '/phone-verifications/verify',
                            {'phoneNumber': phone.text, 'code': code.text},
                          );
                          setS(
                            () => token = r['verificationToken']?.toString(),
                          );
                        } catch (x) {
                          if (c.mounted) msg(c, x.toString());
                        }
                      },
                      child: Text(token == null ? '확인' : '완료'),
                    ),
                  ),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(c),
              child: const Text('취소'),
            ),
            FilledButton(
              onPressed: () async {
                if (token == null) return msg(c, '휴대폰 인증을 완료해 주세요.');
                try {
                  final r = await widget.api.post('/auth/signup', {
                    'email': e.text.trim(),
                    'password': p.text,
                    'nickname': n.text.trim(),
                    'phoneNumber': phone.text,
                    'phoneVerificationToken': token,
                  });
                  if (dc.mounted) Navigator.pop(dc);
                  widget.done(Map<String, dynamic>.from(r));
                } catch (x) {
                  if (c.mounted) msg(c, x.toString());
                }
              },
              child: const Text('가입하기'),
            ),
          ],
        ),
      ),
    );
  }
}

class Shell extends StatefulWidget {
  const Shell({
    super.key,
    required this.api,
    required this.user,
    required this.changed,
    required this.logout,
  });
  final ApiClient api;
  final Map<String, dynamic> user;
  final ValueChanged<Map<String, dynamic>> changed;
  final VoidCallback logout;
  @override
  State<Shell> createState() => _ShellState();
}

class _ShellState extends State<Shell> {
  int tab = 0;
  bool loading = true;
  List<Map<String, dynamic>> groups = [], todos = [];
  Map<String, dynamic> fines = {'items': []};
  int? groupId;
  @override
  void initState() {
    super.initState();
    refresh();
  }

  Future<void> refresh() async {
    if (mounted) setState(() => loading = true);
    try {
      groups = List<Map<String, dynamic>>.from(
        (await widget.api.get(
          '/groups',
        )).map((e) => Map<String, dynamic>.from(e)),
      );
      if (groups.isEmpty) {
        groupId = null;
        todos = [];
        fines = {'items': []};
      } else {
        if (!groups.any((g) => g['id'] == groupId)) {
          groupId = groups.first['id'] as int;
        }
        final r = await Future.wait([
          widget.api.get('/groups/$groupId/todos'),
          widget.api.get('/groups/$groupId/fines'),
        ]);
        todos = List<Map<String, dynamic>>.from(
          r[0].map((e) => Map<String, dynamic>.from(e)),
        );
        fines = Map<String, dynamic>.from(r[1]);
      }
    } catch (e) {
      if (mounted) msg(context, e.toString());
    } finally {
      if (mounted) setState(() => loading = false);
    }
  }

  @override
  Widget build(BuildContext c) {
    final pages = [
      Home(
        user: widget.user,
        groups: groups,
        todos: todos,
        fines: fines,
        groupId: groupId,
        select: (v) {
          groupId = v;
          refresh();
        },
      ),
      Todos(api: widget.api, todos: todos, groupId: groupId, refresh: refresh),
      Fines(
        api: widget.api,
        data: fines,
        userId: widget.user['id'] as int,
        changed: widget.changed,
        refresh: refresh,
      ),
      Stats(todos: todos, fines: fines),
      Profile(
        api: widget.api,
        user: widget.user,
        changed: widget.changed,
        logout: widget.logout,
      ),
    ];
    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(9),
              child: Image.asset('assets/branding/app_icon.png', width: 34),
            ),
            const SizedBox(width: 10),
            const Text(
              'CommitMate',
              style: TextStyle(fontWeight: FontWeight.w900),
            ),
          ],
        ),
        actions: [
          IconButton(
            onPressed: refresh,
            icon: const Icon(Icons.refresh_rounded),
          ),
        ],
      ),
      body: loading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: refresh,
              child: IndexedStack(index: tab, children: pages),
            ),
      floatingActionButton: tab == 0
          ? FloatingActionButton.extended(
              onPressed: groupMenu,
              backgroundColor: C.primary,
              foregroundColor: Colors.white,
              icon: const Icon(Icons.groups),
              label: const Text('그룹 관리'),
            )
          : null,
      bottomNavigationBar: NavigationBar(
        selectedIndex: tab,
        onDestinationSelected: (v) => setState(() => tab = v),
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

  Future<void> groupMenu() async => showModalBottomSheet<void>(
    context: context,
    showDragHandle: true,
    builder: (sc) => SafeArea(
      child: Wrap(
        children: [
          const ListTile(
            title: Text(
              '그룹 관리',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.w900),
            ),
          ),
          ListTile(
            leading: const RIcon(Icons.add),
            title: const Text('새 그룹 만들기'),
            onTap: () {
              Navigator.pop(sc);
              createGroup();
            },
          ),
          ListTile(
            leading: const RIcon(Icons.login),
            title: const Text('참여 코드로 가입'),
            onTap: () {
              Navigator.pop(sc);
              join();
            },
          ),
          if (groupId != null)
            ListTile(
              leading: const RIcon(Icons.settings),
              title: const Text('현재 그룹 상세 관리'),
              onTap: () {
                Navigator.pop(sc);
                manage();
              },
            ),
        ],
      ),
    ),
  );
  Future<void> createGroup() async {
    final n = TextEditingController(), d = TextEditingController();
    if (await form(context, '새 그룹 만들기', [
      TextField(
        controller: n,
        decoration: const InputDecoration(labelText: '그룹 이름'),
      ),
      gap,
      TextField(
        controller: d,
        maxLines: 3,
        decoration: const InputDecoration(labelText: '설명'),
      ),
    ])) {
      try {
        await widget.api.post('/groups', {
          'roomName': n.text.trim(),
          'description': d.text.trim(),
        });
        await refresh();
      } catch (e) {
        if (mounted) msg(context, e.toString());
      }
    }
  }

  Future<void> join() async {
    final x = TextEditingController();
    if (await form(context, '그룹 참여', [
      TextField(
        controller: x,
        textCapitalization: TextCapitalization.characters,
        decoration: const InputDecoration(labelText: '6자리 참여 코드'),
      ),
    ])) {
      try {
        await widget.api.post('/groups/join', {
          'joinCode': x.text.trim().toUpperCase(),
        });
        await refresh();
      } catch (e) {
        if (mounted) msg(context, e.toString());
      }
    }
  }

  Future<void> manage() async {
    try {
      final r = await Future.wait([
        widget.api.get('/groups/$groupId/invite'),
        widget.api.get('/groups/$groupId/members'),
      ]);
      final inv = Map<String, dynamic>.from(r[0]),
          members = List<Map<String, dynamic>>.from(
            r[1].map((e) => Map<String, dynamic>.from(e)),
          );
      if (!mounted) return;
      await showDialog<void>(
        context: context,
        builder: (dc) => AlertDialog(
          title: const Text('그룹 상세 관리'),
          content: SizedBox(
            width: 420,
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: C.soft,
                      borderRadius: BorderRadius.circular(16),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text('참여 코드'),
                        SelectableText(
                          '${inv['joinCode']}',
                          style: const TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.w900,
                            color: C.primary,
                          ),
                        ),
                      ],
                    ),
                  ),
                  gap,
                  ...members.map(
                    (m) => ListTile(
                      contentPadding: EdgeInsets.zero,
                      leading: CircleAvatar(
                        child: Text(m['nickname'].toString().characters.first),
                      ),
                      title: Text(m['nickname'].toString()),
                      subtitle: Text(m['role'] == 'ADMIN' ? '관리자' : '멤버'),
                      trailing:
                          m['id'] == widget.user['id'] || m['role'] == 'ADMIN'
                          ? null
                          : PopupMenuButton<String>(
                              onSelected: (a) async {
                                try {
                                  await widget.api.post(
                                    '/groups/$groupId/members/${m['id']}/$a',
                                  );
                                  if (dc.mounted) Navigator.pop(dc);
                                  await refresh();
                                } catch (e) {
                                  if (mounted) msg(context, e.toString());
                                }
                              },
                              itemBuilder: (_) => const [
                                PopupMenuItem(
                                  value: 'delegate',
                                  child: Text('관리자 위임'),
                                ),
                                PopupMenuItem(
                                  value: 'kick',
                                  child: Text('내보내기'),
                                ),
                              ],
                            ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.pop(dc);
                editGroup();
              },
              child: const Text('정보 수정'),
            ),
            TextButton(
              onPressed: () async {
                Navigator.pop(dc);
                if (await confirm(context, '이 그룹에서 나갈까요?')) {
                  try {
                    await widget.api.post('/groups/$groupId/leave');
                    await refresh();
                  } catch (e) {
                    if (mounted) msg(context, e.toString());
                  }
                }
              },
              child: const Text('나가기'),
            ),
            TextButton(
              onPressed: () => Navigator.pop(dc),
              child: const Text('닫기'),
            ),
          ],
        ),
      );
    } catch (e) {
      if (mounted) msg(context, e.toString());
    }
  }

  Future<void> editGroup() async {
    final g = groups.firstWhere((x) => x['id'] == groupId),
        n = TextEditingController(text: g['roomName']?.toString()),
        d = TextEditingController(text: g['description']?.toString() ?? '');
    if (await form(context, '그룹 정보 수정', [
      TextField(
        controller: n,
        decoration: const InputDecoration(labelText: '그룹 이름'),
      ),
      gap,
      TextField(
        controller: d,
        maxLines: 3,
        decoration: const InputDecoration(labelText: '설명'),
      ),
    ])) {
      try {
        await widget.api.patch('/groups/$groupId', {
          'roomName': n.text.trim(),
          'description': d.text.trim(),
        });
        await refresh();
      } catch (e) {
        if (mounted) msg(context, e.toString());
      }
    }
  }
}

class Home extends StatelessWidget {
  const Home({
    super.key,
    required this.user,
    required this.groups,
    required this.todos,
    required this.fines,
    required this.groupId,
    required this.select,
  });
  final Map<String, dynamic> user, fines;
  final List<Map<String, dynamic>> groups, todos;
  final int? groupId;
  final ValueChanged<int?> select;
  @override
  Widget build(BuildContext c) {
    final ready = todos.where((x) => x['status'] == 'READY').toList();
    return ListView(
      padding: pad,
      children: [
        Text('${user['nickname']}님, 오늘도 반가워요 👋', style: title),
        const Text(
          '작은 약속 하나가 팀의 큰 변화를 만들어요.',
          style: TextStyle(color: C.muted),
        ),
        const SizedBox(height: 22),
        Container(
          padding: const EdgeInsets.all(22),
          decoration: BoxDecoration(
            gradient: const LinearGradient(
              colors: [C.dark, C.primary, Color(0xff8a70f4)],
            ),
            borderRadius: BorderRadius.circular(28),
            boxShadow: const [
              BoxShadow(
                color: Color(0x385b4ae8),
                blurRadius: 24,
                offset: Offset(0, 12),
              ),
            ],
          ),
          child: Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      '현재 미납 벌금',
                      style: TextStyle(color: Colors.white70),
                    ),
                    Text(
                      '${money(fines['unpaidAmount'])}원',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 30,
                        fontWeight: FontWeight.w900,
                      ),
                    ),
                    Text(
                      '보유 포인트 ${money(user['point'])}P',
                      style: const TextStyle(color: Colors.white70),
                    ),
                  ],
                ),
              ),
              const Icon(Icons.savings, size: 64, color: C.gold),
            ],
          ),
        ),
        const SizedBox(height: 22),
        if (groups.isNotEmpty)
          DropdownButtonFormField<int>(
            initialValue: groupId,
            decoration: const InputDecoration(
              labelText: '현재 그룹',
              prefixIcon: Icon(Icons.groups),
            ),
            items: groups
                .map(
                  (g) => DropdownMenuItem(
                    value: g['id'] as int,
                    child: Text(g['roomName'].toString()),
                  ),
                )
                .toList(),
            onChanged: select,
          ),
        if (groups.isEmpty)
          const Empty(
            Icons.group_add_outlined,
            '아직 참여한 그룹이 없어요',
            '그룹을 만들거나 참여 코드로 친구들과 시작해 보세요.',
          ),
        const SizedBox(height: 24),
        Section('진행 중인 할 일'),
        if (ready.isEmpty)
          const Empty(
            Icons.task_alt,
            '모든 약속을 지켰어요!',
            '새로운 할 일을 추가해 흐름을 이어가세요.',
          ),
        ...ready.take(3).map((x) => TodoCard(x)),
      ],
    );
  }
}

class Todos extends StatelessWidget {
  const Todos({
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
  Widget build(BuildContext c) => ListView(
    padding: pad,
    children: [
      Row(
        children: [
          const Expanded(child: Section('할 일')),
          FilledButton.icon(
            onPressed: groupId == null ? null : () => edit(c),
            icon: const Icon(Icons.add),
            label: const Text('추가'),
          ),
        ],
      ),
      if (groupId == null)
        const Empty(
          Icons.groups_outlined,
          '그룹을 먼저 선택해 주세요',
          '홈에서 그룹을 만들거나 참여할 수 있어요.',
        ),
      ...todos.map(
        (x) => Dismissible(
          key: ValueKey(x['id']),
          direction: DismissDirection.endToStart,
          background: Container(
            margin: const EdgeInsets.only(bottom: 12),
            alignment: Alignment.centerRight,
            padding: const EdgeInsets.only(right: 24),
            decoration: BoxDecoration(
              color: C.red,
              borderRadius: BorderRadius.circular(22),
            ),
            child: const Icon(Icons.delete, color: Colors.white),
          ),
          confirmDismiss: (_) async {
            if (!await confirm(c, '이 할 일을 삭제할까요?')) return false;
            try {
              await api.delete('/todos/${x['id']}');
              await refresh();
              return true;
            } catch (e) {
              if (c.mounted) msg(c, e.toString());
              return false;
            }
          },
          child: GestureDetector(
            onTap: () async {
              try {
                await api.post('/todos/${x['id']}/toggle');
                await refresh();
              } catch (e) {
                if (c.mounted) msg(c, e.toString());
              }
            },
            onLongPress: () => edit(c, todo: x),
            child: TodoCard(x),
          ),
        ),
      ),
    ],
  );
  Future<void> edit(BuildContext c, {Map<String, dynamic>? todo}) async {
    final w = TextEditingController(text: todo?['work']?.toString()),
        a = TextEditingController(text: '${todo?['fineAmount'] ?? 1000}');
    DateTime deadline =
        DateTime.tryParse(todo?['deadline']?.toString() ?? '') ??
        DateTime.now().add(const Duration(days: 1));
    final ok = await showDialog<bool>(
      context: c,
      builder: (dc) => StatefulBuilder(
        builder: (c, setS) => AlertDialog(
          title: Text(todo == null ? '할 일 추가' : '할 일 수정'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: w,
                decoration: const InputDecoration(labelText: '할 일'),
              ),
              gap,
              TextField(
                controller: a,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(labelText: '미완료 시 벌금'),
              ),
              gap,
              ListTile(
                tileColor: C.soft,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                ),
                leading: const Icon(Icons.event, color: C.primary),
                title: const Text('마감 일시'),
                subtitle: Text(date(deadline.toIso8601String())),
                onTap: () async {
                  final d = await showDatePicker(
                    context: c,
                    initialDate: deadline,
                    firstDate: DateTime.now().subtract(const Duration(days: 1)),
                    lastDate: DateTime.now().add(const Duration(days: 3650)),
                  );
                  if (d == null || !c.mounted) return;
                  final t = await showTimePicker(
                    context: c,
                    initialTime: TimeOfDay.fromDateTime(deadline),
                  );
                  if (t != null) {
                    setS(
                      () => deadline = DateTime(
                        d.year,
                        d.month,
                        d.day,
                        t.hour,
                        t.minute,
                      ),
                    );
                  }
                },
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(c, false),
              child: const Text('취소'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(c, true),
              child: const Text('저장'),
            ),
          ],
        ),
      ),
    );
    if (ok != true) return;
    try {
      final b = {
        'work': w.text.trim(),
        'amount': int.tryParse(a.text) ?? 0,
        'deadline': deadline.toIso8601String(),
      };
      if (todo == null) {
        await api.post('/groups/$groupId/todos', b);
      } else {
        await api.patch('/todos/${todo['id']}', b);
      }
      await refresh();
    } catch (e) {
      if (c.mounted) msg(c, e.toString());
    }
  }
}

class Fines extends StatelessWidget {
  const Fines({
    super.key,
    required this.api,
    required this.data,
    required this.userId,
    required this.changed,
    required this.refresh,
  });
  final ApiClient api;
  final Map<String, dynamic> data;
  final int userId;
  final ValueChanged<Map<String, dynamic>> changed;
  final Future<void> Function() refresh;
  @override
  Widget build(BuildContext c) {
    final xs = List<Map<String, dynamic>>.from(
      (data['items'] ?? []).map((e) => Map<String, dynamic>.from(e)),
    );
    return ListView(
      padding: pad,
      children: [
        const Section('벌금 내역'),
        Row(
          children: [
            Expanded(
              child: Metric('미납', '${money(data['unpaidAmount'])}원', C.red),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: Metric('납부', '${money(data['paidAmount'])}원', C.green),
            ),
          ],
        ),
        if (xs.isEmpty)
          const Empty(Icons.savings_outlined, '부과된 벌금이 없어요', '약속을 잘 지키고 있네요!'),
        ...xs.map(
          (x) => Card(
            child: ListTile(
              contentPadding: const EdgeInsets.all(14),
              leading: const RIcon(Icons.savings),
              title: Text(
                x['work'].toString(),
                style: const TextStyle(fontWeight: FontWeight.w800),
              ),
              subtitle: Text(
                '${x['ownerNickname']} · ${x['status'] == 'PAID' ? '납부 완료' : '미납'}',
              ),
              trailing: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    '${money(x['amount'])}원',
                    style: const TextStyle(
                      color: C.red,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                  if (x['status'] == 'UNPAID' && x['ownerId'] == userId)
                    InkWell(
                      onTap: () async {
                        try {
                          changed(
                            Map<String, dynamic>.from(
                              await api.post('/fines/${x['id']}/pay', {
                                'memo': '앱에서 납부',
                              }),
                            ),
                          );
                          await refresh();
                        } catch (e) {
                          if (c.mounted) msg(c, e.toString());
                        }
                      },
                      child: const Text(
                        '납부하기',
                        style: TextStyle(color: C.primary, fontSize: 12),
                      ),
                    ),
                ],
              ),
            ),
          ),
        ),
      ],
    );
  }
}

class Stats extends StatelessWidget {
  const Stats({super.key, required this.todos, required this.fines});
  final List<Map<String, dynamic>> todos;
  final Map<String, dynamic> fines;
  @override
  Widget build(BuildContext c) {
    final done = todos.where((x) => x['status'] == 'FINISH').length,
        rate = todos.isEmpty ? 0 : (done * 100 / todos.length).round();
    return ListView(
      padding: pad,
      children: [
        const Section('활동 통계'),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(24),
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
                        color: C.primary,
                        fontSize: 30,
                        fontWeight: FontWeight.w900,
                      ),
                    ),
                  ],
                ),
                gap,
                LinearProgressIndicator(
                  value: rate / 100,
                  minHeight: 12,
                  borderRadius: BorderRadius.circular(8),
                ),
                const SizedBox(height: 24),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: [
                    Small('전체', '${todos.length}'),
                    Small('완료', '$done'),
                    Small('미납', '${fines['unpaidCount'] ?? 0}'),
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

class Profile extends StatelessWidget {
  const Profile({
    super.key,
    required this.api,
    required this.user,
    required this.changed,
    required this.logout,
  });
  final ApiClient api;
  final Map<String, dynamic> user;
  final ValueChanged<Map<String, dynamic>> changed;
  final VoidCallback logout;
  @override
  Widget build(BuildContext c) => ListView(
    padding: pad,
    children: [
      CircleAvatar(
        radius: 48,
        backgroundColor: C.soft,
        child: Text(
          user['nickname'].toString().characters.first,
          style: const TextStyle(
            fontSize: 34,
            fontWeight: FontWeight.w900,
            color: C.primary,
          ),
        ),
      ),
      gap,
      Text(
        user['nickname'].toString(),
        textAlign: TextAlign.center,
        style: title,
      ),
      Text(
        user['email'].toString(),
        textAlign: TextAlign.center,
        style: const TextStyle(color: C.muted),
      ),
      const SizedBox(height: 22),
      Metric('보유 포인트', '${money(user['point'])}P', C.primary),
      FilledButton.tonalIcon(
        onPressed: () => charge(c),
        icon: const Icon(Icons.add_card),
        label: const Text('포인트 충전'),
      ),
      gap,
      OutlinedButton.icon(
        onPressed: () => edit(c),
        icon: const Icon(Icons.manage_accounts),
        label: const Text('프로필 수정'),
      ),
      gap,
      TextButton.icon(
        onPressed: () async {
          try {
            await api.post('/auth/logout');
          } finally {
            await api.clearSession();
            logout();
          }
        },
        icon: const Icon(Icons.logout),
        label: const Text('로그아웃'),
      ),
    ],
  );
  Future<void> charge(BuildContext c) async {
    final a = TextEditingController(text: '10000');
    if (await form(c, '포인트 충전', [
      TextField(
        controller: a,
        keyboardType: TextInputType.number,
        decoration: const InputDecoration(labelText: '충전 금액'),
      ),
    ])) {
      try {
        changed(
          Map<String, dynamic>.from(
            await api.post('/me/points', {'amount': int.tryParse(a.text) ?? 0}),
          ),
        );
      } catch (e) {
        if (c.mounted) msg(c, e.toString());
      }
    }
  }

  Future<void> edit(BuildContext c) async {
    final n = TextEditingController(text: user['nickname'].toString()),
        cur = TextEditingController(),
        p = TextEditingController(),
        pc = TextEditingController();
    if (await form(c, '프로필 수정', [
      TextField(
        controller: n,
        decoration: const InputDecoration(labelText: '닉네임'),
      ),
      gap,
      TextField(
        controller: cur,
        obscureText: true,
        decoration: const InputDecoration(labelText: '현재 비밀번호'),
      ),
      gap,
      TextField(
        controller: p,
        obscureText: true,
        decoration: const InputDecoration(labelText: '새 비밀번호 (선택)'),
      ),
      gap,
      TextField(
        controller: pc,
        obscureText: true,
        decoration: const InputDecoration(labelText: '새 비밀번호 확인'),
      ),
    ])) {
      try {
        changed(
          Map<String, dynamic>.from(
            await api.patch('/me', {
              'nickname': n.text.trim(),
              'currentPassword': cur.text,
              'newPassword': p.text,
              'newPasswordConfirm': pc.text,
            }),
          ),
        );
      } catch (e) {
        if (c.mounted) msg(c, e.toString());
      }
    }
  }
}

class TodoCard extends StatelessWidget {
  const TodoCard(this.x, {super.key});
  final Map<String, dynamic> x;
  @override
  Widget build(BuildContext c) {
    final s = x['status'], done = s == 'FINISH', expired = s == 'EXPIRED';
    return Card(
      child: ListTile(
        contentPadding: const EdgeInsets.all(14),
        leading: RIcon(
          done
              ? Icons.check
              : expired
              ? Icons.priority_high
              : Icons.radio_button_unchecked,
          color: done
              ? C.green
              : expired
              ? C.red
              : C.primary,
        ),
        title: Text(
          x['work'].toString(),
          style: TextStyle(
            fontWeight: FontWeight.w800,
            decoration: done ? TextDecoration.lineThrough : null,
          ),
        ),
        subtitle: Text('${x['ownerNickname']} · ${date(x['deadline'])}'),
        trailing: Text(
          '${money(x['fineAmount'])}원',
          style: const TextStyle(color: C.red, fontWeight: FontWeight.w900),
        ),
      ),
    );
  }
}

class Section extends StatelessWidget {
  const Section(this.text, {super.key});
  final String text;
  @override
  Widget build(BuildContext c) => Padding(
    padding: const EdgeInsets.only(bottom: 13),
    child: Text(
      text,
      style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w900),
    ),
  );
}

class Metric extends StatelessWidget {
  const Metric(this.label, this.value, this.color, {super.key});
  final String label, value;
  final Color color;
  @override
  Widget build(BuildContext c) => Card(
    child: Padding(
      padding: const EdgeInsets.all(18),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: const TextStyle(color: C.muted, fontSize: 12)),
          Text(
            value,
            style: TextStyle(
              color: color,
              fontSize: 20,
              fontWeight: FontWeight.w900,
            ),
          ),
        ],
      ),
    ),
  );
}

class Small extends StatelessWidget {
  const Small(this.label, this.value, {super.key});
  final String label, value;
  @override
  Widget build(BuildContext c) => Column(
    children: [
      Text(
        value,
        style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w900),
      ),
      Text(label, style: const TextStyle(color: C.muted)),
    ],
  );
}

class Empty extends StatelessWidget {
  const Empty(this.icon, this.title, this.message, {super.key});
  final IconData icon;
  final String title, message;
  @override
  Widget build(BuildContext c) => Container(
    margin: const EdgeInsets.only(bottom: 12),
    padding: const EdgeInsets.all(28),
    decoration: BoxDecoration(
      color: Colors.white,
      borderRadius: BorderRadius.circular(22),
    ),
    child: Column(
      children: [
        Icon(icon, size: 42, color: C.primary),
        gap,
        Text(title, style: const TextStyle(fontWeight: FontWeight.w900)),
        Text(
          message,
          textAlign: TextAlign.center,
          style: const TextStyle(color: C.muted, fontSize: 12),
        ),
      ],
    ),
  );
}

class RIcon extends StatelessWidget {
  const RIcon(this.icon, {super.key, this.color = C.primary});
  final IconData icon;
  final Color color;
  @override
  Widget build(BuildContext c) => CircleAvatar(
    backgroundColor: C.soft,
    child: Icon(icon, color: color),
  );
}

const gap = SizedBox(height: 10),
    pad = EdgeInsets.fromLTRB(20, 12, 20, 100),
    title = TextStyle(fontSize: 23, fontWeight: FontWeight.w900);
Future<bool> form(BuildContext c, String title, List<Widget> children) async =>
    await showDialog<bool>(
      context: c,
      builder: (c) => AlertDialog(
        title: Text(title),
        content: SingleChildScrollView(
          child: Column(mainAxisSize: MainAxisSize.min, children: children),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(c, false),
            child: const Text('취소'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(c, true),
            child: const Text('확인'),
          ),
        ],
      ),
    ) ??
    false;
Future<bool> confirm(BuildContext c, String text) async =>
    await showDialog<bool>(
      context: c,
      builder: (c) => AlertDialog(
        title: const Text('확인'),
        content: Text(text),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(c, false),
            child: const Text('취소'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(c, true),
            child: const Text('확인'),
          ),
        ],
      ),
    ) ??
    false;
void msg(BuildContext c, String text) => ScaffoldMessenger.of(c).showSnackBar(
  SnackBar(content: Text(text), behavior: SnackBarBehavior.floating),
);
String money(dynamic v) {
  final n = int.tryParse('${v ?? 0}') ?? 0;
  return n.toString().replaceAllMapped(
    RegExp(r'\B(?=(\d{3})+(?!\d))'),
    (m) => ',',
  );
}

String date(dynamic v) {
  final d = DateTime.tryParse('${v ?? ''}');
  if (d == null) return '마감 없음';
  String t(int n) => n.toString().padLeft(2, '0');
  return '${d.month}/${d.day} ${t(d.hour)}:${t(d.minute)}';
}
