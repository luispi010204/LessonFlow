package zhaw.ch.lessonflow.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import zhaw.ch.lessonflow.model.Lesson;
import zhaw.ch.lessonflow.model.Quiz;
import zhaw.ch.lessonflow.model.QuizCreateDTO;
import zhaw.ch.lessonflow.model.QuizQuestion;
import zhaw.ch.lessonflow.repository.QuizRepository;
import zhaw.ch.lessonflow.services.AiQuizService;
import zhaw.ch.lessonflow.services.CourseService;
import zhaw.ch.lessonflow.services.LessonService;
import zhaw.ch.lessonflow.services.UserService;

@RestController
@RequestMapping("/api")
public class QuizController {

    @Autowired
    QuizRepository quizRepository;

    @Autowired
    LessonService lessonService;

    @Autowired
    UserService userService;

    @Autowired
    CourseService courseService;

    @Autowired
    AiQuizService aiQuizService;

    @PostMapping("/quiz")
    public ResponseEntity<Quiz> createQuiz(@RequestBody QuizCreateDTO fDTO) {

        if (!userService.userHasRole("tutor")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<Lesson> lessonData = lessonService.getLessonById(fDTO.getLessonId());

        if (lessonData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (fDTO.getPassPercent() < 1 || fDTO.getPassPercent() > 100) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!isValidQuizQuestions(fDTO.getQuestions())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Lesson lesson = lessonData.get();

        if (!courseService.courseBelongsToTutor(lesson.getCourseId(), userService.getCurrentUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (quizRepository.findByLessonId(fDTO.getLessonId()).isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Quiz fDAO = new Quiz(
                fDTO.getLessonId(),
                fDTO.getPassPercent(),
                fDTO.getQuestions());

        Quiz savedQuiz = quizRepository.save(fDAO);
        return new ResponseEntity<>(savedQuiz, HttpStatus.CREATED);
    }

    @PostMapping("/quiz/lesson/{lessonId}/generate-ai")
    public ResponseEntity<Quiz> generateAiQuiz(
            @PathVariable String lessonId,
            @RequestParam(defaultValue = "3") int questionCount,
            @RequestParam(defaultValue = "70") int passPercent) {

        if (!userService.userHasRole("tutor")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<Lesson> lessonData = lessonService.getLessonById(lessonId);

        if (lessonData.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (passPercent < 1 || passPercent > 100) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (questionCount < 1 || questionCount > 10) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Lesson lesson = lessonData.get();

        if (!courseService.courseBelongsToTutor(lesson.getCourseId(), userService.getCurrentUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (quizRepository.findByLessonId(lessonId).isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<List<QuizQuestion>> generatedQuestions = aiQuizService.generateQuizQuestions(
                lesson.getTitle(),
                lesson.getMaterial(),
                questionCount
        );

        if (generatedQuestions.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (!isValidQuizQuestions(generatedQuestions.get())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Quiz quiz = new Quiz(
                lessonId,
                passPercent,
                generatedQuestions.get()
        );

        Quiz savedQuiz = quizRepository.save(quiz);
        return new ResponseEntity<>(savedQuiz, HttpStatus.CREATED);
    }

    @GetMapping("/quiz")
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    @GetMapping("/quiz/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable String id) {
        Optional<Quiz> quiz = quizRepository.findById(id);

        if (quiz.isPresent()) {
            return new ResponseEntity<>(quiz.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/quiz/lesson/{lessonId}")
    public ResponseEntity<Quiz> getQuizByLessonId(@PathVariable String lessonId) {
        Optional<Quiz> quiz = quizRepository.findByLessonId(lessonId);

        if (quiz.isPresent()) {
            return new ResponseEntity<>(quiz.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private boolean isValidQuizQuestions(List<QuizQuestion> questions) {
        if (questions == null || questions.isEmpty()) {
            return false;
        }

        for (QuizQuestion question : questions) {
            if (question.getQuestionText() == null || question.getQuestionText().isBlank()) {
                return false;
            }

            if (question.getOptions() == null || question.getOptions().size() < 2) {
                return false;
            }

            for (String option : question.getOptions()) {
                if (option == null || option.isBlank()) {
                    return false;
                }
            }

            if (question.getCorrectOptionIndex() < 0
                    || question.getCorrectOptionIndex() >= question.getOptions().size()) {
                return false;
            }
        }

        return true;
    }
}