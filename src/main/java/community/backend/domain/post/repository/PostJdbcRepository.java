package community.backend.domain.post.repository;

import community.backend.domain.post.dto.response.PostDetailResponse;
import community.backend.domain.post.dto.response.PostListDetailResponse;
import community.backend.domain.post.entity.Post;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostJdbcRepository implements PostRepository {

  private final JdbcTemplate jdbcTemplate;
  private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @Override
  public long save(Post post) {
    String sql = """
      INSERT INTO posts(user_id,title,description,post_image_url,view_count,like_count,comment_count,created_at,updated_at)
      VALUES (?,?,?,?,0,0,0,NOW(),NOW())
      """;
    KeyHolder kh = new GeneratedKeyHolder();
    jdbcTemplate.update(conn -> {
      PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, post.getUserId());
      ps.setString(2, post.getTitle());
      ps.setString(3, post.getDescription());
      ps.setString(4, post.getPostImageUrl());
      return ps;
    }, kh);
    return Objects.requireNonNull(kh.getKey()).longValue();
  }

  @Override
  public List<PostListDetailResponse> findList(Long lastPostId, int size) {
    String sql = """
      SELECT p.id, p.title, u.nickname, u.profile_image_url, p.updated_at, p.like_count, p.comment_count, p.view_count
      FROM posts p JOIN users u ON p.user_id=u.id
      WHERE p.deleted_at IS NULL AND p.id < ?
      ORDER BY p.id DESC LIMIT ?
      """;
    return jdbcTemplate.query(sql, (rs, n) -> {
      long likes = rs.getLong("like_count");
      long views = rs.getLong("view_count");
      return new PostListDetailResponse(
          rs.getLong("id"),
          rs.getString("title"),
          rs.getString("nickname"),
          rs.getString("profile_image_url"),
          rs.getTimestamp("updated_at").toLocalDateTime().format(F),
          likes,
          displayCount(likes),
          rs.getLong("comment_count"),
          views,
          displayCount(views)
      );
    }, lastPostId, size);
  }

  @Override
  public Optional<PostDetailResponse> findDetail(Long postId) {
    String sql = """
      SELECT p.id, p.title, p.description, p.post_image_url, p.updated_at, p.like_count, p.view_count,
             u.nickname, u.profile_image_url
      FROM posts p JOIN users u ON p.user_id=u.id
      WHERE p.id=? AND p.deleted_at IS NULL
      """;
    List<PostDetailResponse> rows = jdbcTemplate.query(sql, (rs, n) ->
        new PostDetailResponse(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("post_image_url"),
            new PostDetailResponse.Author(rs.getString("nickname"), rs.getString("profile_image_url")),
            rs.getTimestamp("updated_at").toLocalDateTime().format(F),
            rs.getLong("like_count"),
            rs.getLong("view_count")
        ), postId);
    return rows.stream().findFirst();
  }

  @Override
  public Optional<Post> findById(Long postId) {
    String sql = "SELECT id,user_id,title,description,post_image_url,view_count,like_count,comment_count FROM posts WHERE id=? AND deleted_at IS NULL";
    List<Post> rows = jdbcTemplate.query(sql, (rs, n) -> Post.builder()
        .id(rs.getLong("id"))
        .userId(rs.getLong("user_id"))
        .title(rs.getString("title"))
        .description(rs.getString("description"))
        .postImageUrl(rs.getString("post_image_url"))
        .viewCount(rs.getLong("view_count"))
        .likeCount(rs.getLong("like_count"))
        .commentCount(rs.getLong("comment_count"))
        .build(), postId);
    return rows.stream().findFirst();
  }

  @Override
  public int increaseViewCount(Long postId) {
    return jdbcTemplate.update("UPDATE posts SET view_count=view_count+1 WHERE id=? AND deleted_at IS NULL", postId);
  }

  @Override
  public int updatePost(Long postId, String title, String description, String postImageUrl) {
    return jdbcTemplate.update(
        "UPDATE posts SET title=?, description=?, post_image_url=?, updated_at=NOW() WHERE id=? AND deleted_at IS NULL",
        title, description, postImageUrl, postId
    );
  }

  @Override
  public int softDelete(Long postId) {
    return jdbcTemplate.update("UPDATE posts SET deleted_at=NOW(), updated_at=NOW() WHERE id=? AND deleted_at IS NULL", postId);
  }

  private static String displayCount(long value) {
    if (value >= 1_000_000) return (value / 1_000_000) + "m";
    if (value >= 1_000) return (value / 1_000) + "k";
    return String.valueOf(value);
  }
}
