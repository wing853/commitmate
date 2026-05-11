-- 비밀번호는 현재 평문 저장 방식이므로 간단하게 설정했습니다.
INSERT INTO user (email, password, nickname, created_at) VALUES
                                                             ('minsoo@example.com', '1234', '박민수', NOW()),
                                                             ('ghildong@example.com', '1234', '홍길동', NOW()),
                                                             ('sunny@example.com', '1234', '김태양', NOW()),
                                                             ('dev_leo@example.com', '1234', '레오', NOW()),
                                                             ('commit_king@example.com', '1234', '커밋왕', NOW());