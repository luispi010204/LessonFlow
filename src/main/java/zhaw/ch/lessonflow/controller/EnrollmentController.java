package zhaw.ch.lessonflow.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import zhaw.ch.lessonflow.model.Course;
import zhaw.ch.lessonflow.model.CourseStatus;
import zhaw.ch.lessonflow.model.Enrollment;
import zhaw.ch.lessonflow.model.EnrollmentCreateDTO;
import zhaw.ch.lessonflow.repository.CourseRepository;
import zhaw.ch.lessonflow.repository.EnrollmentRepository;
import zhaw.ch.lessonflow.services.CourseService;
import zhaw.ch.lessonflow.services.EnrollmentService;
import zhaw.ch.lessonflow.services.UserService;

@RestController
@RequestMapping("/api")
public class EnrollmentController {

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    CourseService courseService;

    @Autowired
    EnrollmentService enrollmentService;

    @Autowired
    UserService userService;

    @PostMapping("/enrollment")
    public ResponseEntity<Enrollment> createEnrollment(@RequestBody EnrollmentCreateDTO fDTO) {

        if (!userService.userHasRole("learner")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (fDTO.getCourseId() == null || fDTO.getCourseId().isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Course> courseData = courseRepository.findById(fDTO.getCourseId());

        if (courseData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Course course = courseData.get();

        if (course.getStatus() != CourseStatus.PUBLISHED) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        String currentUserId = userService.getCurrentUserId();

        if (enrollmentService.isAlreadyEnrolled(fDTO.getCourseId(), currentUserId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Enrollment fDAO = new Enrollment(
                fDTO.getCourseId(),
                currentUserId,
                "ENROLLED");

        Enrollment savedEnrollment = enrollmentService.createEnrollmentWithProgress(fDAO);
        return new ResponseEntity<>(savedEnrollment, HttpStatus.CREATED);
    }

    @GetMapping("/enrollment")
    public ResponseEntity<List<Enrollment>> getAllEnrollments() {

        if (!userService.userHasRole("tutor")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        String currentUserId = userService.getCurrentUserId();

        List<Course> tutorCourses = courseRepository.findByTutorUserId(currentUserId);
        List<Enrollment> enrollments = new ArrayList<>();

        for (Course course : tutorCourses) {
            enrollments.addAll(enrollmentRepository.findByCourseId(course.getId()));
        }

        return new ResponseEntity<>(enrollments, HttpStatus.OK);
    }

    @GetMapping("/enrollment/{id}")
    public ResponseEntity<Enrollment> getEnrollmentById(@PathVariable String id) {
        Optional<Enrollment> enrollmentData = enrollmentRepository.findById(id);

        if (enrollmentData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Enrollment enrollment = enrollmentData.get();
        String currentUserId = userService.getCurrentUserId();

        if (userService.userHasRole("learner")
                && enrollment.getLearnerUserId().equals(currentUserId)) {
            return new ResponseEntity<>(enrollment, HttpStatus.OK);
        }

        if (userService.userHasRole("tutor")
                && courseService.courseBelongsToTutor(enrollment.getCourseId(), currentUserId)) {
            return new ResponseEntity<>(enrollment, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    /* eigentlich abgelöst von /enrollment/me */
    @GetMapping("/enrollment/learner/{learnerUserId}")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByLearner(@PathVariable String learnerUserId) {

        if (!userService.userHasRole("learner")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        String currentUserId = userService.getCurrentUserId();

        if (!learnerUserId.equals(currentUserId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Enrollment> enrollments = enrollmentRepository.findByLearnerUserId(currentUserId);

        return new ResponseEntity<>(enrollments, HttpStatus.OK);
    }

    @GetMapping("/enrollment/course/{courseId}")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByCourse(@PathVariable String courseId) {

        if (!userService.userHasRole("tutor")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (!courseService.courseExists(courseId)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!courseService.courseBelongsToTutor(courseId, userService.getCurrentUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);

        return new ResponseEntity<>(enrollments, HttpStatus.OK);
    }

    /* Get enrollments for the current Auth0 user */
    @GetMapping("/enrollment/me")
    public ResponseEntity<List<Enrollment>> getMyEnrollments() {

        if (!userService.userHasRole("learner")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        String currentUserId = userService.getCurrentUserId();

        List<Enrollment> enrollments = enrollmentRepository.findByLearnerUserId(currentUserId);

        return new ResponseEntity<>(enrollments, HttpStatus.OK);
    }
}