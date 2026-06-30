CREATE TABLE `users` (
                         `id` BigInt NOT NULL AUTO_INCREMENT COMMENT '유저 id',
                         `email` VARCHAR(100) NOT NULL COMMENT '이메일',
                         `password` VARCHAR(100) NOT NULL COMMENT '비밀번호',
                         `nickname` VARCHAR(20) NOT NULL COMMENT '사용자 닉네임',
                         `created_at` DATETIME NOT NULL COMMENT '생성 일자',
                         `updated_at` DATETIME NOT NULL COMMENT '수정 일자',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `ux_users_email` (`email`),
                         UNIQUE KEY `ux_users_nickname` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `boards` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '게시판 id',
                          `code` VARCHAR(30) NOT NULL COMMENT '게시판 코드',
                          `name` VARCHAR(50) NOT NULL COMMENT '게시판 이름',
                          `description` VARCHAR(255) NULL COMMENT '게시판 설명',
                          `is_active` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
                          `created_at` DATETIME NOT NULL COMMENT '생성 일자',
                          `updated_at` DATETIME NOT NULL COMMENT '수정 일자',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `ux_boards_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `posts` (
                         `id` BigInt NOT NULL AUTO_INCREMENT COMMENT '게시글 id',
                         `board_id` BIGINT NOT NULL COMMENT '게시판 id',
                         `user_id` BIGINT NOT NULL COMMENT '유저 id',
                         `title` VARCHAR(100) NOT NULL COMMENT '게시글 제목',
                         `description` TEXT NOT NULL COMMENT '게시글 내용',
                         `view_count` BIGINT NOT NULL DEFAULT 0 COMMENT '총 조회 수',
                         `like_count` BIGINT NOT NULL DEFAULT 0 COMMENT '총 좋아요 수',
                         `comment_count` BIGINT NOT NULL DEFAULT 0 COMMENT '총 댓글 수',
                         `created_at` DATETIME NOT NULL COMMENT '생성 일자',
                         `updated_at` DATETIME NOT NULL COMMENT '수정 일자',
                         PRIMARY KEY (`id`),
                         CONSTRAINT `fk_posts_board_id` FOREIGN KEY (`board_id`) REFERENCES `boards` (`id`),
                         CONSTRAINT `fk_posts_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_profile_images` (
                             `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '유저 프로필 이미지 id',
                             `user_id` BIGINT NOT NULL COMMENT '유저 id',
                             `user_profile_image_url` VARCHAR(255) NOT NULL COMMENT '유저 프로필 이미지 url',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `ux_user_profile_images_user_id` (`user_id`),
                             CONSTRAINT `fk_user_profile_images_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `post_images` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '게시글 이미지 id',
                        `post_id` BIGINT NOT NULL COMMENT '게시글 id',
                        `post_image_url` VARCHAR(255) NOT NULL COMMENT '게시글 이미지 url',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `ux_post_images_post_id` (`post_id`),
                        CONSTRAINT `fk_post_images_post_id` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `comments` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '댓글 id',
                            `post_id` BIGINT NOT NULL COMMENT '게시글 id',
                            `user_id` BIGINT NOT NULL COMMENT '유저 id',
                            `content` TEXT NOT NULL COMMENT '댓글 내용',
                            `created_at` DATETIME NOT NULL COMMENT '생성 일자',
                            `updated_at` DATETIME NOT NULL COMMENT '수정 일자',
                            PRIMARY KEY (`id`),
                            CONSTRAINT `fk_comments_post_id` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
                            CONSTRAINT `fk_comments_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `post_likes` (
                              `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '좋아요 id',
                              `post_id` BIGINT NOT NULL COMMENT '게시글 id',
                              `user_id` BIGINT NOT NULL COMMENT '유저 id',
                              `created_at` DATETIME NOT NULL COMMENT '좋아요 누른 시간',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `ux_post_likes_post_user` (`post_id`, `user_id`),
                              CONSTRAINT `fk_post_likes_post_id` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
                              CONSTRAINT `fk_post_likes_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);

CREATE TABLE `auths` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '리프레시 토큰 id',
                         `user_id` BIGINT NOT NULL COMMENT '유저 id',
                         `token` VARCHAR(512) NOT NULL COMMENT '리프레시 토큰',
                         `expires_at` DATETIME NOT NULL COMMENT '만료 시간',
                         `created_at` DATETIME NOT NULL COMMENT '생성 일자',
                         `updated_at` DATETIME NOT NULL COMMENT '수정 일자',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `ux_refresh_tokens_user_id` (`user_id`),
                         UNIQUE KEY `ux_refresh_tokens_token` (`token`),
                         CONSTRAINT `fk_refresh_tokens_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
