-- Load-test seed data for MySQL 8.x
-- Creates 100 test users and 5,000 posts.
-- Safe to re-run: it only removes posts in the load-test id range first.

DELIMITER //

DROP PROCEDURE IF EXISTS seed_loadtest_posts_5000//

CREATE PROCEDURE seed_loadtest_posts_5000()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE selected_board_id BIGINT;
  DECLARE selected_user_id BIGINT;

  INSERT INTO boards (code, name, description, is_active, created_at, updated_at) VALUES
    ('FREE', '자유게시판', '자유롭게 이야기하는 게시판', true, NOW(), NOW()),
    ('SECRET', '비밀게시판', '비밀 글을 작성하는 게시판', true, NOW(), NOW()),
    ('GRADUATE', '졸업생게시판', '졸업생을 위한 게시판', true, NOW(), NOW()),
    ('FRESHMAN', '새내기게시판', '새내기를 위한 게시판', true, NOW(), NOW()),
    ('MARKET', '장터게시판', '중고책 등 거래 게시판', true, NOW(), NOW())
  ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    is_active = VALUES(is_active),
    updated_at = NOW();

  SET i = 1;
  WHILE i <= 100 DO
    INSERT INTO users (id, email, password, nickname, created_at, updated_at)
    VALUES (
      900000 + i,
      CONCAT('loadtest', i, '@example.com'),
      'Passw0rd!',
      CONCAT('loadtest', i),
      NOW(),
      NOW()
    )
    ON DUPLICATE KEY UPDATE
      password = VALUES(password),
      nickname = VALUES(nickname),
      updated_at = NOW();

    SET i = i + 1;
  END WHILE;

  DELETE FROM posts WHERE id BETWEEN 900001 AND 905000;

  SET i = 1;
  WHILE i <= 5000 DO
    SELECT id
      INTO selected_board_id
      FROM boards
     WHERE code = ELT(((i - 1) MOD 5) + 1, 'FREE', 'SECRET', 'GRADUATE', 'FRESHMAN', 'MARKET')
     LIMIT 1;

    SET selected_user_id = 900001 + ((i - 1) MOD 100);

    INSERT INTO posts (
      id,
      board_id,
      user_id,
      title,
      description,
      view_count,
      like_count,
      comment_count,
      created_at,
      updated_at
    )
    VALUES (
      900000 + i,
      selected_board_id,
      selected_user_id,
      CONCAT('부하 테스트 게시글 ', i),
      CONCAT(
        'nGrinder 부하 테스트를 위한 게시글 데이터입니다. ',
        'postNo=', i,
        ', boardSlot=', ((i - 1) MOD 5) + 1,
        ', userSlot=', ((i - 1) MOD 100) + 1,
        '. 목록 조회, 커서 페이징, 상세 조회 테스트에 사용합니다.'
      ),
      (i * 17) MOD 100000,
      (i * 7) MOD 300,
      (i * 3) MOD 80,
      TIMESTAMPADD(SECOND, i, '2026-07-01 00:00:00'),
      TIMESTAMPADD(SECOND, i, '2026-07-01 00:00:00')
    );

    SET i = i + 1;
  END WHILE;
END//

CALL seed_loadtest_posts_5000()//

DROP PROCEDURE IF EXISTS seed_loadtest_posts_5000//

DELIMITER ;

SELECT COUNT(*) AS loadtest_post_count
  FROM posts
 WHERE id BETWEEN 900001 AND 905000;
