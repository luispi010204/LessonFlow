<script>
	let { data, form } = $props();

	let course = data.course;
	let lessons = data.lessons || [];
	let error = data.error;
</script>

<div class="d-flex justify-content-between align-items-center mb-4">
	<div>
		<h1 class="mb-1">Course Details</h1>
		<p class="text-muted mb-0">
			View course information and available lessons.
		</p>
	</div>

	<a href="/courses" class="btn btn-outline-secondary">Back to Courses</a>
</div>

{#if error}
	<div class="alert alert-danger">
		{error}
	</div>
{/if}

{#if course}
	<div class="card shadow-sm mb-4">
		<div class="card-body">
			<div class="d-flex justify-content-between align-items-start mb-2">
				<h2 class="card-title mb-0">{course.title}</h2>

				{#if course.status}
					<span class="badge bg-secondary">{course.status}</span>
				{/if}
			</div>

			<p class="card-text text-muted">
				{course.description}
			</p>

			<p class="mb-0">
				<strong>Course ID:</strong>
				{course.id}
			</p>
		</div>
	</div>

	<div class="card shadow-sm mb-4">
		<div class="card-header">
			<h5 class="mb-0">Lessons</h5>
		</div>

		<div class="card-body">
			{#if lessons.length === 0}
				<p class="text-muted mb-0">
					This course does not contain any lessons yet.
				</p>
			{:else}
				<div class="list-group">
					{#each lessons as lesson}
						<div class="list-group-item">
							<div
								class="d-flex justify-content-between align-items-start"
							>
								<div>
									<h6 class="mb-1">
										Lesson {lesson.lessonNumber}: {lesson.title}
									</h6>

									<p class="mb-1 text-muted">
										{lesson.material}
									</p>

									{#if lesson.meetingLink}
										<small>
											<strong>Meeting:</strong>
											{lesson.meetingLink}
										</small>
									{/if}
								</div>
							</div>
						</div>
					{/each}
				</div>
			{/if}
		</div>
	</div>

	<div class="card shadow-sm">
		<div class="card-body">
			<h5 class="card-title">Enrollment</h5>
			<p class="card-text text-muted">
				Enroll in this course to start your learning flow.
			</p>

			{#if form?.error}
				<div class="alert alert-danger">
					{form.error}
				</div>
			{/if}

			<form method="POST" action="?/enroll">
				<button class="btn btn-primary" type="submit">
					Enroll in Course
				</button>
			</form>
		</div>
	</div>
{:else if !error}
	<div class="card shadow-sm">
		<div class="card-body">
			<p class="text-muted mb-0">Course not found.</p>
		</div>
	</div>
{/if}
