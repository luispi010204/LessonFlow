package zhaw.ch.lessonflow.repositories;

import zhaw.ch.lessonflow.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CourseRepository extends MongoRepository<Course, String> {
    List<Course> findByTutorUserId(String tutorUserId);
}