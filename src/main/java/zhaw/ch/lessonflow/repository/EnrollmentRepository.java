package zhaw.ch.lessonflow.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import zhaw.ch.lessonflow.model.Enrollment;

public interface EnrollmentRepository extends MongoRepository<Enrollment, String> {
    List<Enrollment> findByLearnerUserId(String learnerUserId);

    List<Enrollment> findByCourseId(String courseId);

    Optional<Enrollment> findByCourseIdAndLearnerUserId(String courseId, String learnerUserId);
}