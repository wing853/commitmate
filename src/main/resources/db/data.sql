-- 1. User 데이터 (제공해주신 내용)
INSERT INTO user_tb (email, password, nickname, created_at) VALUES
                                                             ('minsoo@example.com', '1234', '박민수', NOW()),
                                                             ('ghildong@example.com', '1234', '홍길동', NOW()),
                                                             ('sunny@example.com', '1234', '김태양', NOW()),
                                                             ('dev_leo@example.com', '1234', '레오', NOW()),
                                                             ('commit_king@example.com', '1234', '커밋왕', NOW());

-- 2. Group 데이터 (워크스페이스)
-- ※ 주의: 테이블명이 'group'인 경우 SQL 예약어이므로 백틱(`)을 사용해야 합니다.
INSERT INTO group_tb (room_name, description, invite_code, created_at) VALUES
                                                                          ('알고리즘 1일 1커밋 🌳', '매일 자정까지 백준 1문제 풀고 인증하기', 'INVITE-AAA', NOW()),
                                                                          ('새벽 기상 챌린지 ⏰', '오전 6시 30분 전까지 기상 사진 업로드', 'INVITE-BBB', NOW()),
                                                                          ('스프링 부트 스터디 🍃', '매주 기술 블로그 포스팅 및 코드 리뷰', 'INVITE-CCC', NOW());

-- 3. GroupMember 데이터 (관계 및 달성률)
-- 박민수(1)는 알고리즘 방장, 새벽기상 멤버
INSERT INTO group_member_tb (user_id, group_id, role, achievement_rate) VALUES
                                                                         (1, 1, 'ADMIN', 100.0),
                                                                         (1, 2, 'MEMBER', 82.5);

-- 홍길동(2)은 알고리즘 멤버, 스프링 스터디 방장
INSERT INTO group_member_tb (user_id, group_id, role, achievement_rate) VALUES
                                                                         (2, 1, 'MEMBER', 95.0),
                                                                         (2, 3, 'ADMIN', 100.0);