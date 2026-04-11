package lt.linas_puplauskas.repository;

import lt.linas_puplauskas.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
