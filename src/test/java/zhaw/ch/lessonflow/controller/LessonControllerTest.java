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

import zhaw.ch.lessonflow.model.Course;
import zhaw.ch.lessonflow.model.CourseStatus;
import zhaw.ch.lessonflow.model.Enrollment;
import zhaw.ch.lessonflow.model.Lesson;
import zhaw.ch.lessonflow.model.LessonCreateDTO;
import zhaw.ch.lessonflow.repository.CourseRepository;
import zhaw.ch.lessonflow.repository.EnrollmentRepository;
import zhaw.ch.lessonflow.repository.LessonRepository;
import zhaw.ch.lessonflow.services.CourseService;
import zhaw.ch.lessonflow.services.UserService;

@ExtendWith(MockitoExtension.class)
public class LessonControllerTest {

    @Mock
    LessonRepository lessonRepository;

    @Mock
    CourseRepository courseRepository;

    @Mock
    EnrollmentRepository enrollmentRepository;

    @Mock
    CourseService courseService;

    @Mock
    UserService userService;

    @InjectMocks
    LessonController lessonController;

    Lesson lesson;
    LessonCreateDTO lessonCreateDTO;
    LessonCreateDTO lessonUpdateDTO;
    Course course;
    Enrollment enrollment;

    @BeforeEach
    void setUp() {
        lesson = new Lesson(
                "course-1",
                1,
                "Understanding Rhythm",
                "Learn about beats, tempo and note values.",
                "https://meeting.example.com");
        ReflectionTestUtils.setField(lesson, "id", "lesson-1");

        lessonCreateDTO = new LessonCreateDTO();
        ReflectionTestUtils.setField(lessonCreateDTO, "courseId", "course-1");
        ReflectionTestUtils.setField(lessonCreateDTO, "lessonNumber", 1);
        ReflectionTestUtils.setField(lessonCreateDTO, "title", "Understanding Rhythm");
        ReflectionTestUtils.setField(lessonCreateDTO, "material", "Learn about beats, tempo and note values.");
        ReflectionTestUtils.setField(lessonCreateDTO, "meetingLink", "https://meeting.example.com");

        lessonUpdateDTO = new LessonCreateDTO();
        ReflectionTestUtils.setField(lessonUpdateDTO, "courseId", "ignored-course-id");
        ReflectionTestUtils.setField(lessonUpdateDTO, "lessonNumber", 99);
        ReflectionTestUtils.setField(lessonUpdateDTO, "title", "Updated Rhythm");
        ReflectionTestUtils.setField(lessonUpdateDTO, "material", "Updated lesson material.");
        ReflectionTestUtils.setField(lessonUpdateDTO, "meetingLink", "https://updated-meeting.example.com");

        course = new Course(
                "auth0|tutor-1",
                "Music Basics",
                "Learn the basics of rhythm and melody.",
                CourseStatus.PUBLISHED);
        ReflectionTestUtils.setField(course, "id", "course-1");

        enrollment = new Enrollment(
                "course-1",
                "auth0|learner-1",
                "ENROLLED");
        ReflectionTestUtils.setField(enrollment, "id", "enrollment-1");
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
    void shouldNotCreateLessonWhenTitleIsBlank() {
        ReflectionTestUtils.setField(lessonCreateDTO, "title", "");

        when(userService.userHasRole("tutor")).thenReturn(true);

        ResponseEntity<Lesson> response = lessonController.createLesson(lessonCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(courseService, never()).courseExists(any());
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void shouldNotCreateLessonWhenCourseIdIsBlank() {
        ReflectionTestUtils.setField(lessonCreateDTO, "courseId", "");

        when(userService.userHasRole("tutor")).thenReturn(true);

        ResponseEntity<Lesson> response = lessonController.createLesson(lessonCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(courseService, never()).courseExists(any());
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void shouldNotCreateLessonWhenLessonNumberIsInvalid() {
        ReflectionTestUtils.setField(lessonCreateDTO, "lessonNumber", 0);

        when(userService.userHasRole("tutor")).thenReturn(true);

        ResponseEntity<Lesson> response = lessonController.createLesson(lessonCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(courseService, never()).courseExists(any());
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
    void shouldUpdateLessonWhenTutorOwnsCourse() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(true);
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Lesson> response = lessonController.updateLesson("lesson-1", lessonUpdateDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("lesson-1", response.getBody().getId());
        assertEquals("course-1", response.getBody().getCourseId());
        assertEquals(1, response.getBody().getLessonNumber());
        assertEquals("Updated Rhythm", response.getBody().getTitle());
        assertEquals("Updated lesson material.", response.getBody().getMaterial());
        assertEquals("https://updated-meeting.example.com", response.getBody().getMeetingLink());

        verify(lessonRepository).save(lesson);
    }

    @Test
    void shouldNotUpdateLessonWhenUserIsNotTutor() {
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<Lesson> response = lessonController.updateLesson("lesson-1", lessonUpdateDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(lessonRepository, never()).findById(any());
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingLessonThatDoesNotExist() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.empty());

        ResponseEntity<Lesson> response = lessonController.updateLesson("lesson-1", lessonUpdateDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingLessonWithBlankMaterial() {
        ReflectionTestUtils.setField(lessonUpdateDTO, "material", "");

        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));

        ResponseEntity<Lesson> response = lessonController.updateLesson("lesson-1", lessonUpdateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(courseService, never()).courseBelongsToTutor(any(), any());
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void shouldReturnForbiddenWhenUpdatingLessonOfAnotherTutorsCourse() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUserId()).thenReturn("auth0|other-tutor");
        when(courseService.courseBelongsToTutor("course-1", "auth0|other-tutor")).thenReturn(false);

        ResponseEntity<Lesson> response = lessonController.updateLesson("lesson-1", lessonUpdateDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void shouldReturnAllLessonsForTutorCoursesWhenUserIsTutor() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseRepository.findByTutorUserId("auth0|tutor-1")).thenReturn(List.of(course));
        when(lessonRepository.findByCourseIdOrderByLessonNumberAsc("course-1")).thenReturn(List.of(lesson));

        ResponseEntity<List<Lesson>> response = lessonController.getAllLessons();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(1, response.getBody().size());
        assertEquals("lesson-1", response.getBody().get(0).getId());
        assertEquals("Understanding Rhythm", response.getBody().get(0).getTitle());

        verify(courseRepository).findByTutorUserId("auth0|tutor-1");
        verify(lessonRepository).findByCourseIdOrderByLessonNumberAsc("course-1");
        verify(lessonRepository, never()).findAll();
    }

    @Test
    void shouldNotReturnAllLessonsWhenUserIsNotTutor() {
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<List<Lesson>> response = lessonController.getAllLessons();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(courseRepository, never()).findByTutorUserId(any());
        verify(lessonRepository, never()).findAll();
    }

    @Test
    void shouldReturnLessonByIdWhenTutorOwnsCourse() {
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(true);

        ResponseEntity<Lesson> response = lessonController.getLessonById("lesson-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("lesson-1", response.getBody().getId());
        assertEquals("Understanding Rhythm", response.getBody().getTitle());
    }

    @Test
    void shouldReturnLessonByIdWhenLearnerIsEnrolledInCourse() {
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(userService.userHasRole("tutor")).thenReturn(false);
        when(userService.userHasRole("learner")).thenReturn(true);
        when(enrollmentRepository.findByLearnerUserId("auth0|learner-1")).thenReturn(List.of(enrollment));

        ResponseEntity<Lesson> response = lessonController.getLessonById("lesson-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("lesson-1", response.getBody().getId());
        assertEquals("Understanding Rhythm", response.getBody().getTitle());
    }

    @Test
    void shouldReturnForbiddenWhenLearnerIsNotEnrolledInLessonCourse() {
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-2");
        when(userService.userHasRole("tutor")).thenReturn(false);
        when(userService.userHasRole("learner")).thenReturn(true);
        when(enrollmentRepository.findByLearnerUserId("auth0|learner-2")).thenReturn(List.of());

        ResponseEntity<Lesson> response = lessonController.getLessonById("lesson-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void shouldReturnForbiddenWhenTutorDoesNotOwnLessonCourse() {
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUserId()).thenReturn("auth0|other-tutor");
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(courseService.courseBelongsToTutor("course-1", "auth0|other-tutor")).thenReturn(false);
        when(userService.userHasRole("learner")).thenReturn(false);

        ResponseEntity<Lesson> response = lessonController.getLessonById("lesson-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void shouldReturnNotFoundWhenLessonDoesNotExist() {
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.empty());

        ResponseEntity<Lesson> response = lessonController.getLessonById("lesson-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnLessonsByCourseIdWhenTutorOwnsCourse() {
        when(courseService.courseExists("course-1")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(true);
        when(lessonRepository.findByCourseIdOrderByLessonNumberAsc("course-1"))
                .thenReturn(List.of(lesson));

        ResponseEntity<List<Lesson>> response = lessonController.getLessonsByCourseId("course-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(1, response.getBody().size());
        assertEquals("lesson-1", response.getBody().get(0).getId());
        assertEquals("course-1", response.getBody().get(0).getCourseId());
    }

    @Test
    void shouldReturnLessonsByCourseIdWhenLearnerIsEnrolled() {
        when(courseService.courseExists("course-1")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(userService.userHasRole("tutor")).thenReturn(false);
        when(userService.userHasRole("learner")).thenReturn(true);
        when(enrollmentRepository.findByLearnerUserId("auth0|learner-1")).thenReturn(List.of(enrollment));
        when(lessonRepository.findByCourseIdOrderByLessonNumberAsc("course-1"))
                .thenReturn(List.of(lesson));

        ResponseEntity<List<Lesson>> response = lessonController.getLessonsByCourseId("course-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(1, response.getBody().size());
        assertEquals("lesson-1", response.getBody().get(0).getId());
        assertEquals("course-1", response.getBody().get(0).getCourseId());
    }

    @Test
    void shouldReturnNotFoundWhenCourseDoesNotExistForLessonsByCourseId() {
        when(courseService.courseExists("course-1")).thenReturn(false);

        ResponseEntity<List<Lesson>> response = lessonController.getLessonsByCourseId("course-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(lessonRepository, never()).findByCourseIdOrderByLessonNumberAsc(any());
    }

    @Test
    void shouldReturnForbiddenWhenUserCannotAccessLessonsByCourseId() {
        when(courseService.courseExists("course-1")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|other-user");
        when(userService.userHasRole("tutor")).thenReturn(false);
        when(userService.userHasRole("learner")).thenReturn(false);

        ResponseEntity<List<Lesson>> response = lessonController.getLessonsByCourseId("course-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(lessonRepository, never()).findByCourseIdOrderByLessonNumberAsc(any());
    }
}