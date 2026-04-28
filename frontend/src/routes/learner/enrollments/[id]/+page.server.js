export const load = async ({ params, locals }) => {
	return {
		enrollmentId: params.id,
		user: locals.user || {},
		isAuthenticated: locals.isAuthenticated || false
	};
};