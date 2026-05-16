package zhaw.ch.lessonflow.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

import zhaw.ch.lessonflow.model.Enrollment;
import zhaw.ch.lessonflow.model.Lesson;
import zhaw.ch.lessonflow.model.LessonProgress;
import zhaw.ch.lessonflow.model.LessonProgressState;
import zhaw.ch.lessonflow.model.ProgressSummaryDTO;
import zhaw.ch.lessonflow.repository.LessonProgressRepository;
import zhaw.ch.lessonflow.repository.LessonRepository;

@ExtendWith(MockitoExtension.class)
public class LessonProgressServiceTest {

    @Mock
    LessonProgressRepository lessonProgressRepository;

    @Mock
    LessonRepository lessonRepository;

    @Mock
    EnrollmentService enrollmentService;

    @InjectMocks
    LessonProgressService lessonProgressService;

    Enrollment enrollment;
    LessonProgress progress;
    LessonProgress nextProgress;

    Lesson lesson1;
    Lesson lesson2;
    Lesson lesson3;

    @BeforeEach
    void setUp() {
        enrollment = new Enrollment(
                "course-1",
                "auth0|learner-1",
                "ENROLLED"
        );
        ReflectionTestUtils.setField(enrollment, "id", "enrollment-1");

        progress = new LessonProgress(
                "enrollment-1",
                "lesson-1",
                LessonProgressState.UNLOCKED,
                false,
                0
        );
        progress.setId("progress-1");

        nextProgress = new LessonProgress(
                "enrollment-1",
                "lesson-2",
                LessonProgressState.LOCKED,
                false,
                0
        );
        nextProgress.setId("progress-2");

        lesson1 = new Lesson(
                "course-1",
                1,
                "Lesson 1",
                "Material 1",
                "https://meeting-1.example.com"
        );
        ReflectionTestUtils.setField(lesson1, "id", "lesson-1");

        lesson2 = new Lesson(
                "course-1",
                2,
                "Lesson 2",
                "Material 2",
                "https://meeting-2.example.com"
        );
        ReflectionTestUtils.setField(lesson2, "id", "lesson-2");

        lesson3 = new Lesson(
                "course-1",
                3,
                "Lesson 3",
                "Material 3",
                "https://meeting-3.example.com"
        );
        ReflectionTestUtils.setField(lesson3, "id", "lesson-3");
    }

    @Test
    void shouldMarkMaterialDoneWhenStateIsUnlocked() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(progress));
        when(lessonProgressRepository.save(any(LessonProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<LessonProgress> result = lessonProgressService.markMaterialDone("progress-1");

        assertTrue(result.isPresent());
        assertEquals(LessonProgressState.MATERIAL_DONE, result.get().getState());

        verify(lessonProgressRepository).save(progress);
    }

    @Test
    void shouldNotMarkMaterialDoneWhenProgressDoesNotExist() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.empty());

        Optional<LessonProgress> result = lessonProgressService.markMaterialDone("progress-1");

        assertFalse(result.isPresent());

        verify(lessonProgressRepository, never()).save(any(LessonProgress.class));
    }

    @Test
    void shouldNotMarkMaterialDoneWhenStateIsNotUnlocked() {
        progress.setState(LessonProgressState.MATERIAL_DONE);

        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(progress));

        Optional<LessonProgress> result = lessonProgressService.markMaterialDone("progress-1");

        assertFalse(result.isPresent());

        verify(lessonProgressRepository, never()).save(any(LessonProgress.class));
    }

    @Test
    void shouldConfirmMeetingWhenStateIsMaterialDone() {
        progress.setState(LessonProgressState.MATERIAL_DONE);

        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(progress));
        when(lessonProgressRepository.save(any(LessonProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<LessonProgress> result = lessonProgressService.confirmMeeting("progress-1");

        assertTrue(result.isPresent());
        assertEquals(LessonProgressState.MEETING_DONE, result.get().getState());
        assertTrue(result.get().isMeetingConfirmed());

        verify(lessonProgressRepository).save(progress);
    }

    @Test
    void shouldNotConfirmMeetingWhenProgressDoesNotExist() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.empty());

        Optional<LessonProgress> result = lessonProgressService.confirmMeeting("progress-1");

        assertFalse(result.isPresent());

        verify(lessonProgressRepository, never()).save(any(LessonProgress.class));
    }

    @Test
    void shouldNotConfirmMeetingWhenStateIsNotMaterialDone() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(progress));

        Optional<LessonProgress> result = lessonProgressService.confirmMeeting("progress-1");

        assertFalse(result.isPresent());

        verify(lessonProgressRepository, never()).save(any(LessonProgress.class));
    }

    @Test
    void shouldNotMarkPassedDirectlyFromUnlocked() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(progress));

        Optional<LessonProgress> result = lessonProgressService.markPassed("progress-1");

        assertFalse(result.isPresent());

        verify(lessonProgressRepository, never()).save(any(LessonProgress.class));
    }

    @Test
    void shouldNotMarkPassedWhenProgressDoesNotExist() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.empty());

        Optional<LessonProgress> result = lessonProgressService.markPassed("progress-1");

        assertFalse(result.isPresent());

        verify(lessonProgressRepository, never()).save(any(LessonProgress.class));
    }

    @Test
    void shouldMarkPassedWhenStateIsMeetingDone() {
        progress.setState(LessonProgressState.MEETING_DONE);

        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(progress));
        when(lessonProgressRepository.save(any(LessonProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.empty());

        Optional<LessonProgress> result = lessonProgressService.markPassed("progress-1");

        assertTrue(result.isPresent());
        assertEquals(LessonProgressState.PASSED, result.get().getState());

        verify(lessonProgressRepository).save(progress);
    }

    @Test
    void shouldUnlockNextLessonWhenMarkPassedAndNextProgressIsLocked() {
        progress.setState(LessonProgressState.MEETING_DONE);

        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(progress));
        when(lessonProgressRepository.save(any(LessonProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson1));
        when(lessonRepository.findByCourseIdAndLessonNumber("course-1", 2))
                .thenReturn(Optional.of(lesson2));
        when(lessonProgressRepository.findByEnrollmentIdAndLessonId("enrollment-1", "lesson-2"))
                .thenReturn(Optional.of(nextProgress));

        Optional<LessonProgress> result = lessonProgressService.markPassed("progress-1");

        assertTrue(result.isPresent());
        assertEquals(LessonProgressState.PASSED, result.get().getState());
        assertEquals(LessonProgressState.UNLOCKED, nextProgress.getState());

        verify(lessonProgressRepository, times(2)).save(any(LessonProgress.class));
        verify(lessonProgressRepository).save(progress);
        verify(lessonProgressRepository).save(nextProgress);
    }

    @Test
    void shouldNotUnlockNextLessonWhenNoNextLessonExists() {
        progress.setState(LessonProgressState.MEETING_DONE);

        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(progress));
        when(lessonProgressRepository.save(any(LessonProgress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson1));
        when(lessonRepository.findByCourseIdAndLessonNumber("course-1", 2))
                .thenReturn(Optional.empty());

        Optional<LessonProgress> result = lessonProgressService.markPassed("progress-1");

        assertTrue(result.isPresent());
        assertEquals(LessonProgressState.PASSED, result.get().getState());

        verify(lessonProgressRepository, times(1)).save(any(LessonProgress.class));
        verify(lessonProgressRepository, never()).findByEnrollmentIdAndLessonId(any(), any());
    }

    @Test
    void shouldReturnProgressByEnrollmentIdAndLessonId() {
        when(lessonProgressRepository.findByEnrollmentIdAndLessonId("enrollment-1", "lesson-1"))
                .thenReturn(Optional.of(progress));

        Optional<LessonProgress> result =
                lessonProgressService.getByEnrollmentIdAndLessonId("enrollment-1", "lesson-1");

        assertTrue(result.isPresent());
        assertEquals("progress-1", result.get().getId());
    }

    @Test
    void shouldSaveProgress() {
        when(lessonProgressRepository.save(progress)).thenReturn(progress);

        LessonProgress result = lessonProgressService.save(progress);

        assertEquals("progress-1", result.getId());

        verify(lessonProgressRepository).save(progress);
    }

    @Test
    void shouldReturnCurrentLessonWhenProgressIsUnlocked() {
        when(enrollmentService.getEnrollmentById("enrollment-1"))
                .thenReturn(Optional.of(enrollment));
        when(lessonRepository.findByCourseIdOrderByLessonNumberAsc("course-1"))
                .thenReturn(List.of(lesson1));
        when(lessonProgressRepository.findByEnrollmentIdAndLessonId("enrollment-1", "lesson-1"))
                .thenReturn(Optional.of(progress));

        Optional<Lesson> result = lessonProgressService.getCurrentLesson("enrollment-1");

        assertTrue(result.isPresent());
        assertEquals("lesson-1", result.get().getId());
        assertEquals("Lesson 1", result.get().getTitle());

        verify(enrollmentService).syncMissingProgressForEnrollment("enrollment-1");
    }

    @Test
    void shouldReturnCurrentLessonWhenProgressIsMaterialDone() {
        progress.setState(LessonProgressState.MATERIAL_DONE);

        when(enrollmentService.getEnrollmentById("enrollment-1"))
                .thenReturn(Optional.of(enrollment));
        when(lessonRepository.findByCourseIdOrderByLessonNumberAsc("course-1"))
                .thenReturn(List.of(lesson1));
        when(lessonProgressRepository.findByEnrollmentIdAndLessonId("enrollment-1", "lesson-1"))
                .thenReturn(Optional.of(progress));

        Optional<Lesson> result = lessonProgressService.getCurrentLesson("enrollment-1");

        assertTrue(result.isPresent());
        assertEquals("lesson-1", result.get().getId());

        verify(enrollmentService).syncMissingProgressForEnrollment("enrollment-1");
    }

    @Test
    void shouldReturnCurrentLessonWhenProgressIsMeetingDone() {
        progress.setState(LessonProgressState.MEETING_DONE);

        when(enrollmentService.getEnrollmentById("enrollment-1"))
                .thenReturn(Optional.of(enrollment));
        when(lessonRepository.findByCourseIdOrderByLessonNumberAsc("course-1"))
                .thenReturn(List.of(lesson1));
        when(lessonProgressRepository.findByEnrollmentIdAndLessonId("enrollment-1", "lesson-1"))
                .thenReturn(Optional.of(progress));

        Optional<Lesson> result = lessonProgressService.getCurrentLesson("enrollment-1");

        assertTrue(result.isPresent());
        assertEquals("lesson-1", result.get().getId());

        verify(enrollmentService).syncMissingProgressForEnrollment("enrollment-1");
    }

    @Test
    void shouldReturnEmptyCurrentLessonWhenEnrollmentDoesNotExist() {
        when(enrollmentService.getEnrollmentById("enrollment-1"))
                .thenReturn(Optional.empty());

        Optional<Lesson> result = lessonProgressService.getCurrentLesson("enrollment-1");

        assertFalse(result.isPresent());

        verify(enrollmentService).syncMissingProgressForEnrollment("enrollment-1");
        verify(lessonRepository, never()).findByCourseIdOrderByLessonNumberAsc(any());
    }

    @Test
    void shouldReturnEmptyCurrentLessonWhenAllLessonsArePassedOrLocked() {
        LessonProgress passedProgress = new LessonProgress(
                "enrollment-1",
                "lesson-1",
                LessonProgressState.PASSED,
                true,
                1
        );

        LessonProgress lockedProgress = new LessonProgress(
                "enrollment-1",
                "lesson-2",
                LessonProgressState.LOCKED,
                false,
                0
        );

        when(enrollmentService.getEnrollmentById("enrollment-1"))
                .thenReturn(Optional.of(enrollment));
        when(lessonRepository.findByCourseIdOrderByLessonNumberAsc("course-1"))
                .thenReturn(List.of(lesson1, lesson2));
        when(lessonProgressRepository.findByEnrollmentIdAndLessonId("enrollment-1", "lesson-1"))
                .thenReturn(Optional.of(passedProgress));
        when(lessonProgressRepository.findByEnrollmentIdAndLessonId("enrollment-1", "lesson-2"))
                .thenReturn(Optional.of(lockedProgress));

        Optional<Lesson> result = lessonProgressService.getCurrentLesson("enrollment-1");

        assertFalse(result.isPresent());

        verify(enrollmentService).syncMissingProgressForEnrollment("enrollment-1");
    }

    @Test
    void shouldReturnEmptyProgressSummaryWhenEnrollmentDoesNotExist() {
        when(enrollmentService.getEnrollmentById("enrollment-1"))
                .thenReturn(Optional.empty());

        Optional<ProgressSummaryDTO> result =
                lessonProgressService.getProgressSummary("enrollment-1");

        assertFalse(result.isPresent());

        verify(enrollmentService).syncMissingProgressForEnrollment("enrollment-1");
        verify(lessonRepository, never()).findByCourseIdOrderByLessonNumberAsc(any());
    }

    @Test
    void shouldReturnEmptyProgressSummaryWhenCourseHasNoLessons() {
        when(enrollmentService.getEnrollmentById("enrollment-1"))
                .thenReturn(Optional.of(enrollment));
        when(lessonRepository.findByCourseIdOrderByLessonNumberAsc("course-1"))
                .thenReturn(List.of());

        Optional<ProgressSummaryDTO> result =
                lessonProgressService.getProgressSummary("enrollment-1");

        assertFalse(result.isPresent());

        verify(enrollmentService).syncMissingProgressForEnrollment("enrollment-1");
    }

    @Test
    void shouldReturnProgressSummary() {
        LessonProgress passedProgress = new LessonProgress(
                "enrollment-1",
                "lesson-1",
                LessonProgressState.PASSED,
                true,
                1
        );

        LessonProgress currentProgress = new LessonProgress(
                "enrollment-1",
                "lesson-2",
                LessonProgressState.UNLOCKED,
                false,
                0
        );

        LessonProgress lockedProgress = new LessonProgress(
                "enrollment-1",
                "lesson-3",
                LessonProgressState.LOCKED,
                false,
                0
        );

        when(enrollmentService.getEnrollmentById("enrollment-1"))
                .thenReturn(Optional.of(enrollment));
        when(lessonRepository.findByCourseIdOrderByLessonNumberAsc("course-1"))
                .thenReturn(List.of(lesson1, lesson2, lesson3));
        when(lessonProgressRepository.findByEnrollmentIdAndLessonId("enrollment-1", "lesson-1"))
                .thenReturn(Optional.of(passedProgress));
        when(lessonProgressRepository.findByEnrollmentIdAndLessonId("enrollment-1", "lesson-2"))
                .thenReturn(Optional.of(currentProgress));
        when(lessonProgressRepository.findByEnrollmentIdAndLessonId("enrollment-1", "lesson-3"))
                .thenReturn(Optional.of(lockedProgress));

        Optional<ProgressSummaryDTO> result =
                lessonProgressService.getProgressSummary("enrollment-1");

        assertTrue(result.isPresent());
        assertEquals(3, result.get().getTotalLessons());
        assertEquals(1, result.get().getPassedLessons());
        assertEquals("lesson-2", result.get().getCurrentLessonId());

        verify(enrollmentService).syncMissingProgressForEnrollment("enrollment-1");
    }
}