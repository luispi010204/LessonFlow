package zhaw.ch.lessonflow.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import zhaw.ch.lessonflow.model.LessonProgress;
import zhaw.ch.lessonflow.model.LessonProgressState;
import zhaw.ch.lessonflow.model.Quiz;
import zhaw.ch.lessonflow.model.QuizAttempt;
import zhaw.ch.lessonflow.model.QuizAttemptCreateDTO;
import zhaw.ch.lessonflow.repository.QuizAttemptRepository;

@ExtendWith(MockitoExtension.class)
public class QuizAttemptServiceTest {

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private QuizService quizService;

    @Mock
    private LessonProgressService lessonProgressService;

    @InjectMocks
    private QuizAttemptService quizAttemptService;

    private QuizAttemptCreateDTO dto;
    private Quiz quiz;
    private LessonProgress progress;

    @BeforeEach
    void setUp() {
        dto = new QuizAttemptCreateDTO();
        ReflectionTestUtils.setField(dto, "quizId", "quiz-1");
        ReflectionTestUtils.setField(dto, "enrollmentId", "enrollment-1");
        ReflectionTestUtils.setField(dto, "lessonId", "lesson-1");
        ReflectionTestUtils.setField(dto, "scorePercent", 80.0);

        quiz = new Quiz(
                "lesson-1",
                70,
                List.of("Question 1", "Question 2")
        );
        ReflectionTestUtils.setField(quiz, "id", "quiz-1");

        progress = new LessonProgress(
                "enrollment-1",
                "lesson-1",
                LessonProgressState.MEETING_DONE,
                false,
                0
        );
        ReflectionTestUtils.setField(progress, "id", "progress-1");
    }

    @Test
    void submitQuizAttemptReturnsEmptyWhenQuizDoesNotExist() {
        when(quizService.getQuizById("quiz-1")).thenReturn(Optional.empty());

        Optional<QuizAttempt> result = quizAttemptService.submitQuizAttempt(dto);

        assertTrue(result.isEmpty());

        verify(lessonProgressService, never()).getByEnrollmentIdAndLessonId(any(), any());
        verify(quizAttemptRepository, never()).save(any());
        verify(lessonProgressService, never()).save(any());
        verify(lessonProgressService, never()).markPassed(any());
    }

    @Test
    void submitQuizAttemptReturnsEmptyWhenProgressDoesNotExist() {
        when(quizService.getQuizById("quiz-1")).thenReturn(Optional.of(quiz));
        when(lessonProgressService.getByEnrollmentIdAndLessonId("enrollment-1", "lesson-1"))
                .thenReturn(Optional.empty());

        Optional<QuizAttempt> result = quizAttemptService.submitQuizAttempt(dto);

        assertTrue(result.isEmpty());

        verify(quizAttemptRepository, never()).save(any());
        verify(lessonProgressService, never()).save(any());
        verify(lessonProgressService, never()).markPassed(any());
    }

    @Test
    void submitQuizAttemptReturnsEmptyWhenProgressStateIsNotMeetingDone() {
        progress.setState(LessonProgressState.MATERIAL_DONE);

        when(quizService.getQuizById("quiz-1")).thenReturn(Optional.of(quiz));
        when(lessonProgressService.getByEnrollmentIdAndLessonId("enrollment-1", "lesson-1"))
                .thenReturn(Optional.of(progress));

        Optional<QuizAttempt> result = quizAttemptService.submitQuizAttempt(dto);

        assertTrue(result.isEmpty());

        verify(quizAttemptRepository, never()).save(any());
        verify(lessonProgressService, never()).save(any());
        verify(lessonProgressService, never()).markPassed(any());
    }

    @Test
    void submitQuizAttemptSavesFailedAttemptAndDoesNotMarkPassed() {
        ReflectionTestUtils.setField(dto, "scorePercent", 50.0);

        when(quizService.getQuizById("quiz-1")).thenReturn(Optional.of(quiz));
        when(lessonProgressService.getByEnrollmentIdAndLessonId("enrollment-1", "lesson-1"))
                .thenReturn(Optional.of(progress));
        when(quizAttemptRepository.save(any(QuizAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<QuizAttempt> result = quizAttemptService.submitQuizAttempt(dto);

        assertTrue(result.isPresent());

        QuizAttempt savedAttempt = result.get();

        assertEquals("quiz-1", savedAttempt.getQuizId());
        assertEquals("enrollment-1", savedAttempt.getEnrollmentId());
        assertEquals("lesson-1", savedAttempt.getLessonId());
        assertEquals(50.0, savedAttempt.getScorePercent());
        assertFalse(savedAttempt.isPassed());

        assertEquals(1, progress.getQuizAttemptsCount());

        verify(quizAttemptRepository).save(any(QuizAttempt.class));
        verify(lessonProgressService).save(progress);
        verify(lessonProgressService, never()).markPassed(any());
    }

    @Test
    void submitQuizAttemptSavesPassedAttemptAndMarksProgressPassed() {
        ReflectionTestUtils.setField(dto, "scorePercent", 80.0);

        when(quizService.getQuizById("quiz-1")).thenReturn(Optional.of(quiz));
        when(lessonProgressService.getByEnrollmentIdAndLessonId("enrollment-1", "lesson-1"))
                .thenReturn(Optional.of(progress));
        when(quizAttemptRepository.save(any(QuizAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<QuizAttempt> result = quizAttemptService.submitQuizAttempt(dto);

        assertTrue(result.isPresent());

        QuizAttempt savedAttempt = result.get();

        assertEquals("quiz-1", savedAttempt.getQuizId());
        assertEquals("enrollment-1", savedAttempt.getEnrollmentId());
        assertEquals("lesson-1", savedAttempt.getLessonId());
        assertEquals(80.0, savedAttempt.getScorePercent());
        assertTrue(savedAttempt.isPassed());

        assertEquals(1, progress.getQuizAttemptsCount());

        verify(quizAttemptRepository).save(any(QuizAttempt.class));
        verify(lessonProgressService).save(progress);
        verify(lessonProgressService).markPassed("progress-1");
    }
}