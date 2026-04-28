export const load = async ({ params, locals }) => {
	return {
		courseId: params.id,
		user: locals.user || {},
		isAuthenticated: locals.isAuthenticated || false
	};
};