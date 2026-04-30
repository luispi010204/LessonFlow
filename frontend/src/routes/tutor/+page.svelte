<script>
	let { data, form } = $props();

	let user = data.user;
	let isAuthenticated = data.isAuthenticated;
	let isTutor = isAuthenticated && user.user_roles && user.user_roles.includes('tutor');

	let courses = data.courses || [];
	let error = data.error;
</script>

<div class="d-flex justify-content-between align-items-center mb-4">
	<div>
		<h1 class="mb-1">Tutor Dashboard</h1>
		<p class="text-muted mb-0">Manage your courses, lessons and quizzes.</p>
	</div>
</div>

{#if !isAuthenticated}
	<div class="alert alert-warning">
		You need to log in to access the tutor dashboard.
	</div>
	<a href="/login" class="btn btn-primary">Login</a>
{:else if !isTutor}
	<div class="alert alert-info">
		This page is intended for tutors. Learners can browse courses and continue learning from their dashboard.
	</div>
	<a href="/courses" class="btn btn-primary">Browse Courses</a>
{:else}
	{#if error}
		<div class="alert alert-danger">
			{error}
		</div>
	{/if}

	{#if form?.error}
		<div class="alert alert-danger">
			{form.error}
		</div>
	{/if}

	{#if form?.success}
		<div class="alert alert-success">
			{form.success}
		</div>
	{/if}

	<div class="row g-3 mb-4">
		<div class="col-md-4">
			<div class="card shadow-sm h-100">
				<div class="card-body">
					<h5 class="card-title">My Courses</h5>
					<p class="display-6 mb-0">{courses.length}</p>
					<p class="text-muted mb-0">Courses owned by the current tutor.</p>
				</div>
			</div>
		</div>

		<div class="col-md-4">
			<div class="card shadow-sm h-100">
				<div class="card-body">
					<h5 class="card-title">Lessons</h5>
					<p class="display-6 mb-0">—</p>
					<p class="text-muted mb-0">Shown inside each course management page.</p>
				</div>
			</div>
		</div>

		<div class="col-md-4">
			<div class="card shadow-sm h-100">
				<div class="card-body">
					<h5 class="card-title">Quizzes</h5>
					<p class="display-6 mb-0">—</p>
					<p class="text-muted mb-0">Manual quizzes now, AI generation later.</p>
				</div>
			</div>
		</div>
	</div>

	<div class="row g-3">
		<div class="col-lg-5">
			<div class="card shadow-sm h-100">
				<div class="card-header">
					<h5 class="mb-0">Create Course</h5>
				</div>

				<div class="card-body">
					<p class="text-muted">
						New courses are created with status <span class="badge bg-secondary">DRAFT</span>.
					</p>

					<form method="POST" action="?/createCourse">
						<div class="mb-3">
							<label for="title" class="form-label">Title</label>
							<input id="title" name="title" type="text" class="form-control" required />
						</div>

						<div class="mb-3">
							<label for="description" class="form-label">Description</label>
							<textarea
								id="description"
								name="description"
								class="form-control"
								rows="4"
								required
							></textarea>
						</div>

						<button class="btn btn-primary" type="submit">Create Course</button>
					</form>
				</div>
			</div>
		</div>

		<div class="col-lg-7">
			<div class="card shadow-sm h-100">
				<div class="card-header">
					<h5 class="mb-0">My Courses</h5>
				</div>

				<div class="card-body">
					{#if courses.length === 0}
						<p class="text-muted">
							You have not created any courses yet.
						</p>
					{:else}
						<div class="list-group">
							{#each courses as course}
								<div class="list-group-item">
									<div class="d-flex justify-content-between align-items-start gap-3">
										<div>
											<div class="d-flex align-items-center gap-2 mb-1">
												<h6 class="mb-0">{course.title}</h6>

												{#if course.status}
													<span class="badge bg-secondary">{course.status}</span>
												{/if}
											</div>

											<p class="text-muted mb-1">
												{course.description}
											</p>

											<small class="text-muted">
												Course ID: {course.id}
											</small>
										</div>

										<a href={`/tutor/courses/${course.id}`} class="btn btn-outline-primary btn-sm">
											Manage Course
										</a>
									</div>
								</div>
							{/each}
						</div>
					{/if}
				</div>
			</div>
		</div>
	</div>
{/if}