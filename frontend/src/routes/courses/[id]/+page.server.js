import axios from 'axios';
import { redirect } from '@sveltejs/kit';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

export async function load({ params, locals }) {
	const jwt_token = locals.jwt_token;
	const courseId = params.id;

	if (!jwt_token) {
		return {
			course: null,
			lessons: [],
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

		return {
			course: courseResponse.data,
			lessons: lessonsResponse.data,
			error: null
		};
	} catch (error) {
		console.log('Error loading course detail:', error?.response?.data || error);

		return {
			course: null,
			lessons: [],
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

			console.log('Error enrolling in course:', error?.response?.data || error);

			return {
				error: 'Could not enroll in course. You may already be enrolled.'
			};
		}
	}
};