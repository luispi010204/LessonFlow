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

        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByLessonNumberAsc(
                savedEnrollment.getCourseId());

        for (Lesson lesson : lessons) {
            createProgressIfMissing(savedEnrollment, lesson);
        }

        return savedEnrollment;
    }

    public void createProgressForNewLessonForExistingEnrollments(Lesson lesson) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(lesson.getCourseId());

        for (Enrollment enrollment : enrollments) {
            createProgressIfMissing(enrollment, lesson);
        }
    }

    public void syncMissingProgressForEnrollment(String enrollmentId) {
        Optional<Enrollment> enrollmentData = enrollmentRepository.findById(enrollmentId);

        if (enrollmentData.isEmpty()) {
            return;
        }

        Enrollment enrollment = enrollmentData.get();

        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByLessonNumberAsc(
                enrollment.getCourseId());

        for (Lesson lesson : lessons) {
            createProgressIfMissing(enrollment, lesson);
        }
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

    private void createProgressIfMissing(Enrollment enrollment, Lesson lesson) {
        Optional<LessonProgress> existingProgress =
                lessonProgressRepository.findByEnrollmentIdAndLessonId(
                        enrollment.getId(),
                        lesson.getId());

        if (existingProgress.isPresent()) {
            return;
        }

        LessonProgressState state = determineInitialState(enrollment.getId(), lesson);

        LessonProgress lessonProgress = new LessonProgress(
                enrollment.getId(),
                lesson.getId(),
                state,
                false,
                0
        );

        lessonProgressRepository.save(lessonProgress);
    }

    private LessonProgressState determineInitialState(String enrollmentId, Lesson lesson) {
        if (lesson.getLessonNumber() == 1) {
            return LessonProgressState.UNLOCKED;
        }

        Optional<Lesson> previousLessonData =
                lessonRepository.findByCourseIdAndLessonNumber(
                        lesson.getCourseId(),
                        lesson.getLessonNumber() - 1);

        if (previousLessonData.isEmpty()) {
            return LessonProgressState.LOCKED;
        }

        Lesson previousLesson = previousLessonData.get();

        Optional<LessonProgress> previousProgressData =
                lessonProgressRepository.findByEnrollmentIdAndLessonId(
                        enrollmentId,
                        previousLesson.getId());

        if (previousProgressData.isPresent()
                && previousProgressData.get().getState() == LessonProgressState.PASSED) {
            return LessonProgressState.UNLOCKED;
        }

        return LessonProgressState.LOCKED;
    }
}