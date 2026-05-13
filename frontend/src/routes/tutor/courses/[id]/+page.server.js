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
			enrollments: [],
			enrollmentProgressCards: [],
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

		const enrollmentsResponse = await axios({
			method: 'get',
			url: `${API_BASE_URL}/api/enrollment/course/${courseId}`,
			headers: {
				Authorization: 'Bearer ' + jwt_token
			}
		});

		const lessons = lessonsResponse.data || [];
		const enrollments = enrollmentsResponse.data || [];

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

		const enrollmentProgressCards = await Promise.all(
			enrollments.map(async (enrollment) => {
				try {
					const progressResponse = await axios({
						method: 'get',
						url: `${API_BASE_URL}/api/progress/enrollment/${enrollment.id}`,
						headers: {
							Authorization: 'Bearer ' + jwt_token
						}
					});

					const progressList = progressResponse.data || [];

					const progressCards = progressList.map((progress) => {
						const lesson = lessons.find((item) => item.id === progress.lessonId);

						return {
							progress,
							lesson
						};
					});

					return {
						enrollment,
						progressCards,
						error: null
					};
				} catch (error) {
					console.log(
						'Error loading progress for enrollment:',
						error?.response?.data || error
					);

					return {
						enrollment,
						progressCards: [],
						error: 'Could not load progress for this enrollment'
					};
				}
			})
		);

		return {
			courseId,
			course: courseResponse.data,
			lessons,
			lessonCards,
			enrollments,
			enrollmentProgressCards,
			error: null
		};
	} catch (error) {
		console.log('Error loading tutor course detail:', error?.response?.data || error);

		return {
			courseId,
			course: null,
			lessons: [],
			lessonCards: [],
			enrollments: [],
			enrollmentProgressCards: [],
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

	generateAiQuiz: async ({ request, locals }) => {
		const jwt_token = locals.jwt_token;

		if (!jwt_token) {
			return {
				error: 'Authentication required'
			};
		}

		const data = await request.formData();

		const lessonId = data.get('lessonId');
		const questionCount = Number(data.get('questionCount') || 3);
		const passPercent = Number(data.get('passPercent') || 70);

		if (!lessonId) {
			return {
				error: 'Lesson ID is missing'
			};
		}

		if (Number.isNaN(questionCount) || questionCount < 1 || questionCount > 10) {
			return {
				error: 'Question count must be between 1 and 10'
			};
		}

		if (Number.isNaN(passPercent) || passPercent < 1 || passPercent > 100) {
			return {
				error: 'Pass percentage must be between 1 and 100'
			};
		}

		try {
			await axios({
				method: 'post',
				url: `${API_BASE_URL}/api/quiz/lesson/${lessonId}/generate-ai`,
				headers: {
					Authorization: 'Bearer ' + jwt_token
				},
				params: {
					questionCount,
					passPercent
				}
			});

			return {
				success: 'AI quiz generated successfully'
			};
		} catch (error) {
			console.log('Error generating AI quiz:', error?.response?.data || error);

			return {
				error: 'Could not generate AI quiz. A quiz may already exist or the lesson material may be too short.'
			};
		}
	},

	confirmMeeting: async ({ request, locals }) => {
		const jwt_token = locals.jwt_token;

		if (!jwt_token) {
			return {
				error: 'Authentication required'
			};
		}

		const data = await request.formData();
		const progressId = data.get('progressId');

		if (!progressId) {
			return {
				error: 'Progress ID is missing'
			};
		}

		try {
			await axios({
				method: 'post',
				url: `${API_BASE_URL}/api/progress/${progressId}/meeting-done`,
				headers: {
					Authorization: 'Bearer ' + jwt_token
				}
			});

			return {
				success: 'Meeting confirmed successfully'
			};
		} catch (error) {
			console.log('Error confirming meeting:', error?.response?.data || error);

			return {
				error: 'Could not confirm meeting'
			};
		}
	}
};