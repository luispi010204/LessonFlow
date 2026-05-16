<script>
	let { data, form } = $props();

	let course = data.course;
	let lessons = data.lessons || [];
	let enrollment = data.enrollment;
	let isEnrolled = data.isEnrolled || false;
	let lessonsAvailable = data.lessonsAvailable || false;
	let error = data.error;
</script>

<div class="d-flex justify-content-between align-items-center mb-4">
	<div>
		<h1 class="mb-1">Course Details</h1>
		<p class="text-muted mb-0">
			View course information and start your learning flow.
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
			{#if !lessonsAvailable && !isEnrolled}
				<div class="alert alert-info mb-0">
					Lesson content is available after enrollment. Enroll in this course to start the
					learning flow and access the first lesson.
				</div>
			{:else if lessons.length === 0}
				<p class="text-muted mb-0">
					This course does not contain any lessons yet.
				</p>
			{:else}
				<div class="list-group">
					{#each lessons as lesson}
						<div class="list-group-item">
							<div class="d-flex justify-content-between align-items-start">
								<div>
									<h6 class="mb-1">
										Lesson {lesson.lessonNumber}: {lesson.title}
									</h6>

									<p class="mb-1 text-muted lesson-preview">
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

			{#if isEnrolled && enrollment}
				<p class="card-text text-muted">
					You are already enrolled in this course. Continue your learning flow.
				</p>

				<a href={`/learner/enrollments/${enrollment.id}`} class="btn btn-primary">
					Continue Learning
				</a>
			{:else}
				<p class="card-text text-muted">
					Enroll in this course to access the lessons and start your learning flow.
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
			{/if}
		</div>
	</div>
{:else if !error}
	<div class="card shadow-sm">
		<div class="card-body">
			<p class="text-muted mb-0">Course not found.</p>
		</div>
	</div>
{/if}

<style>
	.lesson-preview {
		white-space: pre-wrap;
		max-height: 180px;
		overflow-y: auto;
	}
</style>