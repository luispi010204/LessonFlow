<script>
	let { data } = $props();

	let user = data.user;
	let isAuthenticated = data.isAuthenticated;
	let isLearner = isAuthenticated && user.user_roles && user.user_roles.includes('learner');

	let enrollmentCards = data.enrollmentCards || [];
	let error = data.error;
</script>

<div class="d-flex justify-content-between align-items-center mb-4">
	<div>
		<h1 class="mb-1">My Learning</h1>
		<p class="text-muted mb-0">Continue your enrolled courses and current lessons.</p>
	</div>

	<a href="/courses" class="btn btn-outline-primary">Browse Courses</a>
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
	{#if error}
		<div class="alert alert-danger">
			{error}
		</div>
	{/if}

	<div class="row g-3 mb-4">
		<div class="col-md-4">
			<div class="card shadow-sm h-100">
				<div class="card-body">
					<h5 class="card-title">Enrolled Courses</h5>
					<p class="display-6 mb-0">{enrollmentCards.length}</p>
					<p class="text-muted mb-0">Courses you are currently enrolled in.</p>
				</div>
			</div>
		</div>

		<div class="col-md-4">
			<div class="card shadow-sm h-100">
				<div class="card-body">
					<h5 class="card-title">Current Lessons</h5>
					<p class="display-6 mb-0">—</p>
					<p class="text-muted mb-0">Shown inside each learning flow.</p>
				</div>
			</div>
		</div>

		<div class="col-md-4">
			<div class="card shadow-sm h-100">
				<div class="card-body">
					<h5 class="card-title">Progress</h5>
					<p class="display-6 mb-0">—</p>
					<p class="text-muted mb-0">Detailed progress is shown per course.</p>
				</div>
			</div>
		</div>
	</div>

	<div class="card shadow-sm">
		<div class="card-header">
			<h5 class="mb-0">My Courses</h5>
		</div>

		<div class="card-body">
			{#if enrollmentCards.length === 0}
				<p class="text-muted">
					You are not enrolled in any courses yet.
				</p>

				<a href="/courses" class="btn btn-primary">Browse Courses</a>
			{:else}
				<div class="row g-3">
					{#each enrollmentCards as card}
						<div class="col-md-6">
							<div class="card h-100 border">
								<div class="card-body d-flex flex-column">
									<h5 class="card-title">
										{card.course ? card.course.title : 'Unknown Course'}
									</h5>

									<p class="card-text text-muted">
										{card.course ? card.course.description : 'Course details could not be loaded.'}
									</p>

									<p class="mb-2">
										<strong>Status:</strong> {card.enrollment.status}
									</p>

									<p class="text-muted small">
										Enrollment ID: {card.enrollment.id}
									</p>

									<div class="mt-auto">
										<a
											href={`/learner/enrollments/${card.enrollment.id}`}
											class="btn btn-primary"
										>
											Continue Learning
										</a>
									</div>
								</div>
							</div>
						</div>
					{/each}
				</div>
			{/if}
		</div>
	</div>
{/if}