package community.backend.domain.post.service;

import community.backend.domain.post.repository.PostQuerydslRepository;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ViewCountBufferService {

  private final PostQuerydslRepository postQuerydslRepository;
  private final ConcurrentHashMap<Long, AtomicLong> buffer = new ConcurrentHashMap<>();

  public void increment(Long postId) {
    buffer.computeIfAbsent(postId, id -> new AtomicLong()).incrementAndGet();
  }

  // 5초 마다 조회수 반영
  @Scheduled(fixedDelay = 5000)
  public void flush() {
    for (Map.Entry<Long, AtomicLong> entry : buffer.entrySet()) {
      long delta = entry.getValue().getAndSet(0L);
      if (delta <= 0) continue;

      long updated = postQuerydslRepository.increaseViewCount(entry.getKey(), delta);
      if (updated == 0L) {
        // 게시글이 없으면 버퍼 제거
        buffer.remove(entry.getKey());
      }
    }
  }

  @PreDestroy
  public void flushBeforeShutdown() {
    flush();
  }
}