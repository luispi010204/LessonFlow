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
			url: `${API_BASE_URL}/api/course/me`,
			headers: {
				Authorization: 'Bearer ' + jwt_token
			}
		});

		return {
			courses: response.data,
			error: null
		};
	} catch (error) {
		console.log('Error loading tutor courses:', error?.response?.data || error);

		return {
			courses: [],
			error: 'Could not load tutor courses'
		};
	}
}

export const actions = {
	createCourse: async ({ request, locals }) => {
		const jwt_token = locals.jwt_token;

		if (!jwt_token) {
			return {
				error: 'Authentication required'
			};
		}

		const data = await request.formData();

		const course = {
			title: data.get('title'),
			description: data.get('description')
		};

		if (!course.title || !course.description) {
			return {
				error: 'Title and description are required'
			};
		}

		try {
			await axios({
				method: 'post',
				url: `${API_BASE_URL}/api/course`,
				headers: {
					'Content-Type': 'application/json',
					Authorization: 'Bearer ' + jwt_token
				},
				data: course
			});

			return {
				success: 'Course created successfully'
			};
		} catch (error) {
			console.log('Error creating course:', error?.response?.data || error);

			return {
				error: 'Could not create course'
			};
		}
	}
};