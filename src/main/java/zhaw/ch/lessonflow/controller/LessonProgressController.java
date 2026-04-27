package zhaw.ch.lessonflow.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import zhaw.ch.lessonflow.model.Enrollment;
import zhaw.ch.lessonflow.model.Lesson;
import zhaw.ch.lessonflow.model.LessonProgress;
import zhaw.ch.lessonflow.model.ProgressSummaryDTO;
import zhaw.ch.lessonflow.repository.LessonProgressRepository;
import zhaw.ch.lessonflow.services.CourseService;
import zhaw.ch.lessonflow.services.EnrollmentService;
import zhaw.ch.lessonflow.services.LessonProgressService;
import zhaw.ch.lessonflow.services.LessonService;
import zhaw.ch.lessonflow.services.UserService;

@RestController
@RequestMapping("/api")
public class LessonProgressController {

    @Autowired
    LessonProgressRepository lessonProgressRepository;

    @Autowired
    LessonProgressService lessonProgressService;

    @Autowired
    EnrollmentService enrollmentService;

    @Autowired
    CourseService courseService;

    @Autowired
    LessonService lessonService;

    @Autowired
    UserService userService;

    /*
     * Disabled:
     * LessonProgress is created automatically when a learner enrolls in a course.
     * Direct creation through the API should not be allowed anymore.
     */
    /*
    @PostMapping("/progress")
    public ResponseEntity<LessonProgress> createLessonProgress(@RequestBody LessonProgress lessonProgress) {
        LessonProgress savedProgress = lessonProgressRepository.save(lessonProgress);
        return new ResponseEntity<>(savedProgress, HttpStatus.CREATED);
    }
    */

    /*
     * Disabled:
     * Returning all progress entries would expose data across users/courses.
     */
    /*
    @GetMapping("/progress")
    public List<LessonProgress> getAllLessonProgress() {
        return lessonProgressRepository.findAll();
    }
    */

    @GetMapping("/progress/{id}")
    public ResponseEntity<LessonProgress> getLessonProgressById(@PathVariable String id) {
        Optional<LessonProgress> lessonProgress = lessonProgressRepository.findById(id);

        if (lessonProgress.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        LessonProgress progress = lessonProgress.get();

        if (!canAccessEnrollment(progress.getEnrollmentId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(progress, HttpStatus.OK);
    }

    @GetMapping("/progress/enrollment/{enrollmentId}")
    public ResponseEntity<List<LessonProgress>> getLessonProgressByEnrollmentId(@PathVariable String enrollmentId) {
        Optional<Enrollment> enrollmentData = enrollmentService.getEnrollmentById(enrollmentId);

        if (enrollmentData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!canAccessEnrollment(enrollmentId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<LessonProgress> progressList = lessonProgressRepository.findByEnrollmentId(enrollmentId);
        return new ResponseEntity<>(progressList, HttpStatus.OK);
    }

    @GetMapping("/progress/lesson/{lessonId}")
    public ResponseEntity<List<LessonProgress>> getLessonProgressByLessonId(@PathVariable String lessonId) {
        Optional<Lesson> lessonData = lessonService.getLessonById(lessonId);

        if (lessonData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Lesson lesson = lessonData.get();

        if (!userService.userHasRole("tutor")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (!courseService.courseBelongsToTutor(lesson.getCourseId(), userService.getCurrentUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<LessonProgress> progressList = lessonProgressRepository.findByLessonId(lessonId);
        return new ResponseEntity<>(progressList, HttpStatus.OK);
    }

    @PostMapping("/progress/{id}/material-done")
    public ResponseEntity<LessonProgress> markMaterialDone(@PathVariable String id) {
        Optional<LessonProgress> progressData = lessonProgressRepository.findById(id);

        if (progressData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        LessonProgress progress = progressData.get();

        if (!canLearnerModifyProgress(progress)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<LessonProgress> updatedProgress = lessonProgressService.markMaterialDone(id);

        if (updatedProgress.isPresent()) {
            return new ResponseEntity<>(updatedProgress.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/progress/{id}/meeting-done")
    public ResponseEntity<LessonProgress> confirmMeeting(@PathVariable String id) {
        Optional<LessonProgress> progressData = lessonProgressRepository.findById(id);

        if (progressData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        LessonProgress progress = progressData.get();

        if (!canLearnerModifyProgress(progress)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<LessonProgress> updatedProgress = lessonProgressService.confirmMeeting(id);

        if (updatedProgress.isPresent()) {
            return new ResponseEntity<>(updatedProgress.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /*
     * Disabled:
     * Passing a lesson is now handled through POST /api/attempt/submit.
     */
    /*
    @PostMapping("/progress/{id}/passed")
    public ResponseEntity<LessonProgress> markPassed(@PathVariable String id) {
        Optional<LessonProgress> progress = lessonProgressService.markPassed(id);

        if (progress.isPresent()) {
            return new ResponseEntity<>(progress.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    */

    @GetMapping("/enrollment/{id}/current-lesson")
    public ResponseEntity<Lesson> getCurrentLesson(@PathVariable String id) {
        Optional<Enrollment> enrollmentData = enrollmentService.getEnrollmentById(id);

        if (enrollmentData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!canAccessEnrollment(id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<Lesson> lesson = lessonProgressService.getCurrentLesson(id);

        if (lesson.isPresent()) {
            return new ResponseEntity<>(lesson.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/enrollment/{id}/progress-summary")
    public ResponseEntity<ProgressSummaryDTO> getProgressSummary(@PathVariable String id) {
        Optional<Enrollment> enrollmentData = enrollmentService.getEnrollmentById(id);

        if (enrollmentData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!canAccessEnrollment(id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<ProgressSummaryDTO> summary = lessonProgressService.getProgressSummary(id);

        if (summary.isPresent()) {
            return new ResponseEntity<>(summary.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private boolean canAccessEnrollment(String enrollmentId) {
        Optional<Enrollment> enrollmentData = enrollmentService.getEnrollmentById(enrollmentId);

        if (enrollmentData.isEmpty()) {
            return false;
        }

        Enrollment enrollment = enrollmentData.get();
        String currentUserId = userService.getCurrentUserId();

        if (userService.userHasRole("learner")
                && enrollment.getLearnerUserId().equals(currentUserId)) {
            return true;
        }

        if (userService.userHasRole("tutor")
                && courseService.courseBelongsToTutor(enrollment.getCourseId(), currentUserId)) {
            return true;
        }

        return false;
    }

    private boolean canLearnerModifyProgress(LessonProgress progress) {
        if (!userService.userHasRole("learner")) {
            return false;
        }

        return enrollmentService.enrollmentBelongsToLearner(
                progress.getEnrollmentId(),
                userService.getCurrentUserId()
        );
    }
}