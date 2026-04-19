package zhaw.ch.lessonflow.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zhaw.ch.lessonflow.model.LessonProgress;
import zhaw.ch.lessonflow.model.LessonProgressState;
import zhaw.ch.lessonflow.model.Quiz;
import zhaw.ch.lessonflow.model.QuizAttempt;
import zhaw.ch.lessonflow.model.QuizAttemptCreateDTO;
import zhaw.ch.lessonflow.repository.QuizAttemptRepository;

@Service
public class QuizAttemptService {

    @Autowired
    QuizAttemptRepository quizAttemptRepository;

    @Autowired
    QuizService quizService;

    @Autowired
    LessonProgressService lessonProgressService;

    

    public Optional<QuizAttempt> submitQuizAttempt(QuizAttemptCreateDTO dto) {
        Optional<Quiz> quizData = quizService.getQuizById(dto.getQuizId());
        if (quizData.isEmpty()) {
            return Optional.empty();
        }

        Optional<LessonProgress> progressData =
                lessonProgressService.getByEnrollmentIdAndLessonId(dto.getEnrollmentId(), dto.getLessonId());

        if (progressData.isEmpty()) {
            return Optional.empty();
        }

        LessonProgress progress = progressData.get();

        if (progress.getState() != LessonProgressState.MEETING_DONE) {
            return Optional.empty();
        }

        Quiz quiz = quizData.get();
        boolean passed = dto.getScorePercent() >= quiz.getPassPercent();

        QuizAttempt attempt = new QuizAttempt(
                dto.getQuizId(),
                dto.getEnrollmentId(),
                dto.getLessonId(),
                dto.getScorePercent(),
                passed
        );

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        progress.setQuizAttemptsCount(progress.getQuizAttemptsCount() + 1);
        lessonProgressService.save(progress);

        if (passed) {
            lessonProgressService.markPassed(progress.getId());
        }

        return Optional.of(savedAttempt);
    }
}