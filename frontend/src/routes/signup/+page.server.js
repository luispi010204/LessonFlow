import { redirect } from '@sveltejs/kit';
import auth from '$lib/server/auth.service.js';

export const actions = {
	default: async ({ request, cookies }) => {
		const data = await request.formData();

		const email = data.get('email');
		const password = data.get('password');
		const firstName = data.get('firstName');
		const lastName = data.get('lastName');

		if (!email || !password) {
			return {
				error: 'Email and password are required.'
			};
		}

		try {
			await auth.signup(email, password, firstName, lastName, cookies);
		} catch (error) {
			console.error('Signup error:', error?.response?.data || error);
			return {
				error: 'Signup failed. Please check your input.'
			};
		}

		throw redirect(302, '/');
	}
};