-- 1. User 데이터
INSERT INTO user_tb (email, password, nickname, created_at,provider) VALUES
                                                                ('minsoo@example.com', '$2a$10$kDILjaMHE4XobrII2B92NOuxzSaJF8eWw1Yycj3UyAM1ExWTr.PL6', '박민수', NOW(),'LOCAL'),
                                                                ('ghildong@example.com', '$2a$10$x3OgrGvgwOOkOXZ2X.CsVOHkMp3H0NZc3IyetfHgX39QPU0.u7Vk2', '홍길동', NOW(),'LOCAL'),
                                                                ('sunny@example.com', '$2a$10$VzkNM2s0YYQtd/Mcg0h95enWSIIkgtwDAWje.OdYEUqjLHgpSAwq6', '김태양', NOW(),'LOCAL');
-- 2. Group 데이터
INSERT INTO group_tb (room_name, description, invite_code, created_at) VALUES
                                                                           ('알고리즘 1일 1커밋 🌳', '매일 자정까지 백준 1문제 풀기', 'INVITE-AAA', NOW()),
                                                                           ('새벽 기상 챌린지 ⏰', '오전 6시 30분 전까지 기상 사진 업로드', 'INVITE-BBB', NOW());

-- 3. GroupMember 데이터
INSERT INTO group_member_tb (user_id, group_id, role) VALUES
                                                          (1, 1, 'ADMIN'),
                                                          (2, 1, 'MEMBER'),
                                                          (1, 2, 'MEMBER');

-- 4. Fine 데이터
INSERT INTO fine_tb (amount, status, created_at) VALUES
    (5000, 'UNPAID', NOW()); -- id=1

---------------------------------------------------
-- 5. Todo 데이터 (status 기반으로 변경)
---------------------------------------------------

INSERT INTO todo_tb (work, status, group_member_id, fine_id, deadline, created_at) VALUES

-- 알고리즘방 (group_id=1)
('백준 1234번 문제 풀이', 'FINISH', 1, NULL, '2026-05-14 23:59:59', NOW()),
('백준 5678번 문제 풀이', 'READY', 2, NULL, '2026-05-14 23:59:59', NOW()),
('주간 코드리뷰 자료 준비', 'READY', 1, NULL, '2026-05-17 18:00:00', NOW()),

-- 새벽기상방 (group_id=2)
('6:30 기상 인증샷', 'EXPIRED', 3, 1, '2026-05-13 06:30:00', NOW());