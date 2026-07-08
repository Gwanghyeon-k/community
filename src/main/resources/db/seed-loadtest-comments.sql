-- Load-test comments for posts 900001 ~ 905000.
-- Creates 3 comments per post, 15,000 comments total.
-- Safe to re-run: it only removes comments in the load-test id range first.

DELIMITER //

DROP PROCEDURE IF EXISTS seed_loadtest_comments//

CREATE PROCEDURE seed_loadtest_comments()
BEGIN
  DECLARE post_no INT DEFAULT 1;
  DECLARE comment_no INT DEFAULT 1;
  DECLARE comment_id BIGINT;
  DECLARE target_post_id BIGINT;
  DECLARE target_user_id BIGINT;

  DELETE FROM comments WHERE id BETWEEN 9100001 AND 9115000;

  SET post_no = 1;
  WHILE post_no <= 5000 DO
    SET target_post_id = 900000 + post_no;
    SET comment_no = 1;

    WHILE comment_no <= 3 DO
      SET comment_id = 9100000 + ((post_no - 1) * 3) + comment_no;
      SET target_user_id = 900001 + ((post_no + comment_no - 2) MOD 100);

      INSERT INTO comments (
        id,
        post_id,
        user_id,
        content,
        created_at,
        updated_at
      )
      VALUES (
        comment_id,
        target_post_id,
        target_user_id,
        CONCAT(
          '부하 테스트 댓글 ',
          comment_no,
          ' for post ',
          target_post_id
        ),
        TIMESTAMPADD(SECOND, comment_id MOD 86400, '2026-07-02 00:00:00'),
        TIMESTAMPADD(SECOND, comment_id MOD 86400, '2026-07-02 00:00:00')
      );

      SET comment_no = comment_no + 1;
    END WHILE;

    SET post_no = post_no + 1;
  END WHILE;

  UPDATE posts
     SET comment_count = 3,
         updated_at = updated_at
   WHERE id BETWEEN 900001 AND 905000;
END//

CALL seed_loadtest_comments()//

DROP PROCEDURE IF EXISTS seed_loadtest_comments//

DELIMITER ;

SELECT COUNT(*) AS loadtest_comment_count
  FROM comments
 WHERE id BETWEEN 9100001 AND 9115000;
