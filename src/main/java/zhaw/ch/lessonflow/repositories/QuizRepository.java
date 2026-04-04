package zhaw.ch.lessonflow.repositories;

import zhaw.ch.lessonflow.model.Quiz;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface QuizRepository extends MongoRepository<Quiz, String> {
    Optional<Quiz> findByLessonId(String lessonId);
}