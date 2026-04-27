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
import zhaw.ch.lessonflow.services.UserService;

@RestController
@RequestMapping("/api")
public class CourseController {

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UserService userService;

    @PostMapping("/course")
public ResponseEntity<Course> createCourse(@RequestBody CourseCreateDTO fDTO) {

    if (!userService.userHasRole("tutor")) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    Course fDAO = new Course(
            userService.getCurrentUserId(),
            fDTO.getTitle(),
            fDTO.getDescription(),
            fDTO.getStatus()
    );

    Course savedCourse = courseRepository.save(fDAO);
    return new ResponseEntity<>(savedCourse, HttpStatus.CREATED);
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