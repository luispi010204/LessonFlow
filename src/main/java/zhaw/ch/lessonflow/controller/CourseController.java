package zhaw.ch.lessonflow.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import zhaw.ch.lessonflow.model.Course;
import zhaw.ch.lessonflow.model.CourseCreateDTO;
import zhaw.ch.lessonflow.repository.CourseRepository;

@RestController
@RequestMapping("/api")
public class CourseController {

    @Autowired
    CourseRepository courseRepository;

    @PostMapping("/course")
    public ResponseEntity<Course> createCourse(@RequestBody CourseCreateDTO fDTO) {

        Course fDAO = new Course(
                fDTO.getTutorUserId(),
                fDTO.getTitle(),
                fDTO.getDescription(),
                fDTO.getStatus());

        Course f = courseRepository.save(fDAO);
        return new ResponseEntity<>(f, HttpStatus.CREATED);
    }

    @GetMapping("/course")
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @GetMapping("/course/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable String id) {
        Optional<Course> course = courseRepository.findById(id);

        if (course.isPresent()) {
            return new ResponseEntity<>(course.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}