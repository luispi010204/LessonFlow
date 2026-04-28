<script>
	let { data } = $props();

	let courseId = data.courseId;
	let user = data.user;
	let isAuthenticated = data.isAuthenticated;
	let isTutor = isAuthenticated && user.user_roles && user.user_roles.includes('tutor');
</script>

<div class="d-flex justify-content-between align-items-center mb-4">
	<div>
		<h1 class="mb-1">Manage Course</h1>
		<p class="text-muted mb-0">Course ID: {courseId}</p>
	</div>

	<a href="/tutor" class="btn btn-outline-secondary">Back to Tutor Dashboard</a>
</div>

{#if !isAuthenticated}
	<div class="alert alert-warning">
		You need to log in to manage courses.
	</div>
	<a href="/login" class="btn btn-primary">Login</a>
{:else if !isTutor}
	<div class="alert alert-info">
		This page is intended for tutors.
	</div>
{:else}
	<div class="row g-3">
		<div class="col-lg-5">
			<div class="card shadow-sm mb-3">
				<div class="card-header">
					<h5 class="mb-0">Create Lesson</h5>
				</div>

				<div class="card-body">
					<p class="text-muted">
						This form will create lessons for the selected course.
					</p>

					<form>
						<div class="mb-3">
							<label class="form-label" for="lessonNumber">Lesson Number</label>
							<input id="lessonNumber" class="form-control" type="number" disabled />
						</div>

						<div class="mb-3">
							<label class="form-label" for="title">Title</label>
							<input id="title" class="form-control" type="text" disabled />
						</div>

						<div class="mb-3">
							<label class="form-label" for="material">Material</label>
							<textarea id="material" class="form-control" rows="4" disabled></textarea>
						</div>

						<div class="mb-3">
							<label class="form-label" for="meetingLink">Meeting Link</label>
							<input id="meetingLink" class="form-control" type="text" disabled />
						</div>

						<button class="btn btn-primary" disabled>Create Lesson</button>
					</form>
				</div>
			</div>

			<div class="card shadow-sm">
				<div class="card-header">
					<h5 class="mb-0">Create Quiz</h5>
				</div>

				<div class="card-body">
					<p class="text-muted">
						Manual quiz creation will be available here. AI quiz generation will be added later.
					</p>

					<button class="btn btn-outline-primary" disabled>Create Quiz</button>
				</div>
			</div>
		</div>

		<div class="col-lg-7">
			<div class="card shadow-sm">
				<div class="card-header">
					<h5 class="mb-0">Lessons</h5>
				</div>

				<div class="card-body">
					<p class="text-muted">
						Existing lessons for this course will be loaded from the backend.
					</p>

					<div class="border rounded p-3 bg-light">
						<h6>Example Lesson Card</h6>
						<p class="mb-2">
							Lesson number, title, material preview, meeting link and quiz status will be shown here.
						</p>
						<button class="btn btn-outline-primary" disabled>Manage Quiz</button>
					</div>
				</div>
			</div>
		</div>
	</div>
{/if}