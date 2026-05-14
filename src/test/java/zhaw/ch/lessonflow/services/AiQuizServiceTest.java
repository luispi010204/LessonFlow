package zhaw.ch.lessonflow.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import zhaw.ch.lessonflow.model.QuizQuestion;

@ExtendWith(MockitoExtension.class)
public class AiQuizServiceTest {

    @Mock
    ChatClient.Builder chatClientBuilder;

    ChatClient chatClient;

    AiQuizService aiQuizService;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientBuilder.build()).thenReturn(chatClient);

        aiQuizService = new AiQuizService(chatClientBuilder);
    }

    @Test
    void shouldReturnEmptyWhenLessonMaterialIsNull() {
        Optional<List<QuizQuestion>> result =
                aiQuizService.generateQuizQuestions("Rhythm", null, 3);

        assertTrue(result.isEmpty());

        verify(chatClient, never()).prompt();
    }

    @Test
    void shouldReturnEmptyWhenLessonMaterialIsBlank() {
        Optional<List<QuizQuestion>> result =
                aiQuizService.generateQuizQuestions("Rhythm", "   ", 3);

        assertTrue(result.isEmpty());

        verify(chatClient, never()).prompt();
    }

    @Test
    void shouldReturnEmptyWhenQuestionCountIsTooLow() {
        Optional<List<QuizQuestion>> result =
                aiQuizService.generateQuizQuestions("Rhythm", "Lesson material", 0);

        assertTrue(result.isEmpty());

        verify(chatClient, never()).prompt();
    }

    @Test
    void shouldReturnEmptyWhenQuestionCountIsTooHigh() {
        Optional<List<QuizQuestion>> result =
                aiQuizService.generateQuizQuestions("Rhythm", "Lesson material", 11);

        assertTrue(result.isEmpty());

        verify(chatClient, never()).prompt();
    }

    @Test
    void shouldGenerateQuizQuestionsFromValidAiResponse() {
        stubAiResponse("""
                QUESTION: What does rhythm describe?
                A: The color of a note
                B: The timing of sounds
                C: The volume of a song
                D: The name of an instrument
                ANSWER: B

                ---

                QUESTION: What does tempo describe?
                A: The speed of music
                B: The lyrics of music
                C: The shape of notes
                D: The size of an instrument
                ANSWER: A
                """);

        Optional<List<QuizQuestion>> result =
                aiQuizService.generateQuizQuestions(
                        "Understanding Rhythm",
                        "Rhythm describes the timing of sounds. Tempo describes speed.",
                        2
                );

        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());

        QuizQuestion firstQuestion = result.get().get(0);
        assertEquals("What does rhythm describe?", firstQuestion.getQuestionText());
        assertEquals(List.of(
                "The color of a note",
                "The timing of sounds",
                "The volume of a song",
                "The name of an instrument"
        ), firstQuestion.getOptions());
        assertEquals(1, firstQuestion.getCorrectOptionIndex());

        QuizQuestion secondQuestion = result.get().get(1);
        assertEquals("What does tempo describe?", secondQuestion.getQuestionText());
        assertEquals(0, secondQuestion.getCorrectOptionIndex());
    }

    @Test
    void shouldMapAllAnswerLettersToCorrectOptionIndexes() {
        stubAiResponse("""
                QUESTION: Question A?
                A: Correct A
                B: Wrong B
                C: Wrong C
                D: Wrong D
                ANSWER: A

                ---

                QUESTION: Question B?
                A: Wrong A
                B: Correct B
                C: Wrong C
                D: Wrong D
                ANSWER: B

                ---

                QUESTION: Question C?
                A: Wrong A
                B: Wrong B
                C: Correct C
                D: Wrong D
                ANSWER: C

                ---

                QUESTION: Question D?
                A: Wrong A
                B: Wrong B
                C: Wrong C
                D: Correct D
                ANSWER: D
                """);

        Optional<List<QuizQuestion>> result =
                aiQuizService.generateQuizQuestions("Answer Mapping", "Lesson material", 4);

        assertTrue(result.isPresent());
        assertEquals(4, result.get().size());
        assertEquals(0, result.get().get(0).getCorrectOptionIndex());
        assertEquals(1, result.get().get(1).getCorrectOptionIndex());
        assertEquals(2, result.get().get(2).getCorrectOptionIndex());
        assertEquals(3, result.get().get(3).getCorrectOptionIndex());
    }

    @Test
    void shouldTrimValuesAndNormalizeLowercaseAnswer() {
        stubAiResponse("""
                   QUESTION:   Which answer is correct?
                   A:   First option
                   B:   Second option
                   C:   Third option
                   D:   Fourth option
                   ANSWER:   d
                """);

        Optional<List<QuizQuestion>> result =
                aiQuizService.generateQuizQuestions("Trim Test", "Lesson material", 1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("Which answer is correct?", result.get().get(0).getQuestionText());
        assertEquals("First option", result.get().get(0).getOptions().get(0));
        assertEquals("Fourth option", result.get().get(0).getOptions().get(3));
        assertEquals(3, result.get().get(0).getCorrectOptionIndex());
    }

    @Test
    void shouldReturnEmptyWhenAiResponseIsNull() {
        stubAiResponse(null);

        Optional<List<QuizQuestion>> result =
                aiQuizService.generateQuizQuestions("Rhythm", "Lesson material", 1);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenAiResponseIsBlank() {
        stubAiResponse("   ");

        Optional<List<QuizQuestion>> result =
                aiQuizService.generateQuizQuestions("Rhythm", "Lesson material", 1);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenGeneratedQuestionCountDoesNotMatchRequestedCount() {
        stubAiResponse("""
                QUESTION: Only one question?
                A: Option A
                B: Option B
                C: Option C
                D: Option D
                ANSWER: A
                """);

        Optional<List<QuizQuestion>> result =
                aiQuizService.generateQuizQuestions("Rhythm", "Lesson material", 2);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenQuestionBlockIsMissingQuestionText() {
        stubAiResponse("""
                QUESTION:
                A: Option A
                B: Option B
                C: Option C
                D: Option D
                ANSWER: A
                """);

        Optional<List<QuizQuestion>> result =
                aiQuizService.generateQuizQuestions("Rhythm", "Lesson material", 1);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenQuestionBlockIsMissingAnOption() {
        stubAiResponse("""
                QUESTION: What is rhythm?
                A: Option A
                B: Option B
                C: Option C
                ANSWER: A
                """);

        Optional<List<QuizQuestion>> result =
                aiQuizService.generateQuizQuestions("Rhythm", "Lesson material", 1);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenQuestionBlockHasInvalidAnswerLetter() {
        stubAiResponse("""
                QUESTION: What is rhythm?
                A: Option A
                B: Option B
                C: Option C
                D: Option D
                ANSWER: E
                """);

        Optional<List<QuizQuestion>> result =
                aiQuizService.generateQuizQuestions("Rhythm", "Lesson material", 1);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldIgnoreEmptyBlocksAndReturnValidQuestions() {
        stubAiResponse("""
                ---

                QUESTION: What is tempo?
                A: Speed
                B: Color
                C: Size
                D: Shape
                ANSWER: A

                ---
                """);

        Optional<List<QuizQuestion>> result =
                aiQuizService.generateQuizQuestions("Tempo", "Lesson material", 1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("What is tempo?", result.get().get(0).getQuestionText());
        assertEquals(0, result.get().get(0).getCorrectOptionIndex());
    }

    @Test
    void shouldReturnEmptyWhenOnlyMalformedBlocksAreReturned() {
        stubAiResponse("""
                This is not the expected format.

                ---

                QUESTION: Missing answer
                A: Option A
                B: Option B
                C: Option C
                D: Option D
                """);

        Optional<List<QuizQuestion>> result =
                aiQuizService.generateQuizQuestions("Rhythm", "Lesson material", 1);

        assertTrue(result.isEmpty());
    }

    private void stubAiResponse(String aiResponse) {
        when(chatClient
                .prompt()
                .system(anyString())
                .user(anyString())
                .call()
                .content())
                .thenReturn(aiResponse);
    }
}