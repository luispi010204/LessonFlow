<script>
	let { data } = $props();

	let enrollmentId = data.enrollmentId;
	let user = data.user;
	let isAuthenticated = data.isAuthenticated;
	let isLearner = isAuthenticated && user.user_roles && user.user_roles.includes('learner');
</script>

<div class="d-flex justify-content-between align-items-center mb-4">
	<div>
		<h1 class="mb-1">Learning Flow</h1>
		<p class="text-muted mb-0">Enrollment ID: {enrollmentId}</p>
	</div>

	<a href="/learner" class="btn btn-outline-secondary">Back to My Learning</a>
</div>

{#if !isAuthenticated}
	<div class="alert alert-warning">
		You need to log in to access this learning flow.
	</div>
	<a href="/login" class="btn btn-primary">Login</a>
{:else if !isLearner}
	<div class="alert alert-info">
		This page is intended for learners.
	</div>
{:else}
	<div class="row g-3">
		<div class="col-lg-8">
			<div class="card shadow-sm mb-3">
				<div class="card-header">
					<h5 class="mb-0">Current Lesson</h5>
				</div>

				<div class="card-body">
					<p class="text-muted">
						The current unlocked lesson will be loaded from the backend here.
					</p>

					<h5>Example Lesson Title</h5>
					<p>
						Lesson material will be displayed here. The learner will read the material,
						then mark it as completed.
					</p>

					<button class="btn btn-primary" disabled>Mark Material Done</button>
				</div>
			</div>

			<div class="card shadow-sm mb-3">
				<div class="card-header">
					<h5 class="mb-0">Meeting</h5>
				</div>

				<div class="card-body">
					<p class="text-muted">
						The meeting link for the current lesson will be displayed here.
					</p>

					<button class="btn btn-outline-primary" disabled>Confirm Meeting Done</button>
				</div>
			</div>

			<div class="card shadow-sm">
				<div class="card-header">
					<h5 class="mb-0">Quiz Attempt</h5>
				</div>

				<div class="card-body">
					<p class="text-muted">
						After material and meeting are completed, the learner can submit a quiz attempt here.
					</p>

					<div class="mb-3">
						<label class="form-label" for="score">Score Percent</label>
						<input id="score" class="form-control" type="number" disabled />
					</div>

					<button class="btn btn-success" disabled>Submit Quiz Attempt</button>
				</div>
			</div>
		</div>

		<div class="col-lg-4">
			<div class="card shadow-sm">
				<div class="card-header">
					<h5 class="mb-0">Progress Summary</h5>
				</div>

				<div class="card-body">
					<p class="text-muted mb-0">
						Total lessons, passed lessons and current lesson will be shown here.
					</p>
				</div>
			</div>
		</div>
	</div>
{/if}