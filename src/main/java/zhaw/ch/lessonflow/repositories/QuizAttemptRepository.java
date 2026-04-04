package zhaw.ch.lessonflow.repositories;

import zhaw.ch.lessonflow.model.QuizAttempt;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuizAttemptRepository extends MongoRepository<QuizAttempt, String> {
    List<QuizAttempt> findByQuizId(String quizId);
    List<QuizAttempt> findByEnrollmentId(String enrollmentId);
    List<QuizAttempt> findByLessonId(String lessonId);
}