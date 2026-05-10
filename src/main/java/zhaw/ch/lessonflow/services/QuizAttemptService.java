package zhaw.ch.lessonflow.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zhaw.ch.lessonflow.model.LessonProgress;
import zhaw.ch.lessonflow.model.LessonProgressState;
import zhaw.ch.lessonflow.model.Quiz;
import zhaw.ch.lessonflow.model.QuizAttempt;
import zhaw.ch.lessonflow.model.QuizAttemptCreateDTO;
import zhaw.ch.lessonflow.model.QuizQuestion;
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

        Optional<LessonProgress> progressData = lessonProgressService
                .getByEnrollmentIdAndLessonId(dto.getEnrollmentId(), dto.getLessonId());

        if (progressData.isEmpty()) {
            return Optional.empty();
        }

        LessonProgress progress = progressData.get();

        if (progress.getState() != LessonProgressState.MEETING_DONE) {
            return Optional.empty();
        }

        Quiz quiz = quizData.get();

        List<QuizQuestion> questions = quiz.getQuestions();
        List<Integer> selectedOptionIndexes = dto.getSelectedOptionIndexes();

        if (questions == null || questions.isEmpty()) {
            return Optional.empty();
        }

        if (selectedOptionIndexes == null || selectedOptionIndexes.size() != questions.size()) {
            return Optional.empty();
        }

        int correctAnswers = 0;

        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion question = questions.get(i);
            Integer selectedOptionIndex = selectedOptionIndexes.get(i);

            if (selectedOptionIndex == null) {
                return Optional.empty();
            }

            if (selectedOptionIndex < 0 || selectedOptionIndex >= question.getOptions().size()) {
                return Optional.empty();
            }

            if (selectedOptionIndex == question.getCorrectOptionIndex()) {
                correctAnswers++;
            }
        }

        double scorePercent = ((double) correctAnswers / questions.size()) * 100;
        boolean passed = scorePercent >= quiz.getPassPercent();

        QuizAttempt attempt = new QuizAttempt(
                dto.getQuizId(),
                dto.getEnrollmentId(),
                dto.getLessonId(),
                scorePercent,
                passed,
                selectedOptionIndexes);

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        progress.setQuizAttemptsCount(progress.getQuizAttemptsCount() + 1);
        lessonProgressService.save(progress);

        if (passed) {
            lessonProgressService.markPassed(progress.getId());
        }

        return Optional.of(savedAttempt);
    }
}