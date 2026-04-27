package zhaw.ch.lessonflow.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zhaw.ch.lessonflow.model.Course;
import zhaw.ch.lessonflow.repository.CourseRepository;

@Service
public class CourseService {

    @Autowired
    CourseRepository courseRepository;

    public boolean courseExists(String courseId) {
        return courseRepository.existsById(courseId);
    }

    public Optional<Course> getCourseById(String courseId) {
        return courseRepository.findById(courseId);
    }

    public boolean courseBelongsToTutor(String courseId, String tutorUserId) {
        Optional<Course> courseData = courseRepository.findById(courseId);

        if (courseData.isEmpty()) {
            return false;
        }

        Course course = courseData.get();
        return course.getTutorUserId().equals(tutorUserId);
    }
}