package zhaw.ch.lessonflow.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import zhaw.ch.lessonflow.model.Lesson;
import zhaw.ch.lessonflow.model.Quiz;
import zhaw.ch.lessonflow.model.QuizCreateDTO;
import zhaw.ch.lessonflow.model.QuizQuestion;
import zhaw.ch.lessonflow.repository.QuizRepository;
import zhaw.ch.lessonflow.services.CourseService;
import zhaw.ch.lessonflow.services.LessonService;
import zhaw.ch.lessonflow.services.UserService;

@ExtendWith(MockitoExtension.class)
public class QuizControllerTest {

    @Mock
    QuizRepository quizRepository;

    @Mock
    LessonService lessonService;

    @Mock
    UserService userService;

    @Mock
    CourseService courseService;

    @InjectMocks
    QuizController quizController;

    Lesson lesson;
    Quiz quiz;
    QuizCreateDTO quizCreateDTO;
    List<QuizQuestion> questions;

    @BeforeEach
    void setUp() {
        lesson = new Lesson(
                "course-1",
                1,
                "Understanding Rhythm",
                "Learn about beats, tempo and note values.",
                "https://meeting.example.com"
        );
        ReflectionTestUtils.setField(lesson, "id", "lesson-1");

        questions = List.of(
                new QuizQuestion(
                        "Which keyword should be used for a value that should not be reassigned?",
                        List.of("let", "const", "var", "function"),
                        1
                ),
                new QuizQuestion(
                        "What data type is used for true or false values?",
                        List.of("String", "Boolean", "Number", "Array"),
                        1
                )
        );

        quiz = new Quiz(
                "lesson-1",
                70,
                questions
        );
        ReflectionTestUtils.setField(quiz, "id", "quiz-1");

        quizCreateDTO = new QuizCreateDTO();
        ReflectionTestUtils.setField(quizCreateDTO, "lessonId", "lesson-1");
        ReflectionTestUtils.setField(quizCreateDTO, "passPercent", 70);
        ReflectionTestUtils.setField(quizCreateDTO, "questions", questions);
    }

    @Test
    void shouldCreateQuizWhenUserIsTutorLessonExistsCourseBelongsToTutorAndQuizDoesNotExist() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(true);
        when(quizRepository.findByLessonId("lesson-1")).thenReturn(Optional.empty());
        when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> {
            Quiz savedQuiz = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedQuiz, "id", "quiz-1");
            return savedQuiz;
        });

        ResponseEntity<Quiz> response = quizController.createQuiz(quizCreateDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("quiz-1", response.getBody().getId());
        assertEquals("lesson-1", response.getBody().getLessonId());
        assertEquals(70, response.getBody().getPassPercent());
        assertEquals(2, response.getBody().getQuestions().size());
        assertEquals("Which keyword should be used for a value that should not be reassigned?",
                response.getBody().getQuestions().get(0).getQuestionText());
        assertEquals(4, response.getBody().getQuestions().get(0).getOptions().size());
        assertEquals(1, response.getBody().getQuestions().get(0).getCorrectOptionIndex());

        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    void shouldNotCreateQuizWhenUserIsNotTutor() {
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<Quiz> response = quizController.createQuiz(quizCreateDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(lessonService, never()).getLessonById(any());
        verify(courseService, never()).courseBelongsToTutor(any(), any());
        verify(quizRepository, never()).findByLessonId(any());
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    void shouldNotCreateQuizWhenLessonDoesNotExist() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.empty());

        ResponseEntity<Quiz> response = quizController.createQuiz(quizCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(courseService, never()).courseBelongsToTutor(any(), any());
        verify(quizRepository, never()).findByLessonId(any());
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    void shouldNotCreateQuizWhenPassPercentIsTooLow() {
        ReflectionTestUtils.setField(quizCreateDTO, "passPercent", 0);

        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));

        ResponseEntity<Quiz> response = quizController.createQuiz(quizCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(courseService, never()).courseBelongsToTutor(any(), any());
        verify(quizRepository, never()).findByLessonId(any());
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    void shouldNotCreateQuizWhenPassPercentIsTooHigh() {
        ReflectionTestUtils.setField(quizCreateDTO, "passPercent", 101);

        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));

        ResponseEntity<Quiz> response = quizController.createQuiz(quizCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(courseService, never()).courseBelongsToTutor(any(), any());
        verify(quizRepository, never()).findByLessonId(any());
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    void shouldNotCreateQuizWhenQuestionsAreEmpty() {
        ReflectionTestUtils.setField(quizCreateDTO, "questions", List.of());

        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));

        ResponseEntity<Quiz> response = quizController.createQuiz(quizCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(courseService, never()).courseBelongsToTutor(any(), any());
        verify(quizRepository, never()).findByLessonId(any());
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    void shouldNotCreateQuizWhenQuestionTextIsBlank() {
        List<QuizQuestion> invalidQuestions = List.of(
                new QuizQuestion(
                        "",
                        List.of("let", "const", "var", "function"),
                        1
                )
        );
        ReflectionTestUtils.setField(quizCreateDTO, "questions", invalidQuestions);

        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));

        ResponseEntity<Quiz> response = quizController.createQuiz(quizCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(courseService, never()).courseBelongsToTutor(any(), any());
        verify(quizRepository, never()).findByLessonId(any());
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    void shouldNotCreateQuizWhenQuestionHasTooFewOptions() {
        List<QuizQuestion> invalidQuestions = List.of(
                new QuizQuestion(
                        "Which keyword should be used for constants?",
                        List.of("const"),
                        0
                )
        );
        ReflectionTestUtils.setField(quizCreateDTO, "questions", invalidQuestions);

        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));

        ResponseEntity<Quiz> response = quizController.createQuiz(quizCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(courseService, never()).courseBelongsToTutor(any(), any());
        verify(quizRepository, never()).findByLessonId(any());
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    void shouldNotCreateQuizWhenQuestionOptionIsBlank() {
        List<QuizQuestion> invalidQuestions = List.of(
                new QuizQuestion(
                        "Which keyword should be used for constants?",
                        List.of("let", "", "var", "function"),
                        1
                )
        );
        ReflectionTestUtils.setField(quizCreateDTO, "questions", invalidQuestions);

        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));

        ResponseEntity<Quiz> response = quizController.createQuiz(quizCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(courseService, never()).courseBelongsToTutor(any(), any());
        verify(quizRepository, never()).findByLessonId(any());
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    void shouldNotCreateQuizWhenCorrectOptionIndexIsInvalid() {
        List<QuizQuestion> invalidQuestions = List.of(
                new QuizQuestion(
                        "Which keyword should be used for constants?",
                        List.of("let", "const", "var", "function"),
                        9
                )
        );
        ReflectionTestUtils.setField(quizCreateDTO, "questions", invalidQuestions);

        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));

        ResponseEntity<Quiz> response = quizController.createQuiz(quizCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(courseService, never()).courseBelongsToTutor(any(), any());
        verify(quizRepository, never()).findByLessonId(any());
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    void shouldNotCreateQuizWhenCourseDoesNotBelongToTutor() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(false);

        ResponseEntity<Quiz> response = quizController.createQuiz(quizCreateDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(quizRepository, never()).findByLessonId(any());
        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    void shouldNotCreateQuizWhenQuizAlreadyExistsForLesson() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonService.getLessonById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(true);
        when(quizRepository.findByLessonId("lesson-1")).thenReturn(Optional.of(quiz));

        ResponseEntity<Quiz> response = quizController.createQuiz(quizCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    void shouldReturnAllQuizzes() {
        when(quizRepository.findAll()).thenReturn(List.of(quiz));

        List<Quiz> result = quizController.getAllQuizzes();

        assertEquals(1, result.size());
        assertEquals("quiz-1", result.get(0).getId());
        assertEquals("lesson-1", result.get(0).getLessonId());
        assertEquals(2, result.get(0).getQuestions().size());
    }

    @Test
    void shouldReturnQuizByIdWhenQuizExists() {
        when(quizRepository.findById("quiz-1")).thenReturn(Optional.of(quiz));

        ResponseEntity<Quiz> response = quizController.getQuizById("quiz-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("quiz-1", response.getBody().getId());
        assertEquals("lesson-1", response.getBody().getLessonId());
        assertEquals(2, response.getBody().getQuestions().size());
    }

    @Test
    void shouldReturnNotFoundWhenQuizDoesNotExist() {
        when(quizRepository.findById("quiz-1")).thenReturn(Optional.empty());

        ResponseEntity<Quiz> response = quizController.getQuizById("quiz-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnQuizByLessonIdWhenQuizExists() {
        when(quizRepository.findByLessonId("lesson-1")).thenReturn(Optional.of(quiz));

        ResponseEntity<Quiz> response = quizController.getQuizByLessonId("lesson-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("quiz-1", response.getBody().getId());
        assertEquals("lesson-1", response.getBody().getLessonId());
        assertEquals(2, response.getBody().getQuestions().size());
    }

    @Test
    void shouldReturnNotFoundWhenQuizForLessonDoesNotExist() {
        when(quizRepository.findByLessonId("lesson-1")).thenReturn(Optional.empty());

        ResponseEntity<Quiz> response = quizController.getQuizByLessonId("lesson-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}