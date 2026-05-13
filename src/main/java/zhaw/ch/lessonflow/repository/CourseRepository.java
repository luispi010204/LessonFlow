package zhaw.ch.lessonflow.repository;

import zhaw.ch.lessonflow.model.Course;
import zhaw.ch.lessonflow.model.CourseStatus;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CourseRepository extends MongoRepository<Course, String> {
    List<Course> findByTutorUserId(String tutorUserId);

    List<Course> findByStatus(CourseStatus status);
}