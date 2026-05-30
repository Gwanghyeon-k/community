CREATE TABLE `users` (
                         `id` BigInt NOT NULL AUTO_INCREMENT COMMENT '유저 id',
                         `email` VARCHAR(100) NOT NULL COMMENT '이메일',
                         `password` VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
                         `nickname` VARCHAR(20) NOT NULL COMMENT '사용자 닉네임',
                         `profile_image_url` VARCHAR(512) NOT NULL COMMENT '프로필 이미지 url',
                         `created_at` DATETIME NOT NULL COMMENT '생성 일자',
                         `updated_at` DATETIME NOT NULL COMMENT '수정 일자',
                         `deleted_at` DATETIME NULL COMMENT '삭제 일자',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `ux_users_email` (`email`),
                         UNIQUE KEY `ux_users_nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `posts` (
                         `id` BigInt NOT NULL AUTO_INCREMENT COMMENT '게시글 id',
                         `user_id` BIGINT NOT NULL COMMENT '유저 id',
                         `title` VARCHAR(100) NOT NULL COMMENT '게시글 제목',
                         `description` TEXT NOT NULL COMMENT '게시글 내용',
                         `post_image_url` VARCHAR(512) NULL COMMENT '게시글 이미지 url',
                         `view_count` BIGINT NOT NULL DEFAULT 0 COMMENT '총 조회 수',
                         `like_count` BIGINT NOT NULL DEFAULT 0 COMMENT '총 좋아요 수',
                         `comment_count` BIGINT NOT NULL DEFAULT 0 COMMENT '총 댓글 수',
                         `created_at` DATETIME NOT NULL COMMENT '생성 일자',
                         `updated_at` DATETIME NOT NULL COMMENT '수정 일자',
                         `deleted_at` DATETIME NULL COMMENT '삭제 일자',
                         PRIMARY KEY (`id`),
                         CONSTRAINT `fk_posts_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `comments` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '댓글 id',
                            `post_id` BIGINT NOT NULL COMMENT '게시글 id',
                            `user_id` BIGINT NOT NULL COMMENT '유저 id',
                            `content` TEXT NOT NULL COMMENT '댓글 내용',
                            `created_at` DATETIME NOT NULL COMMENT '생성 일자',
                            `updated_at` DATETIME NOT NULL COMMENT '수정 일자',
                            `deleted_at` DATETIME NULL COMMENT '삭제 일자',
                            PRIMARY KEY (`id`),
                            CONSTRAINT `fk_comments_post_id` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`),
                            CONSTRAINT `fk_comments_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `post_likes` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '좋아요 id',
                              `post_id` BIGINT NOT NULL COMMENT '게시글 id',
                              `user_id` BIGINT NOT NULL COMMENT '유저 id',
                              `created_at` DATETIME NOT NULL COMMENT '좋아요 누른 시간',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `ux_post_likes_post_user` (`post_id`, `user_id`),
                              CONSTRAINT `fk_post_likes_post_id` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`),
                              CONSTRAINT `fk_post_likes_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);

CREATE TABLE `refresh_tokens` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '리프레시 토큰 id',
                                  `user_id` BIGINT NOT NULL COMMENT '유저 id',
                                  `token` VARCHAR(1024) NOT NULL COMMENT '리프레시 토큰 문자열',
                                  `expires_at` DATETIME NOT NULL COMMENT '만료 시간',
                                  `created_at` DATETIME NOT NULL COMMENT '생성 일자',
                                  `updated_at` DATETIME NOT NULL COMMENT '수정 일자',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `ux_refresh_tokens_user_id` (`user_id`),
                                  UNIQUE KEY `ux_refresh_tokens_token` (`token`),
                                  CONSTRAINT `fk_refresh_tokens_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;