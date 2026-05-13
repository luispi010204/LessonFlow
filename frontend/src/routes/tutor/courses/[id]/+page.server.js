import axios from 'axios';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

function buildQuizQuestionsFromForm(data) {
	const questionCount = Number(data.get('questionCount'));

	if (Number.isNaN(questionCount) || questionCount < 1) {
		return null;
	}

	const questions = [];

	for (let i = 0; i < questionCount; i++) {
		const questionText = data.get(`questionText-${i}`);
		const optionA = data.get(`questionOptionA-${i}`);
		const optionB = data.get(`questionOptionB-${i}`);
		const optionC = data.get(`questionOptionC-${i}`);
		const optionD = data.get(`questionOptionD-${i}`);
		const correctOptionIndex = Number(data.get(`correctOptionIndex-${i}`));

		const options = [optionA, optionB, optionC, optionD].map((option) =>
			option ? option.toString().trim() : ''
		);

		if (
			!questionText ||
			questionText.toString().trim().length === 0 ||
			options.some((option) => option.length === 0) ||
			Number.isNaN(correctOptionIndex) ||
			correctOptionIndex < 0 ||
			correctOptionIndex > 3
		) {
			return null;
		}

		questions.push({
			questionText: questionText.toString().trim(),
			options,
			correctOptionIndex
		});
	}

	return questions;
}

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

	updateLesson: async ({ request, locals }) => {
		const jwt_token = locals.jwt_token;

		if (!jwt_token) {
			return {
				error: 'Authentication required'
			};
		}

		const data = await request.formData();

		const lessonId = data.get('lessonId');

		const lesson = {
			title: data.get('title'),
			material: data.get('material'),
			meetingLink: data.get('meetingLink')
		};

		if (!lessonId) {
			return {
				error: 'Lesson ID is missing'
			};
		}

		if (!lesson.title || !lesson.material || !lesson.meetingLink) {
			return {
				error: 'All lesson fields are required'
			};
		}

		try {
			await axios({
				method: 'put',
				url: `${API_BASE_URL}/api/lesson/${lessonId}`,
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + jwt_token
				},
				data: lesson
			});

			return {
				success: 'Lesson updated successfully'
			};
		} catch (error) {
			console.log('Error updating lesson:', error?.response?.data || error);

			return {
				error: 'Could not update lesson'
			};
		}
	},

	updateQuiz: async ({ request, locals }) => {
		const jwt_token = locals.jwt_token;

		if (!jwt_token) {
			return {
				error: 'Authentication required'
			};
		}

		const data = await request.formData();

		const quizId = data.get('quizId');
		const passPercent = Number(data.get('passPercent'));
		const questions = buildQuizQuestionsFromForm(data);

		if (!quizId) {
			return {
				error: 'Quiz ID is missing'
			};
		}

		if (Number.isNaN(passPercent) || passPercent < 1 || passPercent > 100) {
			return {
				error: 'Pass percentage must be between 1 and 100'
			};
		}

		if (!questions || questions.length === 0) {
			return {
				error: 'Please provide complete quiz questions with four options and one correct answer'
			};
		}

		try {
			await axios({
				method: 'put',
				url: `${API_BASE_URL}/api/quiz/${quizId}`,
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + jwt_token
				},
				data: {
					passPercent,
					questions
				}
			});

			return {
				success: 'Quiz updated successfully'
			};
		} catch (error) {
			console.log('Error updating quiz:', error?.response?.data || error);

			return {
				error: 'Could not update quiz'
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
				error:
					'Could not generate AI quiz. A quiz may already exist or the lesson material may be too short.'
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