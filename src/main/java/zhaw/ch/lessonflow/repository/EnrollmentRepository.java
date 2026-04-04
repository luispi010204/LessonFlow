package zhaw.ch.lessonflow.repository;

import zhaw.ch.lessonflow.model.Enrollment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends MongoRepository<Enrollment, String> {
    List<Enrollment> findByLearnerUserId(String learnerUserId);
    List<Enrollment> findByCourseId(String courseId);
    Optional<Enrollment> findByCourseIdAndLearnerUserId(String courseId, String learnerUserId);
}