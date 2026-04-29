import axios from 'axios';
import 'dotenv/config';

const API_BASE_URL = process.env.API_BASE_URL;

export async function load({ locals }) {
	const jwt_token = locals.jwt_token;

	if (!jwt_token) {
		return {
			enrollments: [],
			enrollmentCards: [],
			error: 'Authentication required'
		};
	}

	try {
		const enrollmentResponse = await axios({
			method: 'get',
			url: `${API_BASE_URL}/api/enrollment/me`,
			headers: {
				Authorization: 'Bearer ' + jwt_token
			}
		});

		const enrollments = enrollmentResponse.data;

		const enrollmentCards = await Promise.all(
			enrollments.map(async (enrollment) => {
				try {
					const courseResponse = await axios({
						method: 'get',
						url: `${API_BASE_URL}/api/course/${enrollment.courseId}`,
						headers: {
							Authorization: 'Bearer ' + jwt_token
						}
					});

					return {
						enrollment,
						course: courseResponse.data
					};
				} catch (error) {
					console.log('Error loading course for enrollment:', error?.response?.data || error);

					return {
						enrollment,
						course: null
					};
				}
			})
		);

		return {
			enrollments,
			enrollmentCards,
			error: null
		};
	} catch (error) {
		console.log('Error loading enrollments:', error?.response?.data || error);

		return {
			enrollments: [],
			enrollmentCards: [],
			error: 'Could not load your enrollments'
		};
	}
}