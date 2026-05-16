import axios from 'axios';
import { redirect } from '@sveltejs/kit';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

function getRolesFromJwt(jwt_token) {
	if (!jwt_token) {
		return [];
	}

	try {
		const payload = jwt_token.split('.')[1];
		const decodedPayload = JSON.parse(Buffer.from(payload, 'base64url').toString('utf-8'));

		return decodedPayload.user_roles || [];
	} catch (error) {
		console.log('Error reading roles from JWT:', error);
		return [];
	}
}

async function getEnrollmentForCourse(jwt_token, courseId) {
	try {
		const enrollmentsResponse = await axios({
			method: 'get',
			url: `${API_BASE_URL}/api/enrollment/me`,
			headers: {
				Authorization: 'Bearer ' + jwt_token
			}
		});

		const enrollments = enrollmentsResponse.data || [];

		return enrollments.find((enrollment) => enrollment.courseId === courseId) || null;
	} catch (error) {
		return null;
	}
}

export async function load({ params, locals }) {
	const jwt_token = locals.jwt_token;
	const courseId = params.id;

	if (!jwt_token) {
		return {
			course: null,
			lessons: [],
			enrollment: null,
			isEnrolled: false,
			lessonsAvailable: false,
			error: 'Authentication required'
		};
	}

	const roles = getRolesFromJwt(jwt_token);
	const isLearner = roles.includes('learner');
	const isTutor = roles.includes('tutor');

	try {
		const courseResponse = await axios({
			method: 'get',
			url: `${API_BASE_URL}/api/course/${courseId}`,
			headers: {
				Authorization: 'Bearer ' + jwt_token
			}
		});

		let enrollment = null;
		let lessons = [];
		let lessonsAvailable = false;

		if (isLearner) {
			enrollment = await getEnrollmentForCourse(jwt_token, courseId);
		}

		if (isTutor || enrollment) {
			try {
				const lessonsResponse = await axios({
					method: 'get',
					url: `${API_BASE_URL}/api/lesson/course/${courseId}`,
					headers: {
						Authorization: 'Bearer ' + jwt_token
					}
				});

				lessons = lessonsResponse.data || [];
				lessonsAvailable = true;
			} catch (error) {
				console.log('Error loading course lessons:', error?.response?.data || error);
			}
		}

		return {
			course: courseResponse.data,
			lessons,
			enrollment,
			isEnrolled: enrollment !== null,
			lessonsAvailable,
			error: null
		};
	} catch (error) {
		console.log('Error loading course detail:', error?.response?.data || error);

		return {
			course: null,
			lessons: [],
			enrollment: null,
			isEnrolled: false,
			lessonsAvailable: false,
			error: 'Could not load course details'
		};
	}
}

export const actions = {
	enroll: async ({ params, locals }) => {
		const jwt_token = locals.jwt_token;
		const courseId = params.id;

		if (!jwt_token) {
			return {
				error: 'Authentication required'
			};
		}

		try {
			const response = await axios({
				method: 'post',
				url: `${API_BASE_URL}/api/enrollment`,
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + jwt_token
				},
				data: {
					courseId: courseId
				}
			});

			const enrollment = response.data;

			throw redirect(302, `/learner/enrollments/${enrollment.id}`);
		} catch (error) {
			if (error?.status === 302) {
				throw error;
			}

			const existingEnrollment = await getEnrollmentForCourse(jwt_token, courseId);

			if (existingEnrollment) {
				throw redirect(302, `/learner/enrollments/${existingEnrollment.id}`);
			}

			console.log('Error enrolling in course:', error?.response?.data || error);

			return {
				error: 'Could not enroll in course. You may already be enrolled.'
			};
		}
	}
};