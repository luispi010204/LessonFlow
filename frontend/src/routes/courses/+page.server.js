import axios from 'axios';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

export async function load({ locals }) {
	const jwt_token = locals.jwt_token;

	if (!jwt_token) {
		return {
			courses: [],
			error: 'Authentication required'
		};
	}

	try {
		const response = await axios({
			method: 'get',
			url: `${API_BASE_URL}/api/course`,
			headers: {
				Authorization: 'Bearer ' + jwt_token
			}
		});

		return {
			courses: response.data,
			error: null
		};
	} catch (error) {
		console.log('Error loading courses:', error?.response?.data || error);

		return {
			courses: [],
			error: 'Could not load courses'
		};
	}
}