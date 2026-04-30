import axios from 'axios';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

export async function load({ params, locals }) {
	const jwt_token = locals.jwt_token;
	const courseId = params.id;

	if (!jwt_token) {
		return {
			courseId,
			course: null,
			lessons: [],
			lessonCards: [],
			error: 'Authentication required'
		};
	}

	try {
		const courseResponse = await axios({
			method: 'get',
			url: `${API_BASE_URL}/api/course/${courseId}`,
			headers: {
				Authorization: 'Bearer ' + jwt_token
			}
		});

		const lessonsResponse = await axios({
			method: 'get',
			url: `${API_BASE_URL}/api/lesson/course/${courseId}`,
			headers: {
				Authorization: 'Bearer ' + jwt_token
			}
		});

		const lessons = lessonsResponse.data || [];

		const lessonCards = await Promise.all(
			lessons.map(async (lesson) => {
				try {
					const quizResponse = await axios({
						method: 'get',
						url: `${API_BASE_URL}/api/quiz/lesson/${lesson.id}`,
						headers: {
							Authorization: 'Bearer ' + jwt_token
						}
					});

					return {
						lesson,
						quiz: quizResponse.data
					};
				} catch (error) {
					return {
						lesson,
						quiz: null
					};
				}
			})
		);

		return {
			courseId,
			course: courseResponse.data,
			lessons,
			lessonCards,
			error: null
		};
	} catch (error) {
		console.log('Error loading tutor course detail:', error?.response?.data || error);

		return {
			courseId,
			course: null,
			lessons: [],
			lessonCards: [],
			error: 'Could not load course details'
		};
	}
}

export const actions = {
	createLesson: async ({ request, locals, params }) => {
		const jwt_token = locals.jwt_token;
		const courseId = params.id;

		if (!jwt_token) {
			return {
				error: 'Authentication required'
			};
		}

		const data = await request.formData();

		let nextLessonNumber = 1;

		try {
			const lessonsResponse = await axios({
				method: 'get',
				url: `${API_BASE_URL}/api/lesson/course/${courseId}`,
				headers: {
					Authorization: 'Bearer ' + jwt_token
				}
			});

			const existingLessons = lessonsResponse.data || [];

			if (existingLessons.length > 0) {
				nextLessonNumber =
					Math.max(...existingLessons.map((lesson) => lesson.lessonNumber)) + 1;
			}
		} catch (error) {
			console.log('Error calculating next lesson number:', error?.response?.data || error);

			return {
				error: 'Could not calculate next lesson number'
			};
		}

		const lesson = {
			courseId,
			lessonNumber: nextLessonNumber,
			title: data.get('title'),
			material: data.get('material'),
			meetingLink: data.get('meetingLink')
		};

		if (!lesson.title || !lesson.material || !lesson.meetingLink) {
			return {
				error: 'All lesson fields are required'
			};
		}

		try {
			await axios({
				method: 'post',
				url: `${API_BASE_URL}/api/lesson`,
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + jwt_token
				},
				data: lesson
			});

			return {
				success: `Lesson ${nextLessonNumber} created successfully`
			};
		} catch (error) {
			console.log('Error creating lesson:', error?.response?.data || error);

			return {
				error: 'Could not create lesson.'
			};
		}
	},

	createQuiz: async ({ request, locals }) => {
		const jwt_token = locals.jwt_token;

		if (!jwt_token) {
			return {
				error: 'Authentication required'
			};
		}

		const data = await request.formData();

		const lessonId = data.get('lessonId');
		const passPercent = Number(data.get('passPercent'));
		const questionsText = data.get('questions');

		if (!lessonId || !passPercent || !questionsText) {
			return {
				error: 'Lesson, pass percentage and questions are required'
			};
		}

		const questions = questionsText
			.split('\n')
			.map((question) => question.trim())
			.filter((question) => question.length > 0);

		if (questions.length === 0) {
			return {
				error: 'At least one question is required'
			};
		}

		const quiz = {
			lessonId,
			passPercent,
			questions
		};

		try {
			await axios({
				method: 'post',
				url: `${API_BASE_URL}/api/quiz`,
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + jwt_token
				},
				data: quiz
			});

			return {
				success: 'Quiz created successfully'
			};
		} catch (error) {
			console.log('Error creating quiz:', error?.response?.data || error);

			return {
				error: 'Could not create quiz. A quiz may already exist for this lesson.'
			};
		}
	}
};