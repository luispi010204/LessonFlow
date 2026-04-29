<script>
	let { data, form } = $props();

	let enrollmentId = data.enrollmentId;
	let currentLesson = data.currentLesson;
	let currentProgress = data.currentProgress;
	let progressList = data.progressList || [];
	let progressSummary = data.progressSummary;
	let quiz = data.quiz;
	let error = data.error;

	let user = data.user;
	let isAuthenticated = data.isAuthenticated;
	let isLearner = isAuthenticated && user.user_roles && user.user_roles.includes('learner');

	let state = currentProgress?.state;
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
	{#if error}
		<div class="alert alert-danger">{error}</div>
	{/if}

	{#if form?.error}
		<div class="alert alert-danger">{form.error}</div>
	{/if}

	{#if form?.success}
		<div class="alert alert-success">{form.success}</div>
	{/if}

	{#if currentLesson && currentProgress}
		<div class="row g-3">
			<div class="col-lg-8">
				<div class="card shadow-sm mb-3">
					<div class="card-header d-flex justify-content-between align-items-center">
						<h5 class="mb-0">Current Lesson</h5>
						<span class="badge bg-secondary">{state}</span>
					</div>

					<div class="card-body">
						<h4>
							Lesson {currentLesson.lessonNumber}: {currentLesson.title}
						</h4>

						<p class="text-muted mb-3">
							Read the lesson material below. After finishing it, mark the material as done.
						</p>

						<div class="border rounded p-3 bg-light mb-3">
							{currentLesson.material}
						</div>

						{#if state === 'UNLOCKED'}
							<form method="POST" action="?/materialDone">
								<input type="hidden" name="progressId" value={currentProgress.id} />
								<button type="submit" class="btn btn-primary">
									Mark Material Done
								</button>
							</form>
						{:else if state === 'MATERIAL_DONE' || state === 'MEETING_DONE' || state === 'PASSED'}
							<div class="alert alert-success mb-0">
								Material has been completed.
							</div>
						{:else}
							<div class="alert alert-secondary mb-0">
								This lesson is currently not available.
							</div>
						{/if}
					</div>
				</div>

				<div class="card shadow-sm mb-3">
					<div class="card-header">
						<h5 class="mb-0">Meeting</h5>
					</div>

					<div class="card-body">
						{#if currentLesson.meetingLink}
							<p>
								Open the meeting link below when your tutor schedules the session.
							</p>

							<a href={currentLesson.meetingLink} target="_blank" class="btn btn-outline-primary">
								Open Meeting Link
							</a>
						{:else}
							<p class="text-muted">
								No meeting link is available for this lesson.
							</p>
						{/if}

						{#if state === 'MATERIAL_DONE'}
							<div class="alert alert-info mt-3 mb-0">
								After the meeting, your tutor will confirm completion. The quiz will become available afterwards.
							</div>
						{:else if state === 'MEETING_DONE' || state === 'PASSED'}
							<div class="alert alert-success mt-3 mb-0">
								Meeting has been confirmed.
							</div>
						{/if}
					</div>
				</div>

				<div class="card shadow-sm">
					<div class="card-header">
						<h5 class="mb-0">Quiz Attempt</h5>
					</div>

					<div class="card-body">
						{#if state === 'MEETING_DONE'}
							{#if quiz}
								<p class="text-muted">
									Submit your quiz score. Passing the quiz will complete this lesson and unlock the next one.
								</p>

								<p>
									<strong>Required pass percentage:</strong> {quiz.passPercent}%
								</p>

								<form method="POST" action="?/submitQuizAttempt">
									<input type="hidden" name="quizId" value={quiz.id} />
									<input type="hidden" name="lessonId" value={currentLesson.id} />

									<div class="mb-3">
										<label class="form-label" for="scorePercent">Score Percent</label>
										<input
											id="scorePercent"
											name="scorePercent"
											class="form-control"
											type="number"
											min="0"
											max="100"
											step="1"
											required
										/>
									</div>

									<button class="btn btn-success" type="submit">
										Submit Quiz Attempt
									</button>
								</form>
							{:else}
								<div class="alert alert-warning mb-0">
									No quiz exists for this lesson yet.
								</div>
							{/if}
						{:else if state === 'PASSED'}
							<div class="alert alert-success mb-0">
								This lesson has been passed. Continue with the next unlocked lesson.
							</div>
						{:else}
							<div class="alert alert-secondary mb-0">
								The quiz becomes available after the tutor confirms the meeting.
							</div>
						{/if}
					</div>
				</div>
			</div>

			<div class="col-lg-4">
				<div class="card shadow-sm mb-3">
					<div class="card-header">
						<h5 class="mb-0">Progress Summary</h5>
					</div>

					<div class="card-body">
						{#if progressSummary}
							<table class="table table-sm">
								<tbody>
									{#each Object.entries(progressSummary) as [key, value]}
										<tr>
											<th>{key}</th>
											<td>{value}</td>
										</tr>
									{/each}
								</tbody>
							</table>
						{:else}
							<p class="text-muted mb-0">
								No progress summary available.
							</p>
						{/if}
					</div>
				</div>

				<div class="card shadow-sm">
					<div class="card-header">
						<h5 class="mb-0">Lessons</h5>
					</div>

					<div class="card-body">
						{#if progressList.length === 0}
							<p class="text-muted mb-0">
								No lesson progress entries available.
							</p>
						{:else}
							<div class="list-group">
								{#each progressList as progress}
									<div class="list-group-item d-flex justify-content-between align-items-center">
										<span>{progress.lessonId}</span>
										<span class="badge bg-secondary">{progress.state}</span>
									</div>
								{/each}
							</div>
						{/if}
					</div>
				</div>
			</div>
		</div>
	{:else if !error}
		<div class="card shadow-sm">
			<div class="card-body">
				<p class="text-muted mb-0">
					No current lesson found for this enrollment.
				</p>
			</div>
		</div>
	{/if}
{/if}