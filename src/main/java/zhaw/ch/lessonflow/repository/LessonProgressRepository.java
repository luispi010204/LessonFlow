package zhaw.ch.lessonflow.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import zhaw.ch.lessonflow.model.LessonProgress;

public interface LessonProgressRepository extends MongoRepository<LessonProgress, String> {

    List<LessonProgress> findByEnrollmentId(String enrollmentId);

    List<LessonProgress> findByLessonId(String lessonId);

    Optional<LessonProgress> findByEnrollmentIdAndLessonId(String enrollmentId, String lessonId);
}