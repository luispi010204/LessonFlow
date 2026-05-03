package zhaw.ch.lessonflow.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

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

@ExtendWith(MockitoExtension.class)
public class QuizAttemptControllerTest {

    @Mock
    QuizAttemptRepository quizAttemptRepository;

    @Mock
    QuizAttemptService quizAttemptService;

    @Mock
    QuizRepository quizRepository;

    @Mock
    EnrollmentService enrollmentService;

    @Mock
    LessonService lessonService;

    @Mock
    CourseService courseService;

    @Mock
    UserService userService;

    @InjectMocks
    QuizAttemptController quizAttemptController;

    QuizAttempt quizAttempt;
    QuizAttemptCreateDTO quizAttemptCreateDTO;
    Enrollment enrollment;
    Lesson lesson;
    Quiz quiz;

    @BeforeEach
    void setUp() {
        quizAttempt = new QuizAttempt(
                "quiz-1",
                "enrollment-1",
                "lesson-1",
                80.0,
                true
        );
        ReflectionTestUtils.setField(quizAttempt, "id", "attempt-1");

        quizAttemptCreateDTO = new QuizAttemptCreateDTO();
        ReflectionTestUtils.setField(quizAttemptCreateDTO, "quizId", "quiz-1");
        ReflectionTestUtils.setField(quizAttemptCreateDTO, "enrollmentId", "enrollment-1");
        ReflectionTestUtils.setField(quizAttemptCreateDTO, "lessonId", "lesson-1");
        ReflectionTestUtils.setField(quizAttemptCreateDTO, "scorePercent", 80.0);

        enrollment = new Enrollment(
                "course-1",
                "auth0|learner-1",
                "ENROLLED"
        );
        ReflectionTestUtils.setField(enrollment, "id", "enrollment-1");

        lesson = new Lesson(
                "course-1",
                1,
                "Understanding Rhythm",
                "Learn about beats, tempo and note values.",
                "https://meeting.example.com"
        );
        ReflectionTestUtils.setField(lesson, "id", "lesson-1");

        quiz = new Quiz(
                "lesson-1",
                70,
                List.of("Question 1", "Question 2")
        );
        ReflectionTestUtils.setField(quiz, "id", "quiz-1");
    }

    @Test
    void shouldReturnQuizAttemptByIdWhenLearnerOwnsEnrollment() {
        when(quizAttemptRepository.findById("attempt-1")).thenReturn(Optional.of(quizAttempt));
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(userService.userHasRole("learner")).thenReturn(true);

        ResponseEntity<QuizAttempt> response = quizAttemptController.getQuizAttemptById("attempt-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("attempt-1", response.getBody().getId());
        assertEquals("quiz-1", response.getBody().getQuizId());
        assertEquals("enrollment-1", response.getBody().getEnrollmentId());
    }

    @Test
    void shouldReturnQuizAttemptByIdWhenTutorOwnsCourse() {
        when(quizAttemptRepository.findById("attempt-1")).thenReturn(Optional.of(quizAttempt));
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(userService.userHasRole("learner")).thenReturn(false);
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(true);

        ResponseEntity<QuizAttempt> response = quizAttemptController.getQuizAttemptById("attempt-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("attempt-1", response.getBody().getId());
    }

    @Test
    void shouldReturnNotFoundWhenQuizAttemptDoesNotExist() {
        when(quizAttemptRepository.findById("attempt-1")).thenReturn(Optional.empty());

        ResponseEntity<QuizAttempt> response = quizAttemptController.getQuizAttemptById("attempt-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(enrollmentService, never()).getEnrollmentById(anyString());
    }

    @Test
    void shouldReturnForbiddenWhenUserCannotAccessQuizAttemptEnrollment() {
        when(quizAttemptRepository.findById("attempt-1")).thenReturn(Optional.of(quizAttempt));
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|other-user");
        when(userService.userHasRole("learner")).thenReturn(false);
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<QuizAttempt> response = quizAttemptController.getQuizAttemptById("attempt-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void shouldReturnQuizAttemptsByQuizIdWhenTutorCanAccessLesson() {
        when(quizRepository.findById("quiz-1")).thenReturn(Optional.of(quiz));
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(true);
        when(quizAttemptRepository.findByQuizId("quiz-1")).thenReturn(List.of(quizAttempt));

        ResponseEntity<List<QuizAttempt>> response = quizAttemptController.getQuizAttemptsByQuizId("quiz-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(1, response.getBody().size());
        assertEquals("attempt-1", response.getBody().get(0).getId());
        assertEquals("quiz-1", response.getBody().get(0).getQuizId());
    }

    @Test
    void shouldReturnNotFoundWhenQuizDoesNotExistForQuizAttemptsByQuizId() {
        when(quizRepository.findById("quiz-1")).thenReturn(Optional.empty());

        ResponseEntity<List<QuizAttempt>> response = quizAttemptController.getQuizAttemptsByQuizId("quiz-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(quizAttemptRepository, never()).findByQuizId(anyString());
    }

    @Test
    void shouldReturnForbiddenWhenTutorCannotAccessQuizLesson() {
        when(quizRepository.findById("quiz-1")).thenReturn(Optional.of(quiz));
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<List<QuizAttempt>> response = quizAttemptController.getQuizAttemptsByQuizId("quiz-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(quizAttemptRepository, never()).findByQuizId(anyString());
    }

    @Test
    void shouldReturnQuizAttemptsByEnrollmentIdWhenLearnerOwnsEnrollment() {
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(userService.userHasRole("learner")).thenReturn(true);
        when(quizAttemptRepository.findByEnrollmentId("enrollment-1")).thenReturn(List.of(quizAttempt));

        ResponseEntity<List<QuizAttempt>> response =
                quizAttemptController.getQuizAttemptsByEnrollmentId("enrollment-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(1, response.getBody().size());
        assertEquals("enrollment-1", response.getBody().get(0).getEnrollmentId());
    }

    @Test
    void shouldReturnNotFoundWhenEnrollmentDoesNotExistForAttemptsByEnrollmentId() {
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.empty());

        ResponseEntity<List<QuizAttempt>> response =
                quizAttemptController.getQuizAttemptsByEnrollmentId("enrollment-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(quizAttemptRepository, never()).findByEnrollmentId(anyString());
    }

    @Test
    void shouldReturnForbiddenWhenUserCannotAccessEnrollmentAttempts() {
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|other-user");
        when(userService.userHasRole("learner")).thenReturn(false);
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<List<QuizAttempt>> response =
                quizAttemptController.getQuizAttemptsByEnrollmentId("enrollment-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(quizAttemptRepository, never()).findByEnrollmentId(anyString());
    }

    @Test
    void shouldReturnQuizAttemptsByLessonIdWhenTutorCanAccessLesson() {
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(true);
        when(quizAttemptRepository.findByLessonId("lesson-1")).thenReturn(List.of(quizAttempt));

        ResponseEntity<List<QuizAttempt>> response =
                quizAttemptController.getQuizAttemptsByLessonId("lesson-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(1, response.getBody().size());
        assertEquals("lesson-1", response.getBody().get(0).getLessonId());
    }

    @Test
    void shouldReturnNotFoundWhenLessonDoesNotExistForAttemptsByLessonId() {
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.empty());

        ResponseEntity<List<QuizAttempt>> response =
                quizAttemptController.getQuizAttemptsByLessonId("lesson-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(quizAttemptRepository, never()).findByLessonId(anyString());
    }

    @Test
    void shouldReturnForbiddenWhenTutorCannotAccessLessonAttempts() {
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<List<QuizAttempt>> response =
                quizAttemptController.getQuizAttemptsByLessonId("lesson-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(quizAttemptRepository, never()).findByLessonId(anyString());
    }

    @Test
    void shouldSubmitQuizAttemptWhenLearnerOwnsEnrollmentAndServiceReturnsAttempt() {
        when(userService.userHasRole("learner")).thenReturn(true);
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(enrollmentService.enrollmentBelongsToLearner("enrollment-1", "auth0|learner-1"))
                .thenReturn(true);
        when(quizAttemptService.submitQuizAttempt(any(QuizAttemptCreateDTO.class)))
                .thenReturn(Optional.of(quizAttempt));

        ResponseEntity<QuizAttempt> response =
                quizAttemptController.submitQuizAttempt(quizAttemptCreateDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("attempt-1", response.getBody().getId());
        assertEquals(80.0, response.getBody().getScorePercent());
        assertTrue(response.getBody().isPassed());
    }

    @Test
    void shouldReturnForbiddenWhenSubmitterIsNotLearner() {
        when(userService.userHasRole("learner")).thenReturn(false);

        ResponseEntity<QuizAttempt> response =
                quizAttemptController.submitQuizAttempt(quizAttemptCreateDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(enrollmentService, never()).getEnrollmentById(anyString());
        verify(quizAttemptService, never()).submitQuizAttempt(any(QuizAttemptCreateDTO.class));
    }

    @Test
    void shouldReturnBadRequestWhenSubmitEnrollmentDoesNotExist() {
        when(userService.userHasRole("learner")).thenReturn(true);
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.empty());

        ResponseEntity<QuizAttempt> response =
                quizAttemptController.submitQuizAttempt(quizAttemptCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(quizAttemptService, never()).submitQuizAttempt(any(QuizAttemptCreateDTO.class));
    }

    @Test
    void shouldReturnForbiddenWhenSubmitEnrollmentDoesNotBelongToLearner() {
        when(userService.userHasRole("learner")).thenReturn(true);
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|other-learner");
        when(enrollmentService.enrollmentBelongsToLearner("enrollment-1", "auth0|other-learner"))
                .thenReturn(false);

        ResponseEntity<QuizAttempt> response =
                quizAttemptController.submitQuizAttempt(quizAttemptCreateDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(quizAttemptService, never()).submitQuizAttempt(any(QuizAttemptCreateDTO.class));
    }

    @Test
    void shouldReturnBadRequestWhenSubmitQuizAttemptServiceReturnsEmpty() {
        when(userService.userHasRole("learner")).thenReturn(true);
        when(enrollmentService.getEnrollmentById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(enrollmentService.enrollmentBelongsToLearner("enrollment-1", "auth0|learner-1"))
                .thenReturn(true);
        when(quizAttemptService.submitQuizAttempt(any(QuizAttemptCreateDTO.class)))
                .thenReturn(Optional.empty());

        ResponseEntity<QuizAttempt> response =
                quizAttemptController.submitQuizAttempt(quizAttemptCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}