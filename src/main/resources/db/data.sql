-- 1. User 데이터
INSERT INTO user_tb (email, password, nickname, created_at) VALUES
                                                                ('minsoo@example.com', '1234', '박민수', NOW()),
                                                                ('ghildong@example.com', '1234', '홍길동', NOW()),
                                                                ('sunny@example.com', '1234', '김태양', NOW());

-- 2. Group 데이터
INSERT INTO group_tb (room_name, description, invite_code, created_at) VALUES
                                                                           ('알고리즘 1일 1커밋 🌳', '매일 자정까지 백준 1문제 풀기', 'INVITE-AAA', NOW()),
                                                                           ('새벽 기상 챌린지 ⏰', '오전 6시 30분 전까지 기상 사진 업로드', 'INVITE-BBB', NOW());

-- 3. GroupMember 데이터 (가입 관계)
INSERT INTO group_member_tb (user_id, group_id, role, achievement_rate) VALUES
                                                                            (1, 1, 'ADMIN', 100.0), -- 민수: 알고리즘 방장
                                                                            (2, 1, 'MEMBER', 95.0),  -- 길동: 알고리즘 멤버
                                                                            (1, 2, 'MEMBER', 82.5);  -- 민수: 새벽기상 멤버

-- 4. Fine 데이터 (미이행 건에 대한 벌금 미리 생성)
INSERT INTO fine_tb (amount, status, created_at) VALUES
    (5000, 'UNPAID', NOW()); -- id=1

-- 5. Todo 데이터 (핵심!)
-- 시나리오: 알고리즘방은 진행 중, 새벽기상방은 민수가 실패해서 벌금이 매겨짐
INSERT INTO todo_tb (work, is_done, group_id, user_id, fine_id,created_at) VALUES
-- 알고리즘방 (group_id=1)
('백준 1234번 문제 풀이', true, 1, 1, NULL,NOW()),     -- 민수(1) 완료
('백준 5678번 문제 풀이', false, 1, 2, NULL,NOW()),    -- 길동(2) 미완료
('주간 코드리뷰 자료 준비', false, 1, 1, NULL,NOW()),  -- 민수(1) 미완료

-- 새벽기상방 (group_id=2)
('6:30 기상 인증샷', false, 2, 1, 1,NOW());           -- 민수(1) 실패 -> 벌금(1) 연결