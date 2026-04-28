<script>
	let { data } = $props();

	let user = data.user;
	let isAuthenticated = data.isAuthenticated;

	let isTutor = isAuthenticated && user.user_roles && user.user_roles.includes('tutor');
	let isLearner = isAuthenticated && user.user_roles && user.user_roles.includes('learner');
</script>

{#if isAuthenticated}
	<div class="row">
		<div class="col-12">
			<div class="card shadow-sm mb-4">
				<div class="card-body">
					<h1 class="card-title">Welcome to LessonFlow</h1>
					<p class="card-text">
						You are logged in as <strong>{user.name}</strong>.
					</p>

					{#if user.user_roles && user.user_roles.length > 0}
						<p class="mb-0">
							<strong>Roles:</strong> {user.user_roles.join(', ')}
						</p>
					{/if}
				</div>
			</div>
		</div>
	</div>

	<div class="row g-3">
		{#if isLearner}
			<div class="col-md-6">
				<div class="card shadow-sm h-100">
					<div class="card-body">
						<h3 class="card-title">Browse Courses</h3>
						<p class="card-text">
							Explore available courses and enroll in learning paths.
						</p>
						<a href="/courses" class="btn btn-primary">View Courses</a>
					</div>
				</div>
			</div>

			<div class="col-md-6">
				<div class="card shadow-sm h-100">
					<div class="card-body">
						<h3 class="card-title">My Learning</h3>
						<p class="card-text">
							Continue your enrolled courses and work through your current lessons.
						</p>
						<a href="/learner" class="btn btn-outline-primary">Continue Learning</a>
					</div>
				</div>
			</div>
		{/if}

		{#if isTutor}
			<div class="col-md-6">
				<div class="card shadow-sm h-100">
					<div class="card-body">
						<h3 class="card-title">Tutor Dashboard</h3>
						<p class="card-text">
							Manage your courses, lessons and quizzes.
						</p>
						<a href="/tutor" class="btn btn-primary">Open Dashboard</a>
					</div>
				</div>
			</div>
		{/if}

		<div class="col-md-6">
			<div class="card shadow-sm h-100">
				<div class="card-body">
					<h3 class="card-title">Account</h3>
					<p class="card-text">
						View your Auth0 account information and assigned roles.
					</p>
					<a href="/account" class="btn btn-outline-secondary">View Account</a>
				</div>
			</div>
		</div>
	</div>
{:else}
	<div class="row justify-content-center mt-5">
		<div class="col-md-8 col-lg-7">
			<div class="card shadow-sm">
				<div class="card-body text-center">
					<h1 class="card-title mb-3">LessonFlow</h1>
					<p class="lead">
						A structured learning platform for tutors and learners.
					</p>
					<p>
						Tutors create courses with lessons, meetings and quizzes. Learners enroll,
						work through lesson material and unlock the next step by completing quizzes.
					</p>

					<div class="d-flex justify-content-center gap-2 mt-4">
						<a href="/login" class="btn btn-primary">Login</a>
						<a href="/signup" class="btn btn-outline-primary">Sign up</a>
					</div>
				</div>
			</div>
		</div>
	</div>
{/if}