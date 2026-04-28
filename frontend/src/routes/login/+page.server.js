import { redirect } from '@sveltejs/kit';
import auth from '$lib/server/auth.service.js';

export const actions = {
	default: async ({ request, cookies }) => {
		const data = await request.formData();

		const email = data.get('email');
		const password = data.get('password');

		if (!email || !password) {
			return {
				error: 'Email and password are required.'
			};
		}

		try {
			await auth.login(email, password, cookies);
		} catch (error) {
			console.error('Login error:', error?.response?.data || error);
			return {
				error: 'Login failed. Please check your credentials.'
			};
		}

		throw redirect(302, '/');
	}
};