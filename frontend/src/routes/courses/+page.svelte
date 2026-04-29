<script>
	let { data } = $props();

	let user = data.user;
	let isAuthenticated = data.isAuthenticated;
	let isLearner = isAuthenticated && user.user_roles && user.user_roles.includes('learner');

	let courses = data.courses || [];
	let error = data.error;
</script>

<div class="d-flex justify-content-between align-items-center mb-4">
	<div>
		<h1 class="mb-1">Courses</h1>
		<p class="text-muted mb-0">Browse available courses and open course details.</p>
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
	{#if error}
		<div class="alert alert-danger">
			{error}
		</div>
	{/if}

	{#if courses.length === 0}
		<div class="card shadow-sm">
			<div class="card-body">
				<h5 class="card-title">No courses available</h5>
				<p class="card-text text-muted mb-0">
					There are currently no courses to display.
				</p>
			</div>
		</div>
	{:else}
		<div class="row g-3">
			{#each courses as course}
				<div class="col-md-6 col-lg-4">
					<div class="card shadow-sm h-100">
						<div class="card-body d-flex flex-column">
							<div class="d-flex justify-content-between align-items-start mb-2">
								<h5 class="card-title mb-0">{course.title}</h5>

								{#if course.status}
									<span class="badge bg-secondary">{course.status}</span>
								{/if}
							</div>

							<p class="card-text text-muted">
								{course.description}
							</p>

							<div class="mt-auto">
								<a href={`/courses/${course.id}`} class="btn btn-primary">
									View Details
								</a>
							</div>
						</div>
					</div>
				</div>
			{/each}
		</div>
	{/if}
{/if}