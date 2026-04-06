package zhaw.ch.lessonflow.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zhaw.ch.lessonflow.model.Enrollment;
import zhaw.ch.lessonflow.repository.EnrollmentRepository;

@Service
public class EnrollmentService {

    @Autowired
    EnrollmentRepository enrollmentRepository;

    public boolean enrollmentExists(String enrollmentId) {
        return enrollmentRepository.existsById(enrollmentId);
    }

    public boolean isAlreadyEnrolled(String courseId, String learnerUserId) {
        Optional<Enrollment> enrollment = enrollmentRepository.findByCourseIdAndLearnerUserId(courseId, learnerUserId);

        return enrollment.isPresent();
    }
}