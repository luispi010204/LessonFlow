package zhaw.ch.lessonflow.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

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
import zhaw.ch.lessonflow.model.CourseCreateDTO;
import zhaw.ch.lessonflow.model.CourseStatus;
import zhaw.ch.lessonflow.repository.CourseRepository;
import zhaw.ch.lessonflow.services.UserService;

@ExtendWith(MockitoExtension.class)
public class CourseControllerTest {

    @Mock
    CourseRepository courseRepository;

    @Mock
    UserService userService;

    @InjectMocks
    CourseController courseController;

    Course course;
    CourseCreateDTO courseCreateDTO;

    @BeforeEach
    void setUp() {
        course = new Course(
                "auth0|tutor-1",
                "Music Basics",
                "Learn the basics of rhythm and melody.",
                CourseStatus.DRAFT
        );
        ReflectionTestUtils.setField(course, "id", "course-1");

        courseCreateDTO = new CourseCreateDTO();
        ReflectionTestUtils.setField(courseCreateDTO, "title", "Music Basics");
        ReflectionTestUtils.setField(courseCreateDTO, "description", "Learn the basics of rhythm and melody.");
    }

    @Test
    void shouldCreateCourseWhenUserIsTutor() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course savedCourse = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedCourse, "id", "course-1");
            return savedCourse;
        });

        ResponseEntity<Course> response = courseController.createCourse(courseCreateDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("course-1", response.getBody().getId());
        assertEquals("Music Basics", response.getBody().getTitle());
        assertEquals("Learn the basics of rhythm and melody.", response.getBody().getDescription());
        assertEquals("auth0|tutor-1", response.getBody().getTutorUserId());
        assertEquals(CourseStatus.DRAFT, response.getBody().getStatus());

        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void shouldNotCreateCourseWhenUserIsNotTutor() {
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<Course> response = courseController.createCourse(courseCreateDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void shouldReturnAllCourses() {
        when(courseRepository.findAll()).thenReturn(List.of(course));

        List<Course> result = courseController.getAllCourses();

        assertEquals(1, result.size());
        assertEquals("course-1", result.get(0).getId());
        assertEquals("Music Basics", result.get(0).getTitle());
    }

    @Test
    void shouldReturnMyCoursesWhenUserIsTutor() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseRepository.findByTutorUserId("auth0|tutor-1")).thenReturn(List.of(course));

        ResponseEntity<List<Course>> response = courseController.getMyCourses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(1, response.getBody().size());
        assertEquals("course-1", response.getBody().get(0).getId());

        verify(courseRepository).findByTutorUserId("auth0|tutor-1");
    }

    @Test
    void shouldNotReturnMyCoursesWhenUserIsNotTutor() {
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<List<Course>> response = courseController.getMyCourses();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(courseRepository, never()).findByTutorUserId(any());
    }

    @Test
    void shouldReturnCourseByIdWhenCourseExists() {
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        ResponseEntity<Course> response = courseController.getCourseById("course-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("course-1", response.getBody().getId());
        assertEquals("Music Basics", response.getBody().getTitle());
    }

    @Test
    void shouldReturnNotFoundWhenCourseDoesNotExist() {
        when(courseRepository.findById("course-1")).thenReturn(Optional.empty());

        ResponseEntity<Course> response = courseController.getCourseById("course-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}