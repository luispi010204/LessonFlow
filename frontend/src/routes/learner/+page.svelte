<script>
	let { data } = $props();

	let user = data.user;
	let isAuthenticated = data.isAuthenticated;
	let isLearner = isAuthenticated && user.user_roles && user.user_roles.includes('learner');
</script>

<div class="d-flex justify-content-between align-items-center mb-4">
	<div>
		<h1 class="mb-1">My Learning</h1>
		<p class="text-muted mb-0">Continue your enrolled courses and current lessons.</p>
	</div>
</div>

{#if !isAuthenticated}
	<div class="alert alert-warning">
		You need to log in to view your learning dashboard.
	</div>
	<a href="/login" class="btn btn-primary">Login</a>
{:else if !isLearner}
	<div class="alert alert-info">
		This page is intended for learners. Tutors can manage courses in the Tutor Dashboard.
	</div>
	<a href="/tutor" class="btn btn-primary">Go to Tutor Dashboard</a>
{:else}
	<div class="row g-3 mb-4">
		<div class="col-md-4">
			<div class="card shadow-sm h-100">
				<div class="card-body">
					<h5 class="card-title">Enrolled Courses</h5>
					<p class="display-6 mb-0">—</p>
					<p class="text-muted mb-0">Will be loaded from the backend.</p>
				</div>
			</div>
		</div>

		<div class="col-md-4">
			<div class="card shadow-sm h-100">
				<div class="card-body">
					<h5 class="card-title">Current Lesson</h5>
					<p class="display-6 mb-0">—</p>
					<p class="text-muted mb-0">Next open lesson will appear here.</p>
				</div>
			</div>
		</div>

		<div class="col-md-4">
			<div class="card shadow-sm h-100">
				<div class="card-body">
					<h5 class="card-title">Progress</h5>
					<p class="display-6 mb-0">—</p>
					<p class="text-muted mb-0">Progress summary will be shown here.</p>
				</div>
			</div>
		</div>
	</div>

	<div class="card shadow-sm">
		<div class="card-header">
			<h5 class="mb-0">My Courses</h5>
		</div>

		<div class="card-body">
			<p>
				This section will list all enrollments for the current learner.
			</p>

			<div class="border rounded p-3 bg-light">
				<h6>Example Enrollment Card</h6>
				<p class="mb-2">
					Course title, current lesson and progress status will be displayed here.
				</p>
				<button class="btn btn-primary" disabled>Continue Learning</button>
			</div>
		</div>
	</div>
{/if}