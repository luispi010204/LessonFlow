import axios from 'axios';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

function buildQuizResult(attempt, quiz) {
	const selectedOptionIndexes = attempt.selectedOptionIndexes || [];
	const questions = quiz.questions || [];

	let correctAnswers = 0;

	const questionResults = questions.map((question, questionIndex) => {
		const selectedOptionIndex = selectedOptionIndexes[questionIndex];
		const correctOptionIndex = question.correctOptionIndex;
		const isCorrect = selectedOptionIndex === correctOptionIndex;

		if (isCorrect) {
			correctAnswers++;
		}

		return {
			questionText: question.questionText,
			options: question.options || [],
			selectedOptionIndex,
			correctOptionIndex,
			isCorrect
		};
	});

	const scorePercent = Number(attempt.scorePercent || 0);
	const roundedScorePercent = Math.round(scorePercent * 10) / 10;

	return {
		attemptId: attempt.id || null,
		resultKey: attempt.id || `${Date.now()}`,
		scorePercent: roundedScorePercent,
		passed: attempt.passed,
		totalQuestions: questions.length,
		correctAnswers,
		wrongAnswers: questions.length - correctAnswers,
		questionResults
	};
}

function isCourseCompleted(progressSummary) {
	if (!progressSummary) {
		return false;
	}

	return (
		progressSummary.totalLessons > 0 &&
		progressSummary.passedLessons === progressSummary.totalLessons
	);
}

export async function load({ params, locals }) {
	const jwt_token = locals.jwt_token;
	const enrollmentId = params.id;

	if (!jwt_token) {
		return {
			enrollmentId,
			currentLesson: null,
			currentProgress: null,
			progressList: [],
			progressSummary: null,
			courseCompleted: false,
			quiz: null,
			error: 'Authentication required'
		};
	}

	try {
		const [progressResponse, summaryResponse] = await Promise.all([
			axios({
				method: 'get',
				url: `${API_BASE_URL}/api/progress/enrollment/${enrollmentId}`,
				headers: {
					Authorization: 'Bearer ' + jwt_token
				}
			}),
			axios({
				method: 'get',
				url: `${API_BASE_URL}/api/enrollment/${enrollmentId}/progress-summary`,
				headers: {
					Authorization: 'Bearer ' + jwt_token
				}
			})
		]);

		const progressList = progressResponse.data || [];
		const progressSummary = summaryResponse.data;
		const courseCompleted = isCourseCompleted(progressSummary);

		let currentLesson = null;
		let currentProgress = null;
		let quiz = null;

		try {
			const currentLessonResponse = await axios({
				method: 'get',
				url: `${API_BASE_URL}/api/enrollment/${enrollmentId}/current-lesson`,
				headers: {
					Authorization: 'Bearer ' + jwt_token
				}
			});

			currentLesson = currentLessonResponse.data;

			currentProgress = progressList.find(
				(progress) => progress.lessonId === currentLesson.id
			);

			if (currentLesson?.id) {
				try {
					const quizResponse = await axios({
						method: 'get',
						url: `${API_BASE_URL}/api/quiz/lesson/${currentLesson.id}`,
						headers: {
							Authorization: 'Bearer ' + jwt_token
						}
					});

					quiz = quizResponse.data;
				} catch (error) {
					if (error?.response?.status !== 404) {
						console.log('Error loading quiz for current lesson:', error?.response?.data || error);
					}
				}
			}
		} catch (error) {
			if (error?.response?.status !== 404) {
				console.log('Error loading current lesson:', error?.response?.data || error);
			}
		}

		return {
			enrollmentId,
			currentLesson,
			currentProgress,
			progressList,
			progressSummary,
			courseCompleted,
			quiz,
			error: null
		};
	} catch (error) {
		console.log('Error loading learning flow:', error?.response?.data || error);

		return {
			enrollmentId,
			currentLesson: null,
			currentProgress: null,
			progressList: [],
			progressSummary: null,
			courseCompleted: false,
			quiz: null,
			error: 'Could not load learning flow'
		};
	}
}

export const actions = {
	materialDone: async ({ request, locals }) => {
		const jwt_token = locals.jwt_token;
		const data = await request.formData();

		const progressId = data.get('progressId');

		if (!jwt_token) {
			return {
				error: 'Authentication required'
			};
		}

		if (!progressId) {
			return {
				error: 'Progress ID is missing'
			};
		}

		try {
			await axios({
				method: 'post',
				url: `${API_BASE_URL}/api/progress/${progressId}/material-done`,
				headers: {
					Authorization: 'Bearer ' + jwt_token
				}
			});

			return {
				success: 'Material marked as done'
			};
		} catch (error) {
			console.log('Error marking material done:', error?.response?.data || error);

			return {
				error: 'Could not mark material as done'
			};
		}
	},

	submitQuizAttempt: async ({ request, locals, params }) => {
		const jwt_token = locals.jwt_token;
		const enrollmentId = params.id;
		const data = await request.formData();

		const quizId = data.get('quizId');
		const lessonId = data.get('lessonId');
		const questionCount = Number(data.get('questionCount'));

		if (!jwt_token) {
			return {
				error: 'Authentication required'
			};
		}

		if (!quizId || !lessonId || Number.isNaN(questionCount) || questionCount < 1) {
			return {
				error: 'Quiz attempt data is missing'
			};
		}

		const selectedOptionIndexes = [];

		for (let i = 0; i < questionCount; i++) {
			const selectedOptionIndex = data.get(`selectedOptionIndex-${i}`);
			const parsedSelectedOptionIndex = Number(selectedOptionIndex);

			if (selectedOptionIndex === null || Number.isNaN(parsedSelectedOptionIndex)) {
				return {
					error: 'Please answer all quiz questions'
				};
			}

			selectedOptionIndexes.push(parsedSelectedOptionIndex);
		}

		try {
			const attemptResponse = await axios({
				method: 'post',
				url: `${API_BASE_URL}/api/attempt/submit`,
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + jwt_token
				},
				data: {
					quizId,
					enrollmentId,
					lessonId,
					selectedOptionIndexes
				}
			});

			const attempt = attemptResponse.data;

			const quizResponse = await axios({
				method: 'get',
				url: `${API_BASE_URL}/api/quiz/${quizId}`,
				headers: {
					Authorization: 'Bearer ' + jwt_token
				}
			});

			const quiz = quizResponse.data;
			const quizResult = buildQuizResult(attempt, quiz);

			return {
				success: 'Quiz attempt submitted',
				attempt,
				quizResult
			};
		} catch (error) {
			console.log('Error submitting quiz attempt:', error?.response?.data || error);

			return {
				error: 'Could not submit quiz attempt'
			};
		}
	}
};