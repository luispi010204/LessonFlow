package zhaw.ch.lessonflow.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zhaw.ch.lessonflow.model.Quiz;
import zhaw.ch.lessonflow.repository.QuizRepository;

@Service
public class QuizService {

    @Autowired
    QuizRepository quizRepository;

    public Optional<Quiz> getQuizById(String quizId) {
        return quizRepository.findById(quizId);
    }
}