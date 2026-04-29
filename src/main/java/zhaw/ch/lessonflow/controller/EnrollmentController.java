package zhaw.ch.lessonflow.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import zhaw.ch.lessonflow.model.Enrollment;
import zhaw.ch.lessonflow.model.EnrollmentCreateDTO;
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

        if (!courseService.courseExists(fDTO.getCourseId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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
    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    @GetMapping("/enrollment/{id}")
    public ResponseEntity<Enrollment> getEnrollmentById(@PathVariable String id) {
        Optional<Enrollment> enrollment = enrollmentRepository.findById(id);

        if (enrollment.isPresent()) {
            return new ResponseEntity<>(enrollment.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /* eigentlich abgelöst von /enrollment/me  */
    @GetMapping("/enrollment/learner/{learnerUserId}")
    public List<Enrollment> getEnrollmentsByLearner(@PathVariable String learnerUserId) {
        return enrollmentRepository.findByLearnerUserId(learnerUserId);
    }

    @GetMapping("/enrollment/course/{courseId}")
    public List<Enrollment> getEnrollmentsByCourse(@PathVariable String courseId) {
        return enrollmentRepository.findByCourseId(courseId);
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