package zhaw.ch.lessonflow.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zhaw.ch.lessonflow.repository.CourseRepository;

@Service
public class CourseService {

    @Autowired
    CourseRepository courseRepository;

    public boolean courseExists(String courseId) {
        return courseRepository.existsById(courseId);
    }
}