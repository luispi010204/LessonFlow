package zhaw.ch.lessonflow.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import zhaw.ch.lessonflow.model.Enrollment;
import zhaw.ch.lessonflow.model.Lesson;
import zhaw.ch.lessonflow.model.LessonProgress;
import zhaw.ch.lessonflow.model.LessonProgressState;
import zhaw.ch.lessonflow.repository.EnrollmentRepository;
import zhaw.ch.lessonflow.repository.LessonProgressRepository;
import zhaw.ch.lessonflow.repository.LessonRepository;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceTest {

    @Mock
    EnrollmentRepository enrollmentRepository;

    @Mock
    LessonRepository lessonRepository;

    @Mock
    LessonProgressRepository lessonProgressRepository;

    @InjectMocks
    EnrollmentService enrollmentService;

    private Enrollment enrollment;
    private Lesson lesson1;
    private Lesson lesson2;
    private Lesson lesson3;

    @BeforeEach
    void setUp() {
        enrollment = new Enrollment("course-1", "learner-1", "ENROLLED");
        ReflectionTestUtils.setField(enrollment, "id", "enrollment-1");

        lesson1 = new Lesson("course-1", 1, "Rhythm", "Material 1", "link-1");
        ReflectionTestUtils.setField(lesson1, "id", "lesson-1");

        lesson2 = new Lesson("course-1", 2, "Melody", "Material 2", "link-2");
        ReflectionTestUtils.setField(lesson2, "id", "lesson-2");

        lesson3 = new Lesson("course-1", 3, "Harmony", "Material 3", "link-3");
        ReflectionTestUtils.setField(lesson3, "id", "lesson-3");
    }

    @Test
    void shouldCreateEnrollmentAndInitializeLessonProgress() {
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);
        when(lessonRepository.findByCourseId("course-1"))
                .thenReturn(List.of(lesson1, lesson2, lesson3));

        Enrollment result = enrollmentService.createEnrollmentWithProgress(enrollment);

        assertNotNull(result);
        assertEquals("enrollment-1", result.getId());

        verify(enrollmentRepository, times(1)).save(enrollment);
        verify(lessonProgressRepository, times(3)).save(any(LessonProgress.class));

        ArgumentCaptor<LessonProgress> captor = ArgumentCaptor.forClass(LessonProgress.class);
        verify(lessonProgressRepository, times(3)).save(captor.capture());

        List<LessonProgress> savedProgress = captor.getAllValues();

        assertEquals(3, savedProgress.size());

        assertEquals("enrollment-1", savedProgress.get(0).getEnrollmentId());
        assertEquals("lesson-1", savedProgress.get(0).getLessonId());
        assertEquals(LessonProgressState.UNLOCKED, savedProgress.get(0).getState());

        assertEquals("enrollment-1", savedProgress.get(1).getEnrollmentId());
        assertEquals("lesson-2", savedProgress.get(1).getLessonId());
        assertEquals(LessonProgressState.LOCKED, savedProgress.get(1).getState());

        assertEquals("enrollment-1", savedProgress.get(2).getEnrollmentId());
        assertEquals("lesson-3", savedProgress.get(2).getLessonId());
        assertEquals(LessonProgressState.LOCKED, savedProgress.get(2).getState());
    }
}