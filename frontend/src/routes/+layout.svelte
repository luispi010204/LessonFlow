<script>
	import { onMount } from 'svelte';

	import 'bootstrap/dist/css/bootstrap.min.css';
	import './styles.css';

	let { data, children } = $props();

	let user = data.user;
	let isAuthenticated = data.isAuthenticated;

	let isTutor = isAuthenticated && user.user_roles && user.user_roles.includes('tutor');
	let isLearner = isAuthenticated && user.user_roles && user.user_roles.includes('learner');

	onMount(async () => {
		await import('bootstrap/dist/js/bootstrap.bundle.min.js');
	});
</script>

<nav class="navbar navbar-expand-lg bg-light border-bottom">
	<div class="container-fluid">
		<a class="navbar-brand fw-bold" href="/">LessonFlow</a>

		<button
			class="navbar-toggler"
			type="button"
			data-bs-toggle="collapse"
			data-bs-target="#navbarNav"
			aria-controls="navbarNav"
			aria-expanded="false"
			aria-label="Toggle navigation"
		>
			<span class="navbar-toggler-icon"></span>
		</button>

		<div class="collapse navbar-collapse" id="navbarNav">
			<ul class="navbar-nav me-auto mb-2 mb-lg-0">
				{#if isLearner}
					<li class="nav-item">
						<a class="nav-link" href="/courses">Courses</a>
					</li>

					<li class="nav-item">
						<a class="nav-link" href="/learner">My Learning</a>
					</li>
				{/if}

				{#if isTutor}
					<li class="nav-item">
						<a class="nav-link" href="/tutor">Tutor Dashboard</a>
					</li>
				{/if}

				{#if isAuthenticated}
					<li class="nav-item">
						<a class="nav-link" href="/account">Account</a>
					</li>
				{/if}
			</ul>

			<div class="d-flex align-items-center gap-2">
				{#if isAuthenticated}
					<span class="navbar-text">
						{user.name}
					</span>

					<form method="POST" action="/logout" class="m-0">
						<button type="submit" class="btn btn-primary btn-sm">
							Log out
						</button>
					</form>
				{:else}
					<a href="/login" class="btn btn-primary btn-sm">Login</a>
					<a href="/signup" class="btn btn-outline-primary btn-sm">Sign up</a>
				{/if}
			</div>
		</div>
	</div>
</nav>

<main class="container mt-4">
	{@render children()}
</main>