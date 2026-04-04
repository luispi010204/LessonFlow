package zhaw.ch.lessonflow.repository;

import zhaw.ch.lessonflow.model.Lesson;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LessonRepository extends MongoRepository<Lesson, String> {
    List<Lesson> findByCourseId(String courseId);
}
