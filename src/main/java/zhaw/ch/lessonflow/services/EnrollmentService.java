package zhaw.ch.lessonflow.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zhaw.ch.lessonflow.model.Enrollment;
import zhaw.ch.lessonflow.model.Lesson;
import zhaw.ch.lessonflow.model.LessonProgress;
import zhaw.ch.lessonflow.model.LessonProgressState;
import zhaw.ch.lessonflow.repository.EnrollmentRepository;
import zhaw.ch.lessonflow.repository.LessonProgressRepository;
import zhaw.ch.lessonflow.repository.LessonRepository;

@Service
public class EnrollmentService {

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    LessonProgressRepository lessonProgressRepository;

    public boolean enrollmentExists(String enrollmentId) {
        return enrollmentRepository.existsById(enrollmentId);
    }

    public boolean isAlreadyEnrolled(String courseId, String learnerUserId) {
        Optional<Enrollment> enrollment =
                enrollmentRepository.findByCourseIdAndLearnerUserId(courseId, learnerUserId);

        return enrollment.isPresent();
    }

    public Enrollment createEnrollmentWithProgress(Enrollment enrollment) {
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        List<Lesson> lessons = lessonRepository.findByCourseId(savedEnrollment.getCourseId());

        for (int i = 0; i < lessons.size(); i++) {
            Lesson lesson = lessons.get(i);

            LessonProgressState state;
            if (i == 0) {
                state = LessonProgressState.UNLOCKED;
            } else {
                state = LessonProgressState.LOCKED;
            }

            LessonProgress lessonProgress = new LessonProgress(
                    savedEnrollment.getId(),
                    lesson.getId(),
                    state,
                    false,
                    0
            );

            lessonProgressRepository.save(lessonProgress);
        }

        return savedEnrollment;
    }

    public Optional<Enrollment> getEnrollmentById(String enrollmentId) {
    return enrollmentRepository.findById(enrollmentId);
}

public boolean enrollmentBelongsToLearner(String enrollmentId, String learnerUserId) {
    Optional<Enrollment> enrollmentData = enrollmentRepository.findById(enrollmentId);

    if (enrollmentData.isEmpty()) {
        return false;
    }

    Enrollment enrollment = enrollmentData.get();
    return enrollment.getLearnerUserId().equals(learnerUserId);
}
}