package zhaw.ch.lessonflow.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import zhaw.ch.lessonflow.model.Enrollment;
import zhaw.ch.lessonflow.model.Lesson;
import zhaw.ch.lessonflow.model.Quiz;
import zhaw.ch.lessonflow.model.QuizAttempt;
import zhaw.ch.lessonflow.model.QuizAttemptCreateDTO;
import zhaw.ch.lessonflow.repository.QuizAttemptRepository;
import zhaw.ch.lessonflow.repository.QuizRepository;
import zhaw.ch.lessonflow.services.CourseService;
import zhaw.ch.lessonflow.services.EnrollmentService;
import zhaw.ch.lessonflow.services.LessonService;
import zhaw.ch.lessonflow.services.QuizAttemptService;
import zhaw.ch.lessonflow.services.UserService;

@RestController
@RequestMapping("/api")
public class QuizAttemptController {

    @Autowired
    QuizAttemptRepository quizAttemptRepository;

    @Autowired
    QuizAttemptService quizAttemptService;

    @Autowired
    QuizRepository quizRepository;

    @Autowired
    EnrollmentService enrollmentService;

    @Autowired
    LessonService lessonService;

    @Autowired
    CourseService courseService;

    @Autowired
    UserService userService;

    /*
     * Disabled:
     * QuizAttempts should only be created through /attempt/submit
     * because the backend must calculate whether the attempt was passed.
     */
    /*
    @PostMapping("/attempt")
    public ResponseEntity<QuizAttempt> createQuizAttempt(@RequestBody QuizAttempt quizAttempt) {
        QuizAttempt savedAttempt = quizAttemptRepository.save(quizAttempt);
        return new ResponseEntity<>(savedAttempt, HttpStatus.CREATED);
    }
    */

    /*
     * Disabled:
     * Returning all quiz attempts would expose data across learners and courses.
     */
    /*
    @GetMapping("/attempt")
    public List<QuizAttempt> getAllQuizAttempts() {
        return quizAttemptRepository.findAll();
    }
    */

    @GetMapping("/attempt/{id}")
    public ResponseEntity<QuizAttempt> getQuizAttemptById(@PathVariable String id) {
        Optional<QuizAttempt> quizAttemptData = quizAttemptRepository.findById(id);

        if (quizAttemptData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        QuizAttempt quizAttempt = quizAttemptData.get();

        if (!canAccessEnrollment(quizAttempt.getEnrollmentId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(quizAttempt, HttpStatus.OK);
    }

    @GetMapping("/attempt/quiz/{quizId}")
    public ResponseEntity<List<QuizAttempt>> getQuizAttemptsByQuizId(@PathVariable String quizId) {
        Optional<Quiz> quizData = quizRepository.findById(quizId);

        if (quizData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Quiz quiz = quizData.get();

        if (!canTutorAccessLesson(quiz.getLessonId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);
        return new ResponseEntity<>(attempts, HttpStatus.OK);
    }

    @GetMapping("/attempt/enrollment/{enrollmentId}")
    public ResponseEntity<List<QuizAttempt>> getQuizAttemptsByEnrollmentId(@PathVariable String enrollmentId) {
        Optional<Enrollment> enrollmentData = enrollmentService.getEnrollmentById(enrollmentId);

        if (enrollmentData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!canAccessEnrollment(enrollmentId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<QuizAttempt> attempts = quizAttemptRepository.findByEnrollmentId(enrollmentId);
        return new ResponseEntity<>(attempts, HttpStatus.OK);
    }

    @GetMapping("/attempt/lesson/{lessonId}")
    public ResponseEntity<List<QuizAttempt>> getQuizAttemptsByLessonId(@PathVariable String lessonId) {
        Optional<Lesson> lessonData = lessonService.getLessonById(lessonId);

        if (lessonData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (!canTutorAccessLesson(lessonId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<QuizAttempt> attempts = quizAttemptRepository.findByLessonId(lessonId);
        return new ResponseEntity<>(attempts, HttpStatus.OK);
    }

    @PostMapping("/attempt/submit")
    public ResponseEntity<QuizAttempt> submitQuizAttempt(@RequestBody QuizAttemptCreateDTO fDTO) {

        if (!userService.userHasRole("learner")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<Enrollment> enrollmentData = enrollmentService.getEnrollmentById(fDTO.getEnrollmentId());

        if (enrollmentData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!enrollmentService.enrollmentBelongsToLearner(
                fDTO.getEnrollmentId(),
                userService.getCurrentUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<QuizAttempt> attempt = quizAttemptService.submitQuizAttempt(fDTO);

        if (attempt.isPresent()) {
            return new ResponseEntity<>(attempt.get(), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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

    private boolean canTutorAccessLesson(String lessonId) {
        if (!userService.userHasRole("tutor")) {
            return false;
        }

        Optional<Lesson> lessonData = lessonService.getLessonById(lessonId);

        if (lessonData.isEmpty()) {
            return false;
        }

        Lesson lesson = lessonData.get();

        return courseService.courseBelongsToTutor(
                lesson.getCourseId(),
                userService.getCurrentUserId()
        );
    }
}