package zhaw.ch.lessonflow.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import zhaw.ch.lessonflow.model.QuizAttempt;
import zhaw.ch.lessonflow.repository.QuizAttemptRepository;

@RestController
@RequestMapping("/api")
public class QuizAttemptController {

    @Autowired
    QuizAttemptRepository quizAttemptRepository;

    @PostMapping("/attempt")
    public ResponseEntity<QuizAttempt> createQuizAttempt(@RequestBody QuizAttempt quizAttempt) {
        QuizAttempt savedAttempt = quizAttemptRepository.save(quizAttempt);
        return new ResponseEntity<>(savedAttempt, HttpStatus.CREATED);
    }

    @GetMapping("/attempt")
    public List<QuizAttempt> getAllQuizAttempts() {
        return quizAttemptRepository.findAll();
    }

    @GetMapping("/attempt/{id}")
    public ResponseEntity<QuizAttempt> getQuizAttemptById(@PathVariable String id) {
        Optional<QuizAttempt> quizAttempt = quizAttemptRepository.findById(id);

        if (quizAttempt.isPresent()) {
            return new ResponseEntity<>(quizAttempt.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/attempt/quiz/{quizId}")
    public List<QuizAttempt> getQuizAttemptsByQuizId(@PathVariable String quizId) {
        return quizAttemptRepository.findByQuizId(quizId);
    }

    @GetMapping("/attempt/enrollment/{enrollmentId}")
    public List<QuizAttempt> getQuizAttemptsByEnrollmentId(@PathVariable String enrollmentId) {
        return quizAttemptRepository.findByEnrollmentId(enrollmentId);
    }

    @GetMapping("/attempt/lesson/{lessonId}")
    public List<QuizAttempt> getQuizAttemptsByLessonId(@PathVariable String lessonId) {
        return quizAttemptRepository.findByLessonId(lessonId);
    }
}