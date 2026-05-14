<script>
	let { data } = $props();

	let user = data.user;
	let isAuthenticated = data.isAuthenticated;

	let isTutor = isAuthenticated && user.user_roles && user.user_roles.includes('tutor');
	let isLearner = isAuthenticated && user.user_roles && user.user_roles.includes('learner');
</script>

{#if isAuthenticated}
	<div class="row align-items-center mb-4">
		<div class="col-lg-8">
			<div class="card shadow-sm h-100">
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

		<div class="col-lg-4 d-none d-lg-block">
			<div class="text-center">
				<img
					src="/LessonFlowLogo_big.png"
					alt="LessonFlow logo"
					class="home-logo-authenticated"
				/>
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
							Manage your courses, lessons and AI-generated quizzes.
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
	<section class="lessonflow-hero rounded-4 shadow-sm">
		<div class="row align-items-center g-4">
			<div class="col-lg-6 text-center text-lg-start">
				<p class="text-primary fw-semibold mb-2">Self-Paced Learning</p>
				<h1 class="display-4 fw-bold mb-3">LessonFlow</h1>

				<p class="lead mb-3">
					A structured learning platform for tutors and learners.
				</p>

				<p class="text-muted mb-4">
					Tutors create courses with lessons, meetings and AI-generated quizzes.
					Learners enroll, work through lesson material and unlock the next step
					by completing their learning flow.
				</p>

				<div class="d-flex flex-column flex-sm-row gap-2 justify-content-center justify-content-lg-start">
					<a href="/login" class="btn btn-primary btn-lg">Login</a>
					<a href="/signup" class="btn btn-outline-primary btn-lg">Sign up</a>
				</div>
			</div>

			<div class="col-lg-6 text-center">
				<img
					src="/LessonFlowLogo_big.png"
					alt="LessonFlow logo"
					class="home-logo-large"
				/>
			</div>
		</div>
	</section>

	<div class="row g-3 mt-4">
		<div class="col-md-4">
			<div class="card shadow-sm h-100">
				<div class="card-body">
					<h3 class="h5 card-title">Structured Courses</h3>
					<p class="card-text text-muted mb-0">
						Lessons are organized step by step so learners always know what comes next.
					</p>
				</div>
			</div>
		</div>

		<div class="col-md-4">
			<div class="card shadow-sm h-100">
				<div class="card-body">
					<h3 class="h5 card-title">Tutor Guidance</h3>
					<p class="card-text text-muted mb-0">
						Tutors manage learning material, meetings and learner progress in one place.
					</p>
				</div>
			</div>
		</div>

		<div class="col-md-4">
			<div class="card shadow-sm h-100">
				<div class="card-body">
					<h3 class="h5 card-title">AI Quizzes</h3>
					<p class="card-text text-muted mb-0">
						Quiz questions can be generated from lesson material to support the learning flow.
					</p>
				</div>
			</div>
		</div>
	</div>
{/if}

<style>
	.lessonflow-hero {
		padding: 4rem 3rem;
		background:
			linear-gradient(135deg, rgba(235, 248, 255, 0.95), rgba(255, 255, 255, 0.98)),
			linear-gradient(135deg, rgba(13, 110, 253, 0.08), rgba(13, 202, 240, 0.08));
		border: 1px solid rgba(13, 110, 253, 0.12);
	}

	.home-logo-large {
		width: min(100%, 520px);
		max-height: 420px;
		object-fit: contain;
	}

	.home-logo-authenticated {
		width: min(100%, 260px);
		max-height: 220px;
		object-fit: contain;
	}

	@media (max-width: 767.98px) {
		.lessonflow-hero {
			padding: 2.5rem 1.5rem;
		}

		.home-logo-large {
			max-height: 300px;
		}
	}
</style>