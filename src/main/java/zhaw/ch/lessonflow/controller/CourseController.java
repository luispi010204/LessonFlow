package zhaw.ch.lessonflow.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import zhaw.ch.lessonflow.model.Course;
import zhaw.ch.lessonflow.model.CourseCreateDTO;
import zhaw.ch.lessonflow.model.CourseStatus;
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
                CourseStatus.DRAFT
        );

        Course savedCourse = courseRepository.save(fDAO);
        return new ResponseEntity<>(savedCourse, HttpStatus.CREATED);
    }

    @GetMapping("/course")
    public List<Course> getAllCourses() {
        return courseRepository.findByStatus(CourseStatus.PUBLISHED);
    }

    @GetMapping("/course/me")
    public ResponseEntity<List<Course>> getMyCourses() {

        if (!userService.userHasRole("tutor")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        String currentUserId = userService.getCurrentUserId();

        List<Course> courses = courseRepository.findByTutorUserId(currentUserId);

        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @GetMapping("/course/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable String id) {
        Optional<Course> courseData = courseRepository.findById(id);

        if (courseData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Course course = courseData.get();

        if (course.getStatus() == CourseStatus.PUBLISHED) {
            return new ResponseEntity<>(course, HttpStatus.OK);
        }

        if (userService.userHasRole("tutor")
                && course.getTutorUserId().equals(userService.getCurrentUserId())) {
            return new ResponseEntity<>(course, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @PutMapping("/course/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable String id,
            @RequestBody CourseCreateDTO fDTO) {

        if (!userService.userHasRole("tutor")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<Course> courseData = courseRepository.findById(id);

        if (courseData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Course course = courseData.get();

        if (!course.getTutorUserId().equals(userService.getCurrentUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (fDTO.getTitle() == null || fDTO.getTitle().isBlank()
                || fDTO.getDescription() == null || fDTO.getDescription().isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        course.updateDetails(
                fDTO.getTitle(),
                fDTO.getDescription()
        );

        Course savedCourse = courseRepository.save(course);
        return new ResponseEntity<>(savedCourse, HttpStatus.OK);
    }

    @PostMapping("/course/{id}/publish")
    public ResponseEntity<Course> publishCourse(@PathVariable String id) {

        if (!userService.userHasRole("tutor")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<Course> courseData = courseRepository.findById(id);

        if (courseData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Course course = courseData.get();

        if (!course.getTutorUserId().equals(userService.getCurrentUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        course.publish();

        Course savedCourse = courseRepository.save(course);
        return new ResponseEntity<>(savedCourse, HttpStatus.OK);
    }
}