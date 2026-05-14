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
import zhaw.ch.lessonflow.model.CourseStatus;
import zhaw.ch.lessonflow.model.Enrollment;
import zhaw.ch.lessonflow.model.EnrollmentCreateDTO;
import zhaw.ch.lessonflow.repository.CourseRepository;
import zhaw.ch.lessonflow.repository.EnrollmentRepository;
import zhaw.ch.lessonflow.services.CourseService;
import zhaw.ch.lessonflow.services.EnrollmentService;
import zhaw.ch.lessonflow.services.UserService;

@ExtendWith(MockitoExtension.class)
public class EnrollmentControllerTest {

    @Mock
    EnrollmentRepository enrollmentRepository;

    @Mock
    CourseRepository courseRepository;

    @Mock
    CourseService courseService;

    @Mock
    EnrollmentService enrollmentService;

    @Mock
    UserService userService;

    @InjectMocks
    EnrollmentController enrollmentController;

    Enrollment enrollment;
    EnrollmentCreateDTO enrollmentCreateDTO;
    Course publishedCourse;
    Course draftCourse;

    @BeforeEach
    void setUp() {
        enrollment = new Enrollment(
                "course-1",
                "auth0|learner-1",
                "ENROLLED"
        );
        ReflectionTestUtils.setField(enrollment, "id", "enrollment-1");

        enrollmentCreateDTO = new EnrollmentCreateDTO();
        ReflectionTestUtils.setField(enrollmentCreateDTO, "courseId", "course-1");

        publishedCourse = new Course(
                "auth0|tutor-1",
                "Published Course",
                "A published course.",
                CourseStatus.PUBLISHED
        );
        ReflectionTestUtils.setField(publishedCourse, "id", "course-1");

        draftCourse = new Course(
                "auth0|tutor-1",
                "Draft Course",
                "A draft course.",
                CourseStatus.DRAFT
        );
        ReflectionTestUtils.setField(draftCourse, "id", "course-2");
    }

    @Test
    void shouldCreateEnrollmentWhenUserIsLearnerCourseIsPublishedAndNotAlreadyEnrolled() {
        when(userService.userHasRole("learner")).thenReturn(true);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(publishedCourse));
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(enrollmentService.isAlreadyEnrolled("course-1", "auth0|learner-1")).thenReturn(false);
        when(enrollmentService.createEnrollmentWithProgress(any(Enrollment.class))).thenAnswer(invocation -> {
            Enrollment savedEnrollment = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedEnrollment, "id", "enrollment-1");
            return savedEnrollment;
        });

        ResponseEntity<Enrollment> response = enrollmentController.createEnrollment(enrollmentCreateDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("enrollment-1", response.getBody().getId());
        assertEquals("course-1", response.getBody().getCourseId());
        assertEquals("auth0|learner-1", response.getBody().getLearnerUserId());
        assertEquals("ENROLLED", response.getBody().getStatus());

        verify(enrollmentService).createEnrollmentWithProgress(any(Enrollment.class));
    }

    @Test
    void shouldNotCreateEnrollmentWhenUserIsNotLearner() {
        when(userService.userHasRole("learner")).thenReturn(false);

        ResponseEntity<Enrollment> response = enrollmentController.createEnrollment(enrollmentCreateDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(courseRepository, never()).findById(any());
        verify(enrollmentService, never()).isAlreadyEnrolled(any(), any());
        verify(enrollmentService, never()).createEnrollmentWithProgress(any(Enrollment.class));
    }

    @Test
    void shouldNotCreateEnrollmentWhenCourseIdIsBlank() {
        ReflectionTestUtils.setField(enrollmentCreateDTO, "courseId", "");

        when(userService.userHasRole("learner")).thenReturn(true);

        ResponseEntity<Enrollment> response = enrollmentController.createEnrollment(enrollmentCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(courseRepository, never()).findById(any());
        verify(enrollmentService, never()).isAlreadyEnrolled(any(), any());
        verify(enrollmentService, never()).createEnrollmentWithProgress(any(Enrollment.class));
    }

    @Test
    void shouldNotCreateEnrollmentWhenCourseDoesNotExist() {
        when(userService.userHasRole("learner")).thenReturn(true);
        when(courseRepository.findById("course-1")).thenReturn(Optional.empty());

        ResponseEntity<Enrollment> response = enrollmentController.createEnrollment(enrollmentCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(enrollmentService, never()).isAlreadyEnrolled(any(), any());
        verify(enrollmentService, never()).createEnrollmentWithProgress(any(Enrollment.class));
    }

    @Test
    void shouldNotCreateEnrollmentWhenCourseIsDraft() {
        when(userService.userHasRole("learner")).thenReturn(true);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(draftCourse));

        ResponseEntity<Enrollment> response = enrollmentController.createEnrollment(enrollmentCreateDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(enrollmentService, never()).isAlreadyEnrolled(any(), any());
        verify(enrollmentService, never()).createEnrollmentWithProgress(any(Enrollment.class));
    }

    @Test
    void shouldNotCreateEnrollmentWhenAlreadyEnrolled() {
        when(userService.userHasRole("learner")).thenReturn(true);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(publishedCourse));
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(enrollmentService.isAlreadyEnrolled("course-1", "auth0|learner-1")).thenReturn(true);

        ResponseEntity<Enrollment> response = enrollmentController.createEnrollment(enrollmentCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(enrollmentService, never()).createEnrollmentWithProgress(any(Enrollment.class));
    }

    @Test
    void shouldReturnAllEnrollmentsForTutorCoursesWhenUserIsTutor() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseRepository.findByTutorUserId("auth0|tutor-1")).thenReturn(List.of(publishedCourse));
        when(enrollmentRepository.findByCourseId("course-1")).thenReturn(List.of(enrollment));

        ResponseEntity<List<Enrollment>> response = enrollmentController.getAllEnrollments();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(1, response.getBody().size());
        assertEquals("enrollment-1", response.getBody().get(0).getId());
        assertEquals("course-1", response.getBody().get(0).getCourseId());

        verify(courseRepository).findByTutorUserId("auth0|tutor-1");
        verify(enrollmentRepository).findByCourseId("course-1");
        verify(enrollmentRepository, never()).findAll();
    }

    @Test
    void shouldNotReturnAllEnrollmentsWhenUserIsNotTutor() {
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<List<Enrollment>> response = enrollmentController.getAllEnrollments();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(courseRepository, never()).findByTutorUserId(any());
        verify(enrollmentRepository, never()).findAll();
    }

    @Test
    void shouldReturnEnrollmentByIdWhenLearnerOwnsEnrollment() {
        when(enrollmentRepository.findById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(userService.userHasRole("learner")).thenReturn(true);

        ResponseEntity<Enrollment> response = enrollmentController.getEnrollmentById("enrollment-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("enrollment-1", response.getBody().getId());
        assertEquals("course-1", response.getBody().getCourseId());
    }

    @Test
    void shouldReturnEnrollmentByIdWhenTutorOwnsCourse() {
        when(enrollmentRepository.findById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(userService.userHasRole("learner")).thenReturn(false);
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(true);

        ResponseEntity<Enrollment> response = enrollmentController.getEnrollmentById("enrollment-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("enrollment-1", response.getBody().getId());
        assertEquals("course-1", response.getBody().getCourseId());
    }

    @Test
    void shouldReturnForbiddenWhenUserCannotAccessEnrollmentById() {
        when(enrollmentRepository.findById("enrollment-1")).thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUserId()).thenReturn("auth0|other-user");
        when(userService.userHasRole("learner")).thenReturn(false);
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<Enrollment> response = enrollmentController.getEnrollmentById("enrollment-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void shouldReturnNotFoundWhenEnrollmentDoesNotExist() {
        when(enrollmentRepository.findById("enrollment-1")).thenReturn(Optional.empty());

        ResponseEntity<Enrollment> response = enrollmentController.getEnrollmentById("enrollment-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnEnrollmentsByLearnerUserIdWhenLearnerRequestsOwnEnrollments() {
        when(userService.userHasRole("learner")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(enrollmentRepository.findByLearnerUserId("auth0|learner-1"))
                .thenReturn(List.of(enrollment));

        ResponseEntity<List<Enrollment>> response =
                enrollmentController.getEnrollmentsByLearner("auth0|learner-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(1, response.getBody().size());
        assertEquals("enrollment-1", response.getBody().get(0).getId());
        assertEquals("auth0|learner-1", response.getBody().get(0).getLearnerUserId());
    }

    @Test
    void shouldReturnForbiddenWhenLearnerRequestsAnotherLearnersEnrollments() {
        when(userService.userHasRole("learner")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");

        ResponseEntity<List<Enrollment>> response =
                enrollmentController.getEnrollmentsByLearner("auth0|other-learner");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(enrollmentRepository, never()).findByLearnerUserId(any());
    }

    @Test
    void shouldReturnForbiddenWhenNonLearnerRequestsEnrollmentsByLearnerUserId() {
        when(userService.userHasRole("learner")).thenReturn(false);

        ResponseEntity<List<Enrollment>> response =
                enrollmentController.getEnrollmentsByLearner("auth0|learner-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(enrollmentRepository, never()).findByLearnerUserId(any());
    }

    @Test
    void shouldReturnEnrollmentsByCourseIdWhenTutorOwnsCourse() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(courseService.courseExists("course-1")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|tutor-1");
        when(courseService.courseBelongsToTutor("course-1", "auth0|tutor-1")).thenReturn(true);
        when(enrollmentRepository.findByCourseId("course-1"))
                .thenReturn(List.of(enrollment));

        ResponseEntity<List<Enrollment>> response =
                enrollmentController.getEnrollmentsByCourse("course-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(1, response.getBody().size());
        assertEquals("enrollment-1", response.getBody().get(0).getId());
        assertEquals("course-1", response.getBody().get(0).getCourseId());
    }

    @Test
    void shouldReturnForbiddenWhenNonTutorRequestsEnrollmentsByCourseId() {
        when(userService.userHasRole("tutor")).thenReturn(false);

        ResponseEntity<List<Enrollment>> response =
                enrollmentController.getEnrollmentsByCourse("course-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(enrollmentRepository, never()).findByCourseId(any());
    }

    @Test
    void shouldReturnNotFoundWhenCourseDoesNotExistForEnrollmentsByCourseId() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(courseService.courseExists("course-1")).thenReturn(false);

        ResponseEntity<List<Enrollment>> response =
                enrollmentController.getEnrollmentsByCourse("course-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(enrollmentRepository, never()).findByCourseId(any());
    }

    @Test
    void shouldReturnForbiddenWhenTutorDoesNotOwnCourseForEnrollmentsByCourseId() {
        when(userService.userHasRole("tutor")).thenReturn(true);
        when(courseService.courseExists("course-1")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|other-tutor");
        when(courseService.courseBelongsToTutor("course-1", "auth0|other-tutor")).thenReturn(false);

        ResponseEntity<List<Enrollment>> response =
                enrollmentController.getEnrollmentsByCourse("course-1");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(enrollmentRepository, never()).findByCourseId(any());
    }

    @Test
    void shouldReturnMyEnrollmentsWhenUserIsLearner() {
        when(userService.userHasRole("learner")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(enrollmentRepository.findByLearnerUserId("auth0|learner-1"))
                .thenReturn(List.of(enrollment));

        ResponseEntity<List<Enrollment>> response = enrollmentController.getMyEnrollments();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals(1, response.getBody().size());
        assertEquals("enrollment-1", response.getBody().get(0).getId());
        assertEquals("auth0|learner-1", response.getBody().get(0).getLearnerUserId());

        verify(enrollmentRepository).findByLearnerUserId("auth0|learner-1");
    }

    @Test
    void shouldNotReturnMyEnrollmentsWhenUserIsNotLearner() {
        when(userService.userHasRole("learner")).thenReturn(false);

        ResponseEntity<List<Enrollment>> response = enrollmentController.getMyEnrollments();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        verify(enrollmentRepository, never()).findByLearnerUserId(any());
    }
}