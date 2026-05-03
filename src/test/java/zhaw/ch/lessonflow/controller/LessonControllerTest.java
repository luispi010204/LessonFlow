package zhaw.ch.lessonflow.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import zhaw.ch.lessonflow.model.LessonCreateDTO;
import zhaw.ch.lessonflow.repository.LessonRepository;
import zhaw.ch.lessonflow.services.CourseService;
import zhaw.ch.lessonflow.services.UserService;

@ExtendWith(MockitoExtension.class)
public class LessonControllerTest {

    @Mock
    LessonRepository lessonRepository;

    @Mock
    CourseService courseService;

    @Mock
    UserService userService;

    @InjectMocks
    LessonController lessonController;

    Lesson lesson;
    LessonCreateDTO lessonCreateDTO;

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

        lessonCreateDTO = new LessonCreateDTO();
        ReflectionTestUtils.setField(lessonCreateDTO, "courseId", "course-1");
        ReflectionTestUtils.setField(lessonCreateDTO, "lessonNumber", 1);
        ReflectionTestUtils.setField(lessonCreateDTO, "title", "Understanding Rhythm");
        ReflectionTestUtils.setField(lessonCreateDTO, "material", "Learn about beats, tempo and note values.");
        ReflectionTestUtils.setField(lessonCreateDTO, "meetingLink", "https://meeting.example.com");
    }

    @Test
    void shouldCreateLessonWhenCourseExistsBelongsToTutorAndLessonNumberIsFree() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(courseService.courseExists("course-1")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(true);
        when(lessonRepository.findByCourseIdAndLessonNumber("course-1", 1))
                .thenReturn(Optional.empty());
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson savedLesson = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedLesson, "id", "lesson-1");
            return savedLesson;
        });

        ResponseEntity<Lesson> response = lessonController.createLesson(lessonCreateDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("lesson-1", response.getBody().getId());
        assertEquals("course-1", response.getBody().getCourseId());
        assertEquals(1, response.getBody().getLessonNumber());
        assertEquals("Understanding Rhythm", response.getBody().getTitle());
        assertEquals("Learn about beats, tempo and note values.", response.getBody().getMaterial());
        assertEquals("https://meeting.example.com", response.getBody().getMeetingLink());

        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    void shouldNotCreateLessonWhenUserIsNotTutor() {
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<Lesson> response = lessonController.createLesson(lessonCreateDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(courseService, never()).courseExists(any());
        verify(courseService, never()).courseBelongsToTutor(any(), any());
        verify(lessonRepository, never()).findByCourseIdAndLessonNumber(any(), anyInt());
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void shouldNotCreateLessonWhenCourseDoesNotExist() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(courseService.courseExists("course-1")).thenReturn(false);

        ResponseEntity<Lesson> response = lessonController.createLesson(lessonCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(courseService, never()).courseBelongsToTutor(any(), any());
        verify(lessonRepository, never()).findByCourseIdAndLessonNumber(any(), anyInt());
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void shouldNotCreateLessonWhenCourseDoesNotBelongToTutor() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(courseService.courseExists("course-1")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(false);

        ResponseEntity<Lesson> response = lessonController.createLesson(lessonCreateDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(lessonRepository, never()).findByCourseIdAndLessonNumber(any(), anyInt());
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void shouldNotCreateLessonWhenLessonNumberAlreadyExists() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(courseService.courseExists("course-1")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(true);
        when(lessonRepository.findByCourseIdAndLessonNumber("course-1", 1))
                .thenReturn(Optional.of(lesson));

        ResponseEntity<Lesson> response = lessonController.createLesson(lessonCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void shouldReturnAllLessons() {
        when(lessonRepository.findAll()).thenReturn(List.of(lesson));

        List<Lesson> result = lessonController.getAllLessons();

        assertEquals(1, result.size());
        assertEquals("lesson-1", result.get(0).getId());
        assertEquals("Understanding Rhythm", result.get(0).getTitle());
    }

    @Test
    void shouldReturnLessonByIdWhenLessonExists() {
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));

        ResponseEntity<Lesson> response = lessonController.getLessonById("lesson-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("lesson-1", response.getBody().getId());
        assertEquals("Understanding Rhythm", response.getBody().getTitle());
    }

    @Test
    void shouldReturnNotFoundWhenLessonDoesNotExist() {
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.empty());

        ResponseEntity<Lesson> response = lessonController.getLessonById("lesson-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnLessonsByCourseId() {
        when(lessonRepository.findByCourseIdOrderByLessonNumberAsc("course-1"))
                .thenReturn(List.of(lesson));

        List<Lesson> result = lessonController.getLessonsByCourseId("course-1");

        assertEquals(1, result.size());
        assertEquals("lesson-1", result.get(0).getId());
        assertEquals("course-1", result.get(0).getCourseId());
    }
}