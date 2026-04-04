package zhaw.ch.lessonflow.repositories;

import zhaw.ch.lessonflow.model.LessonProgress;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends MongoRepository<LessonProgress, String> {
    List<LessonProgress> findByEnrollmentId(String enrollmentId);
    List<LessonProgress> findByLessonId(String lessonId);
    Optional<LessonProgress> findByEnrollmentIdAndLessonId(String enrollmentId, String lessonId);
}