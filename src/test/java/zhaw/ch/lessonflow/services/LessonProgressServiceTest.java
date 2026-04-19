package zhaw.ch.lessonflow.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import zhaw.ch.lessonflow.model.Lesson;
import zhaw.ch.lessonflow.model.LessonProgress;
import zhaw.ch.lessonflow.model.LessonProgressState;
import zhaw.ch.lessonflow.repository.LessonProgressRepository;
import zhaw.ch.lessonflow.repository.LessonRepository;

@ExtendWith(MockitoExtension.class)
public class LessonProgressServiceTest {

    @Mock
    LessonProgressRepository lessonProgressRepository;

    @Mock
    LessonRepository lessonRepository;

    @InjectMocks
    LessonProgressService lessonProgressService;

    LessonProgress progress;

    @BeforeEach
    void setUp() {
        progress = new LessonProgress("enrollment-1", "lesson-1", LessonProgressState.UNLOCKED, false, 0);
        progress.setId("progress-1");
    }

    @Test
    void shouldMarkMaterialDoneWhenStateIsUnlocked() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(progress));
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<LessonProgress> result = lessonProgressService.markMaterialDone("progress-1");

        assertTrue(result.isPresent());
        assertEquals(LessonProgressState.MATERIAL_DONE, result.get().getState());
    }

    @Test
    void shouldNotMarkPassedDirectlyFromUnlocked() {
        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(progress));

        Optional<LessonProgress> result = lessonProgressService.markPassed("progress-1");

        assertFalse(result.isPresent());
    }

    @Test
    void shouldConfirmMeetingWhenStateIsMaterialDone() {
        progress.setState(LessonProgressState.MATERIAL_DONE);

        when(lessonProgressRepository.findById("progress-1")).thenReturn(Optional.of(progress));
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<LessonProgress> result = lessonProgressService.confirmMeeting("progress-1");

        assertTrue(result.isPresent());
        assertEquals(LessonProgressState.MEETING_DONE, result.get().getState());
        assertTrue(result.get().isMeetingConfirmed());
    }
}