import axios from 'axios';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

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
			quiz: null,
			error: 'Authentication required'
		};
	}

	try {
		const [currentLessonResponse, progressResponse, summaryResponse] = await Promise.all([
			axios({
				method: 'get',
				url: `${API_BASE_URL}/api/enrollment/${enrollmentId}/current-lesson`,
				headers: {
					Authorization: 'Bearer ' + jwt_token
				}
			}),
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

		const currentLesson = currentLessonResponse.data;
		const progressList = progressResponse.data;
		const progressSummary = summaryResponse.data;

		const currentProgress = progressList.find(
			(progress) => progress.lessonId === currentLesson.id
		);

		let quiz = null;

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
				console.log('No quiz found for current lesson:', error?.response?.data || error);
			}
		}

		return {
			enrollmentId,
			currentLesson,
			currentProgress,
			progressList,
			progressSummary,
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
		const scorePercent = data.get('scorePercent');

		if (!jwt_token) {
			return {
				error: 'Authentication required'
			};
		}

		if (!quizId || !lessonId || !scorePercent) {
			return {
				error: 'Quiz attempt data is missing'
			};
		}

		try {
			await axios({
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
					scorePercent: Number(scorePercent)
				}
			});

			return {
				success: 'Quiz attempt submitted'
			};
		} catch (error) {
			console.log('Error submitting quiz attempt:', error?.response?.data || error);

			return {
				error: 'Could not submit quiz attempt'
			};
		}
	}
};