<script>
	let { data } = $props();

	let user = data.user;
	let isAuthenticated = data.isAuthenticated;

	let isTutor = isAuthenticated && user.user_roles && user.user_roles.includes('tutor');
	let isLearner = isAuthenticated && user.user_roles && user.user_roles.includes('learner');
</script>

<div class="d-flex justify-content-between align-items-center mb-4">
	<div>
		<h1 class="mb-1">Account</h1>
		<p class="text-muted mb-0">View your Auth0 profile information and assigned roles.</p>
	</div>
</div>

{#if isAuthenticated}
	<div class="row g-3">
		<div class="col-lg-7">
			<div class="card shadow-sm h-100">
				<div class="card-header">
					<h5 class="mb-0">Profile Information</h5>
				</div>

				<div class="card-body">
					<div class="d-flex align-items-center gap-3 mb-4">
						{#if user.picture}
							<img
								src={user.picture}
								alt="Profile"
								class="rounded-circle"
								width="80"
								height="80"
							/>
						{:else}
							<div
								class="rounded-circle bg-secondary text-white d-flex align-items-center justify-content-center"
								style="width: 80px; height: 80px;"
							>
								{user.name ? user.name.charAt(0).toUpperCase() : '?'}
							</div>
						{/if}

						<div>
							<h4 class="mb-1">{user.name || 'Unknown user'}</h4>
							<p class="text-muted mb-0">{user.email || 'No email available'}</p>
						</div>
					</div>

					<table class="table table-sm">
						<tbody>
							<tr>
								<th scope="row">Name</th>
								<td>{user.name || '-'}</td>
							</tr>
							<tr>
								<th scope="row">Nickname</th>
								<td>{user.nickname || '-'}</td>
							</tr>
							<tr>
								<th scope="row">Email</th>
								<td>{user.email || '-'}</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>

		<div class="col-lg-5">
			<div class="card shadow-sm mb-3">
				<div class="card-header">
					<h5 class="mb-0">Roles</h5>
				</div>

				<div class="card-body">
					{#if user.user_roles && user.user_roles.length > 0}
						<div class="d-flex flex-wrap gap-2">
							{#each user.user_roles as role}
								<span class="badge bg-primary">{role}</span>
							{/each}
						</div>
					{:else}
						<p class="text-muted mb-0">No roles assigned.</p>
					{/if}
				</div>
			</div>

			<div class="card shadow-sm">
				<div class="card-header">
					<h5 class="mb-0">Quick Access</h5>
				</div>

				<div class="card-body d-grid gap-2">
					{#if isLearner}
						<a href="/courses" class="btn btn-primary">Browse Courses</a>
						<a href="/learner" class="btn btn-outline-primary">My Learning</a>
					{/if}

					{#if isTutor}
						<a href="/tutor" class="btn btn-primary">Tutor Dashboard</a>
					{/if}

					<form method="POST" action="/logout">
						<button type="submit" class="btn btn-outline-danger w-100">
							Log out
						</button>
					</form>
				</div>
			</div>
		</div>
	</div>
{:else}
	<div class="card shadow-sm">
		<div class="card-body">
			<div class="alert alert-warning">
				You are not logged in.
			</div>

			<a href="/login" class="btn btn-primary">Login</a>
		</div>
	</div>
{/if}