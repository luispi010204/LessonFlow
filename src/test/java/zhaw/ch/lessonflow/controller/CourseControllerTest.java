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

    Course draftCourse;
    Course publishedCourse;
    CourseCreateDTO courseCreateDTO;
    CourseCreateDTO courseUpdateDTO;

    @BeforeEach
    void setUp() {
        draftCourse = new Course(
                "auth0|tutor-1",
                "Music Basics",
                "Learn the basics of rhythm and melody.",
                CourseStatus.DRAFT);
        ReflectionTestUtils.setField(draftCourse, "id", "course-1");
        draftCourse.setTutorEmail("tutor@lessonflow.com");

        publishedCourse = new Course(
                "auth0|tutor-1",
                "Published Music Basics",
                "A published course.",
                CourseStatus.PUBLISHED);
        ReflectionTestUtils.setField(publishedCourse, "id", "course-2");
        publishedCourse.setTutorEmail("tutor@lessonflow.com");

        courseCreateDTO = new CourseCreateDTO();
        ReflectionTestUtils.setField(courseCreateDTO, "title", "Music Basics");
        ReflectionTestUtils.setField(courseCreateDTO, "description", "Learn the basics of rhythm and melody.");

        courseUpdateDTO = new CourseCreateDTO();
        ReflectionTestUtils.setField(courseUpdateDTO, "title", "Updated Music Basics");
        ReflectionTestUtils.setField(courseUpdateDTO, "description", "Updated course description.");
    }

    @Test
    void shouldCreateCourseWhenUserIsTutor() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(userService.getEmail()).thenReturn("tutor@lessonflow.com");
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
        assertEquals("tutor@lessonflow.com", response.getBody().getTutorEmail());
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
    void shouldNotCreateCourseWhenTitleIsBlank() {
        ReflectionTestUtils.setField(courseCreateDTO, "title", "");

        when(userService.userHasRole("tutor")).thenReturn(true);

        ResponseEntity<Course> response = courseController.createCourse(courseCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(userService, never()).getCurrentUserId();
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void shouldNotCreateCourseWhenDescriptionIsBlank() {
        ReflectionTestUtils.setField(courseCreateDTO, "description", "");

        when(userService.userHasRole("tutor")).thenReturn(true);

        ResponseEntity<Course> response = courseController.createCourse(courseCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(userService, never()).getCurrentUserId();
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void shouldReturnOnlyPublishedCourses() {
        when(courseRepository.findByStatus(CourseStatus.PUBLISHED)).thenReturn(List.of(publishedCourse));

        List<Course> result = courseController.getAllCourses();

        assertEquals(1, result.size());
        assertEquals("course-2", result.get(0).getId());
        assertEquals("Published Music Basics", result.get(0).getTitle());
        assertEquals(CourseStatus.PUBLISHED, result.get(0).getStatus());

        verify(courseRepository).findByStatus(CourseStatus.PUBLISHED);
        verify(courseRepository, never()).findAll();
    }

    @Test
    void shouldReturnMyCoursesWhenUserIsTutor() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseRepository.findByTutorUserId("auth0|tutor-1")).thenReturn(List.of(draftCourse, publishedCourse));

        ResponseEntity<List<Course>> response = courseController.getMyCourses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(2, response.getBody().size());
        assertEquals("course-1", response.getBody().get(0).getId());
        assertEquals("course-2", response.getBody().get(1).getId());

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
    void shouldReturnPublishedCourseByIdWhenCourseExists() {
        when(courseRepository.findById("course-2")).thenReturn(Optional.of(publishedCourse));

        ResponseEntity<Course> response = courseController.getCourseById("course-2");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("course-2", response.getBody().getId());
        assertEquals("Published Music Basics", response.getBody().getTitle());
        assertEquals(CourseStatus.PUBLISHED, response.getBody().getStatus());
    }

    @Test
    void shouldReturnDraftCourseByIdWhenTutorOwnsCourse() {
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(draftCourse));
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");

        ResponseEntity<Course> response = courseController.getCourseById("course-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("course-1", response.getBody().getId());
        assertEquals(CourseStatus.DRAFT, response.getBody().getStatus());
    }

    @Test
    void shouldReturnForbiddenWhenDraftCourseDoesNotBelongToCurrentTutor() {
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(draftCourse));
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|other-tutor");

        ResponseEntity<Course> response = courseController.getCourseById("course-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void shouldReturnForbiddenWhenLearnerRequestsDraftCourseById() {
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(draftCourse));
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<Course> response = courseController.getCourseById("course-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void shouldReturnNotFoundWhenCourseDoesNotExist() {
        when(courseRepository.findById("course-1")).thenReturn(Optional.empty());

        ResponseEntity<Course> response = courseController.getCourseById("course-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldUpdateCourseWhenTutorOwnsCourse() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(draftCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Course> response = courseController.updateCourse("course-1", courseUpdateDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("course-1", response.getBody().getId());
        assertEquals("Updated Music Basics", response.getBody().getTitle());
        assertEquals("Updated course description.", response.getBody().getDescription());
        assertEquals(CourseStatus.DRAFT, response.getBody().getStatus());

        verify(courseRepository).save(draftCourse);
    }

    @Test
    void shouldNotUpdateCourseWhenUserIsNotTutor() {
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<Course> response = courseController.updateCourse("course-1", courseUpdateDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(courseRepository, never()).findById(any());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingCourseThatDoesNotExist() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(courseRepository.findById("course-1")).thenReturn(Optional.empty());

        ResponseEntity<Course> response = courseController.updateCourse("course-1", courseUpdateDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void shouldReturnForbiddenWhenUpdatingCourseOwnedByAnotherTutor() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|other-tutor");
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(draftCourse));

        ResponseEntity<Course> response = courseController.updateCourse("course-1", courseUpdateDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingCourseWithBlankTitle() {
        ReflectionTestUtils.setField(courseUpdateDTO, "title", "");

        when(userService.userHasRole("tutor")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(draftCourse));

        ResponseEntity<Course> response = courseController.updateCourse("course-1", courseUpdateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void shouldPublishCourseWhenTutorOwnsCourse() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(draftCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Course> response = courseController.publishCourse("course-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("course-1", response.getBody().getId());
        assertEquals(CourseStatus.PUBLISHED, response.getBody().getStatus());

        verify(courseRepository).save(draftCourse);
    }

    @Test
    void shouldNotPublishCourseWhenUserIsNotTutor() {
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<Course> response = courseController.publishCourse("course-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(courseRepository, never()).findById(any());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void shouldReturnNotFoundWhenPublishingCourseThatDoesNotExist() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(courseRepository.findById("course-1")).thenReturn(Optional.empty());

        ResponseEntity<Course> response = courseController.publishCourse("course-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void shouldReturnForbiddenWhenPublishingCourseOwnedByAnotherTutor() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|other-tutor");
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(draftCourse));

        ResponseEntity<Course> response = courseController.publishCourse("course-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(courseRepository, never()).save(any(Course.class));
    }
}