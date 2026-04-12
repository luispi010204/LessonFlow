package zhaw.ch.lessonflow.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import zhaw.ch.lessonflow.model.Lesson;

public interface LessonRepository extends MongoRepository<Lesson, String> {

    List<Lesson> findByCourseId(String courseId);

    List<Lesson> findByCourseIdOrderByLessonNumberAsc(String courseId);

    Optional<Lesson> findByCourseIdAndLessonNumber(String courseId, int lessonNumber);
}