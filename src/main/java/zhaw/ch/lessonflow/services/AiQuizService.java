package zhaw.ch.lessonflow.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import zhaw.ch.lessonflow.model.QuizQuestion;

@Service
public class AiQuizService {

    private final ChatClient chatClient;

    public AiQuizService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public Optional<List<QuizQuestion>> generateQuizQuestions(
            String lessonTitle,
            String lessonMaterial,
            int questionCount) {

        if (lessonMaterial == null || lessonMaterial.isBlank()) {
            return Optional.empty();
        }

        if (questionCount < 1 || questionCount > 10) {
            return Optional.empty();
        }

        String aiResponse = chatClient.prompt()
                .system("""
                        You are an assistant for an educational learning platform.
                        Your task is to generate single-choice quiz questions from lesson material.

                        Return ONLY plain text in exactly this format:

                        QUESTION: question text
                        A: first option
                        B: second option
                        C: third option
                        D: fourth option
                        ANSWER: A

                        ---

                        Rules:
                        - Generate exactly the requested number of questions.
                        - Each question must have exactly four answer options.
                        - Exactly one answer must be correct.
                        - The correct answer must be A, B, C or D.
                        - Do not include markdown.
                        - Do not include explanations.
                        - Do not include numbering.
                        """)
                .user(buildPrompt(lessonTitle, lessonMaterial, questionCount))
                .call()
                .content();

        List<QuizQuestion> questions = parseQuizQuestions(aiResponse);

        if (questions.size() != questionCount) {
            return Optional.empty();
        }

        return Optional.of(questions);
    }

    private String buildPrompt(String lessonTitle, String lessonMaterial, int questionCount) {
        return """
                Generate %d single-choice quiz questions for this lesson.

                Lesson title:
                %s

                Lesson material:
                %s
                """.formatted(questionCount, lessonTitle, lessonMaterial);
    }

    private List<QuizQuestion> parseQuizQuestions(String aiResponse) {
        List<QuizQuestion> questions = new ArrayList<>();

        if (aiResponse == null || aiResponse.isBlank()) {
            return questions;
        }

        String[] blocks = aiResponse.split("\\s*---\\s*");

        for (String block : blocks) {
            Optional<QuizQuestion> parsedQuestion = parseQuestionBlock(block);

            if (parsedQuestion.isPresent()) {
                questions.add(parsedQuestion.get());
            }
        }

        return questions;
    }

    private Optional<QuizQuestion> parseQuestionBlock(String block) {
        if (block == null || block.isBlank()) {
            return Optional.empty();
        }

        String questionText = null;
        String optionA = null;
        String optionB = null;
        String optionC = null;
        String optionD = null;
        String answer = null;

        String[] lines = block.split("\\R");

        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.startsWith("QUESTION:")) {
                questionText = line.substring("QUESTION:".length()).trim();
            } else if (line.startsWith("A:")) {
                optionA = line.substring("A:".length()).trim();
            } else if (line.startsWith("B:")) {
                optionB = line.substring("B:".length()).trim();
            } else if (line.startsWith("C:")) {
                optionC = line.substring("C:".length()).trim();
            } else if (line.startsWith("D:")) {
                optionD = line.substring("D:".length()).trim();
            } else if (line.startsWith("ANSWER:")) {
                answer = line.substring("ANSWER:".length()).trim();
            }
        }

        if (isBlank(questionText)
                || isBlank(optionA)
                || isBlank(optionB)
                || isBlank(optionC)
                || isBlank(optionD)
                || isBlank(answer)) {
            return Optional.empty();
        }

        int correctOptionIndex = mapAnswerToIndex(answer);

        if (correctOptionIndex < 0) {
            return Optional.empty();
        }

        QuizQuestion quizQuestion = new QuizQuestion(
                questionText,
                List.of(optionA, optionB, optionC, optionD),
                correctOptionIndex
        );

        return Optional.of(quizQuestion);
    }

    private int mapAnswerToIndex(String answer) {
        String normalizedAnswer = answer.trim().toUpperCase();

        if (normalizedAnswer.equals("A")) {
            return 0;
        }

        if (normalizedAnswer.equals("B")) {
            return 1;
        }

        if (normalizedAnswer.equals("C")) {
            return 2;
        }

        if (normalizedAnswer.equals("D")) {
            return 3;
        }

        return -1;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}