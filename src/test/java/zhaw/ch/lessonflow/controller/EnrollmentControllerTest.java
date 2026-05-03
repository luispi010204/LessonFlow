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

import zhaw.ch.lessonflow.model.Enrollment;
import zhaw.ch.lessonflow.model.EnrollmentCreateDTO;
import zhaw.ch.lessonflow.repository.EnrollmentRepository;
import zhaw.ch.lessonflow.services.CourseService;
import zhaw.ch.lessonflow.services.EnrollmentService;
import zhaw.ch.lessonflow.services.UserService;

@ExtendWith(MockitoExtension.class)
public class EnrollmentControllerTest {

    @Mock
    EnrollmentRepository enrollmentRepository;

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
    }

    @Test
    void shouldCreateEnrollmentWhenUserIsLearnerCourseExistsAndNotAlreadyEnrolled() {
        when(userService.userHasRole("learner")).thenReturn(true);
        when(courseService.courseExists("course-1")).thenReturn(true);
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

        verify(courseService, never()).courseExists(any());
        verify(enrollmentService, never()).isAlreadyEnrolled(any(), any());
        verify(enrollmentService, never()).createEnrollmentWithProgress(any(Enrollment.class));
    }

    @Test
    void shouldNotCreateEnrollmentWhenCourseDoesNotExist() {
        when(userService.userHasRole("learner")).thenReturn(true);
        when(courseService.courseExists("course-1")).thenReturn(false);

        ResponseEntity<Enrollment> response = enrollmentController.createEnrollment(enrollmentCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(enrollmentService, never()).isAlreadyEnrolled(any(), any());
        verify(enrollmentService, never()).createEnrollmentWithProgress(any(Enrollment.class));
    }

    @Test
    void shouldNotCreateEnrollmentWhenAlreadyEnrolled() {
        when(userService.userHasRole("learner")).thenReturn(true);
        when(courseService.courseExists("course-1")).thenReturn(true);
        when(userService.getCurrentUserId()).thenReturn("auth0|learner-1");
        when(enrollmentService.isAlreadyEnrolled("course-1", "auth0|learner-1")).thenReturn(true);

        ResponseEntity<Enrollment> response = enrollmentController.createEnrollment(enrollmentCreateDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(enrollmentService, never()).createEnrollmentWithProgress(any(Enrollment.class));
    }

    @Test
    void shouldReturnAllEnrollments() {
        when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment));

        List<Enrollment> result = enrollmentController.getAllEnrollments();

        assertEquals(1, result.size());
        assertEquals("enrollment-1", result.get(0).getId());
        assertEquals("course-1", result.get(0).getCourseId());
    }

    @Test
    void shouldReturnEnrollmentByIdWhenEnrollmentExists() {
        when(enrollmentRepository.findById("enrollment-1")).thenReturn(Optional.of(enrollment));

        ResponseEntity<Enrollment> response = enrollmentController.getEnrollmentById("enrollment-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
        assertEquals("enrollment-1", response.getBody().getId());
        assertEquals("course-1", response.getBody().getCourseId());
    }

    @Test
    void shouldReturnNotFoundWhenEnrollmentDoesNotExist() {
        when(enrollmentRepository.findById("enrollment-1")).thenReturn(Optional.empty());

        ResponseEntity<Enrollment> response = enrollmentController.getEnrollmentById("enrollment-1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnEnrollmentsByLearnerUserId() {
        when(enrollmentRepository.findByLearnerUserId("auth0|learner-1"))
                .thenReturn(List.of(enrollment));

        List<Enrollment> result = enrollmentController.getEnrollmentsByLearner("auth0|learner-1");

        assertEquals(1, result.size());
        assertEquals("enrollment-1", result.get(0).getId());
        assertEquals("auth0|learner-1", result.get(0).getLearnerUserId());
    }

    @Test
    void shouldReturnEnrollmentsByCourseId() {
        when(enrollmentRepository.findByCourseId("course-1"))
                .thenReturn(List.of(enrollment));

        List<Enrollment> result = enrollmentController.getEnrollmentsByCourse("course-1");

        assertEquals(1, result.size());
        assertEquals("enrollment-1", result.get(0).getId());
        assertEquals("course-1", result.get(0).getCourseId());
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