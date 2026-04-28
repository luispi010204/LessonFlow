<script>
	let { data } = $props();

	let user = data.user;
	let isAuthenticated = data.isAuthenticated;
	let isLearner = isAuthenticated && user.user_roles && user.user_roles.includes('learner');
</script>

<div class="d-flex justify-content-between align-items-center mb-4">
	<div>
		<h1 class="mb-1">Courses</h1>
		<p class="text-muted mb-0">Browse available courses and enroll as a learner.</p>
	</div>
</div>

{#if !isAuthenticated}
	<div class="alert alert-warning">
		You need to log in to view available courses.
	</div>
	<a href="/login" class="btn btn-primary">Login</a>
{:else if !isLearner}
	<div class="alert alert-info">
		The course catalog is mainly intended for learners. Tutors manage their own courses in the Tutor Dashboard.
	</div>
	<a href="/tutor" class="btn btn-primary">Go to Tutor Dashboard</a>
{:else}
	<div class="card shadow-sm mb-3">
		<div class="card-body">
			<h5 class="card-title">Available Courses</h5>
			<p class="card-text">
				This page will display all available courses from the backend.
			</p>
			<p class="text-muted mb-0">
				In the next frontend issue, this page will load real course data and provide enrollment actions.
			</p>
		</div>
	</div>

	<div class="row g-3">
		<div class="col-md-6">
			<div class="card h-100 border-dashed">
				<div class="card-body">
					<h5 class="card-title">Example Course Card</h5>
					<p class="card-text">
						Course title, description and status will be shown here.
					</p>
					<button class="btn btn-outline-primary" disabled>View Details</button>
					<button class="btn btn-primary" disabled>Enroll</button>
				</div>
			</div>
		</div>
	</div>
{/if}