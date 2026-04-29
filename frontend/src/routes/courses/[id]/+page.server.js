import axios from 'axios';
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