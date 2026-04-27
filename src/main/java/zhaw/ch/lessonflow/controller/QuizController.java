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
import zhaw.ch.lessonflow.repository.QuizRepository;
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

    @PostMapping("/quiz")
    public ResponseEntity<Quiz> createQuiz(@RequestBody QuizCreateDTO fDTO) {

        if (!userService.userHasRole("tutor")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<Lesson> lessonData = lessonService.getLessonById(fDTO.getLessonId());

        if (lessonData.isEmpty()) {
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
}