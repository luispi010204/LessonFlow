package zhaw.ch.lessonflow.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zhaw.ch.lessonflow.model.Enrollment;
import zhaw.ch.lessonflow.model.Lesson;
import zhaw.ch.lessonflow.model.LessonProgress;
import zhaw.ch.lessonflow.model.LessonProgressState;
import zhaw.ch.lessonflow.model.ProgressSummaryDTO;
import zhaw.ch.lessonflow.repository.LessonProgressRepository;
import zhaw.ch.lessonflow.repository.LessonRepository;

@Service
public class LessonProgressService {

    @Autowired
    LessonProgressRepository lessonProgressRepository;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    EnrollmentService enrollmentService;

    public Optional<LessonProgress> markMaterialDone(String progressId) {
        Optional<LessonProgress> progressData = lessonProgressRepository.findById(progressId);

        if (progressData.isEmpty()) {
            return Optional.empty();
        }

        LessonProgress progress = progressData.get();

        if (progress.getState() != LessonProgressState.UNLOCKED) {
            return Optional.empty();
        }

        progress.setState(LessonProgressState.MATERIAL_DONE);
        LessonProgress savedProgress = lessonProgressRepository.save(progress);
        return Optional.of(savedProgress);
    }

    public Optional<LessonProgress> confirmMeeting(String progressId) {
        Optional<LessonProgress> progressData = lessonProgressRepository.findById(progressId);

        if (progressData.isEmpty()) {
            return Optional.empty();
        }

        LessonProgress progress = progressData.get();

        if (progress.getState() != LessonProgressState.MATERIAL_DONE) {
            return Optional.empty();
        }

        progress.setMeetingConfirmed(true);
        progress.setState(LessonProgressState.MEETING_DONE);

        LessonProgress savedProgress = lessonProgressRepository.save(progress);
        return Optional.of(savedProgress);
    }

    public Optional<LessonProgress> markPassed(String progressId) {
        Optional<LessonProgress> progressData = lessonProgressRepository.findById(progressId);

        if (progressData.isEmpty()) {
            return Optional.empty();
        }

        LessonProgress progress = progressData.get();

        if (progress.getState() != LessonProgressState.MEETING_DONE) {
            return Optional.empty();
        }

        progress.setState(LessonProgressState.PASSED);
        LessonProgress savedProgress = lessonProgressRepository.save(progress);

        Optional<Lesson> currentLessonData = lessonRepository.findById(progress.getLessonId());
        if (currentLessonData.isPresent()) {
            Lesson currentLesson = currentLessonData.get();

            Optional<Lesson> nextLessonData = lessonRepository.findByCourseIdAndLessonNumber(
                    currentLesson.getCourseId(),
                    currentLesson.getLessonNumber() + 1);

            if (nextLessonData.isPresent()) {
                Lesson nextLesson = nextLessonData.get();

                Optional<LessonProgress> nextProgressData = lessonProgressRepository.findByEnrollmentIdAndLessonId(
                        progress.getEnrollmentId(),
                        nextLesson.getId());

                if (nextProgressData.isPresent()) {
                    LessonProgress nextProgress = nextProgressData.get();

                    if (nextProgress.getState() == LessonProgressState.LOCKED) {
                        nextProgress.setState(LessonProgressState.UNLOCKED);
                        lessonProgressRepository.save(nextProgress);
                    }
                }
            }
        }

        return Optional.of(savedProgress);
    }

    public Optional<LessonProgress> getByEnrollmentIdAndLessonId(String enrollmentId, String lessonId) {
        return lessonProgressRepository.findByEnrollmentIdAndLessonId(enrollmentId, lessonId);
    }

    public LessonProgress save(LessonProgress progress) {
        return lessonProgressRepository.save(progress);
    }

    public Optional<Lesson> getCurrentLesson(String enrollmentId) {
        enrollmentService.syncMissingProgressForEnrollment(enrollmentId);

        Optional<Enrollment> enrollmentData = enrollmentService.getEnrollmentById(enrollmentId);

        if (enrollmentData.isEmpty()) {
            return Optional.empty();
        }

        Enrollment enrollment = enrollmentData.get();

        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByLessonNumberAsc(
                enrollment.getCourseId());

        for (Lesson lesson : lessons) {
            Optional<LessonProgress> progressData =
                    lessonProgressRepository.findByEnrollmentIdAndLessonId(
                            enrollmentId,
                            lesson.getId());

            if (progressData.isPresent() && isCurrentProgressState(progressData.get().getState())) {
                return Optional.of(lesson);
            }
        }

        return Optional.empty();
    }

    public Optional<ProgressSummaryDTO> getProgressSummary(String enrollmentId) {
        enrollmentService.syncMissingProgressForEnrollment(enrollmentId);

        Optional<Enrollment> enrollmentData = enrollmentService.getEnrollmentById(enrollmentId);

        if (enrollmentData.isEmpty()) {
            return Optional.empty();
        }

        Enrollment enrollment = enrollmentData.get();

        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByLessonNumberAsc(
                enrollment.getCourseId());

        if (lessons.isEmpty()) {
            return Optional.empty();
        }

        int totalLessons = lessons.size();
        int passedLessons = 0;
        String currentLessonId = null;

        for (Lesson lesson : lessons) {
            Optional<LessonProgress> progressData =
                    lessonProgressRepository.findByEnrollmentIdAndLessonId(
                            enrollmentId,
                            lesson.getId());

            if (progressData.isEmpty()) {
                continue;
            }

            LessonProgress progress = progressData.get();

            if (progress.getState() == LessonProgressState.PASSED) {
                passedLessons++;
            }

            if (currentLessonId == null && isCurrentProgressState(progress.getState())) {
                currentLessonId = progress.getLessonId();
            }
        }

        ProgressSummaryDTO summary = new ProgressSummaryDTO(
                totalLessons,
                passedLessons,
                currentLessonId);

        return Optional.of(summary);
    }

    private boolean isCurrentProgressState(LessonProgressState state) {
        return state == LessonProgressState.UNLOCKED
                || state == LessonProgressState.MATERIAL_DONE
                || state == LessonProgressState.MEETING_DONE;
    }
}