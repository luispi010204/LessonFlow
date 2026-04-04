package zhaw.ch.lessonflow.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import zhaw.ch.lessonflow.model.Enrollment;
import zhaw.ch.lessonflow.repository.EnrollmentRepository;

@RestController
@RequestMapping("/api")
public class EnrollmentController {

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @PostMapping("/enrollment")
    public ResponseEntity<Enrollment> createEnrollment(@RequestBody Enrollment enrollment) {
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
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

    @GetMapping("/enrollment/learner/{learnerUserId}")
    public List<Enrollment> getEnrollmentsByLearner(@PathVariable String learnerUserId) {
        return enrollmentRepository.findByLearnerUserId(learnerUserId);
    }

    @GetMapping("/enrollment/course/{courseId}")
    public List<Enrollment> getEnrollmentsByCourse(@PathVariable String courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }
}