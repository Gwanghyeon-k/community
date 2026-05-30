package community.backend.domain.post.repository;

import community.backend.domain.post.dto.response.PostDetailResponse;
import community.backend.domain.post.dto.response.PostListDetailResponse;
import community.backend.domain.post.entity.Post;
import static community.backend.domain.post.repository.PostSql.FIND_DETAIL;
import static community.backend.domain.post.repository.PostSql.FIND_LIST;
import static community.backend.domain.post.repository.PostSql.FIND_POST_BY_ID;
import static community.backend.domain.post.repository.PostSql.INCREASE_VIEW_COUNT;
import static community.backend.domain.post.repository.PostSql.SAVE_POST;
import static community.backend.domain.post.repository.PostSql.SOFT_DELETE_POST;
import static community.backend.domain.post.repository.PostSql.UPDATE_POST;
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
    KeyHolder kh = new GeneratedKeyHolder();
    jdbcTemplate.update(conn -> {
      PreparedStatement ps = conn.prepareStatement(SAVE_POST, Statement.RETURN_GENERATED_KEYS);
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
    return jdbcTemplate.query(FIND_LIST, (rs, n) -> {
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
    List<PostDetailResponse> rows = jdbcTemplate.query(FIND_DETAIL, (rs, n) ->
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
    List<Post> rows = jdbcTemplate.query(FIND_POST_BY_ID, (rs, n) -> Post.builder()
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
    return jdbcTemplate.update(INCREASE_VIEW_COUNT, postId);
  }

  @Override
  public int updatePost(Long postId, String title, String description, String postImageUrl) {
    return jdbcTemplate.update(
        UPDATE_POST,
        title, description, postImageUrl, postId
    );
  }

  @Override
  public int softDelete(Long postId) {
    return jdbcTemplate.update(SOFT_DELETE_POST, postId);
  }

  private static String displayCount(long value) {
    if (value >= 1_000_000) return (value / 1_000_000) + "m";
    if (value >= 1_000) return (value / 1_000) + "k";
    return String.valueOf(value);
  }
}
