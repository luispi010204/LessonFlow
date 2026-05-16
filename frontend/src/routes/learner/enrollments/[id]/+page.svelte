<script>
	let { data, form } = $props();

	let enrollmentId = $derived(data.enrollmentId);
	let currentLesson = $derived(data.currentLesson);
	let currentProgress = $derived(data.currentProgress);
	let progressList = $derived(data.progressList || []);
	let progressSummary = $derived(data.progressSummary);
	let courseCompleted = $derived(data.courseCompleted);
	let quiz = $derived(data.quiz);
	let error = $derived(data.error);

	let user = $derived(data.user);
	let isAuthenticated = $derived(data.isAuthenticated);
	let isLearner = $derived(
		isAuthenticated &&
			user?.user_roles &&
			user.user_roles.includes("learner"),
	);

	let lessonState = $derived(currentProgress?.state);
	let hiddenQuizResultKey = $state(null);

	function formatPercent(value) {
		if (
			value === null ||
			value === undefined ||
			Number.isNaN(Number(value))
		) {
			return "0";
		}

		return Number(value).toFixed(1).replace(".0", "");
	}

	function getOptionLabel(optionIndex) {
		if (optionIndex === 0) {
			return "A";
		}

		if (optionIndex === 1) {
			return "B";
		}

		if (optionIndex === 2) {
			return "C";
		}

		if (optionIndex === 3) {
			return "D";
		}

		return "";
	}

	function getQuizResult() {
		if (form?.quizResult) {
			return form.quizResult;
		}

		if (!form?.attempt || !quiz?.questions) {
			return null;
		}

		const attempt = form.attempt;
		const selectedOptionIndexes = attempt.selectedOptionIndexes || [];

		let correctAnswers = 0;

		const questionResults = quiz.questions.map(
			(question, questionIndex) => {
				const selectedOptionIndex =
					selectedOptionIndexes[questionIndex];
				const correctOptionIndex = question.correctOptionIndex;
				const isCorrect = selectedOptionIndex === correctOptionIndex;

				if (isCorrect) {
					correctAnswers++;
				}

				return {
					questionText: question.questionText,
					options: question.options || [],
					selectedOptionIndex,
					correctOptionIndex,
					isCorrect,
				};
			},
		);

		return {
			scorePercent: attempt.scorePercent,
			passed: attempt.passed,
			totalQuestions: quiz.questions.length,
			correctAnswers,
			wrongAnswers: quiz.questions.length - correctAnswers,
			questionResults,
		};
	}

	function getQuizResultKey() {
		const quizResult = getQuizResult();

		if (!quizResult) {
			return null;
		}

		if (quizResult.resultKey) {
			return quizResult.resultKey;
		}

		if (quizResult.attemptId) {
			return quizResult.attemptId;
		}

		return JSON.stringify({
			scorePercent: quizResult.scorePercent,
			correctAnswers: quizResult.correctAnswers,
			wrongAnswers: quizResult.wrongAnswers,
			selectedAnswers: quizResult.questionResults?.map(
				(result) => result.selectedOptionIndex,
			),
		});
	}

	function shouldShowQuizResult() {
		const quizResult = getQuizResult();

		if (!quizResult) {
			return false;
		}

		return getQuizResultKey() !== hiddenQuizResultKey;
	}

	function hideQuizResult() {
		hiddenQuizResultKey = getQuizResultKey();
	}

	function getResultAlertClass(quizResult) {
		if (quizResult?.passed) {
			return "alert-success";
		}

		return "alert-danger";
	}

	function getQuestionResultClass(questionResult) {
		if (questionResult?.isCorrect) {
			return "border-success bg-success-subtle";
		}

		return "border-danger bg-danger-subtle";
	}

	function getOptionClass(optionIndex, questionResult) {
		if (optionIndex === questionResult.correctOptionIndex) {
			return "list-group-item-success border-success";
		}

		if (
			optionIndex === questionResult.selectedOptionIndex &&
			optionIndex !== questionResult.correctOptionIndex
		) {
			return "list-group-item-danger border-danger";
		}

		return "";
	}
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
	<div class="alert alert-info">This page is intended for learners.</div>
{:else}
	{#if error}
		<div class="alert alert-danger">{error}</div>
	{/if}

	{#if form?.error}
		<div class="alert alert-danger">{form.error}</div>
	{/if}

	{#if form?.success && !form?.quizResult}
		<div class="alert alert-success">{form.success}</div>
	{/if}

	{#if shouldShowQuizResult()}
		{@const quizResult = getQuizResult()}

		<div class="card shadow-sm mb-4">
			<div class="card-header">
				<h5 class="mb-0">Quiz Result</h5>
			</div>

			<div class="card-body">
				<div class={`alert ${getResultAlertClass(quizResult)} mb-3`}>
					<h6 class="alert-heading mb-2">
						{#if quizResult.passed}
							Quiz passed
						{:else}
							Quiz not passed
						{/if}
					</h6>

					<p class="mb-0">
						You answered <strong>{quizResult.correctAnswers}</strong>
						of <strong>{quizResult.totalQuestions}</strong> questions
						correctly. Your score is
						<strong>{formatPercent(quizResult.scorePercent)}%</strong>.
					</p>
				</div>

				<div class="row g-2 mb-3">
					<div class="col-md-4">
						<div class="border rounded p-3 h-100 text-center">
							<div class="text-muted small">Score</div>
							<div class="fs-4 fw-bold">
								{formatPercent(quizResult.scorePercent)}%
							</div>
						</div>
					</div>

					<div class="col-md-4">
						<div class="border rounded p-3 h-100 text-center">
							<div class="text-muted small">Correct</div>
							<div class="fs-4 fw-bold text-success">
								{quizResult.correctAnswers}
							</div>
						</div>
					</div>

					<div class="col-md-4">
						<div class="border rounded p-3 h-100 text-center">
							<div class="text-muted small">Wrong</div>
							<div class="fs-4 fw-bold text-danger">
								{quizResult.wrongAnswers}
							</div>
						</div>
					</div>
				</div>

				{#if quizResult.questionResults && quizResult.questionResults.length > 0}
					<div class="mb-3">
						<h6 class="mb-3">Answer Details</h6>

						{#each quizResult.questionResults as questionResult, questionIndex}
							<div
								class={`border rounded p-3 mb-3 ${getQuestionResultClass(questionResult)}`}
							>
								<div
									class="d-flex justify-content-between align-items-start gap-2 mb-2"
								>
									<h6 class="mb-0">
										Question {questionIndex + 1}: {questionResult.questionText}
									</h6>

									{#if questionResult.isCorrect}
										<span class="badge bg-success">Correct</span>
									{:else}
										<span class="badge bg-danger">Wrong</span>
									{/if}
								</div>

								<div class="list-group">
									{#each questionResult.options as option, optionIndex}
										<div
											class={`list-group-item ${getOptionClass(optionIndex, questionResult)}`}
										>
											<div
												class="d-flex justify-content-between align-items-start gap-2"
											>
												<div>
													<strong>{getOptionLabel(optionIndex)}:</strong>
													{option}
												</div>

												<div class="d-flex gap-1">
													{#if optionIndex === questionResult.selectedOptionIndex}
														<span class="badge bg-primary">Your answer</span>
													{/if}

													{#if optionIndex === questionResult.correctOptionIndex}
														<span class="badge bg-success">Correct answer</span>
													{/if}
												</div>
											</div>
										</div>
									{/each}
								</div>
							</div>
						{/each}
					</div>
				{/if}

				<button
					type="button"
					class="btn btn-outline-primary"
					onclick={hideQuizResult}
				>
					Continue
				</button>
			</div>
		</div>
	{/if}

	{#if currentLesson && currentProgress}
		<div class="row g-3">
			<div class="col-lg-8">
				<div class="card shadow-sm mb-3">
					<div
						class="card-header d-flex justify-content-between align-items-center"
					>
						<h5 class="mb-0">Current Lesson</h5>
						<span class="badge bg-secondary">{lessonState}</span>
					</div>

					<div class="card-body">
						<h4>
							Lesson {currentLesson.lessonNumber}: {currentLesson.title}
						</h4>

						<p class="text-muted mb-3">
							Read the structured lesson material below. After
							finishing it, mark the material as done.
						</p>

						<div class="lesson-material-card mb-3">
							<div
								class="d-flex justify-content-between align-items-center mb-3"
							>
								<h5 class="mb-0">Lesson Material</h5>
								<span class="badge bg-light text-dark border">
									Study Content
								</span>
							</div>

							<div class="lesson-material-content">
								{currentLesson.material}
							</div>
						</div>

						{#if lessonState === "UNLOCKED"}
							<form method="POST" action="?/materialDone">
								<input
									type="hidden"
									name="progressId"
									value={currentProgress.id}
								/>
								<button type="submit" class="btn btn-primary">
									Mark Material Done
								</button>
							</form>
						{:else if lessonState === "MATERIAL_DONE" || lessonState === "MEETING_DONE" || lessonState === "PASSED"}
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
								Open the meeting link below when your tutor
								schedules the session.
							</p>

							<a
								href={currentLesson.meetingLink}
								target="_blank"
								class="btn btn-outline-primary"
							>
								Open Meeting Link
							</a>
						{:else}
							<p class="text-muted">
								No meeting link is available for this lesson.
							</p>
						{/if}

						{#if lessonState === "MATERIAL_DONE"}
							<div class="alert alert-info mt-3 mb-0">
								After the meeting, your tutor will confirm
								completion. The quiz will become available
								afterwards.
							</div>
						{:else if lessonState === "MEETING_DONE" || lessonState === "PASSED"}
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
						{#if shouldShowQuizResult()}
							<div class="alert alert-info mb-0">
								Review your quiz result above. Click Continue to
								start another attempt.
							</div>
						{:else if lessonState === "MEETING_DONE"}
							{#if quiz}
								<p class="text-muted">
									Answer all single-choice questions. The
									backend will calculate your score
									automatically.
								</p>

								<p>
									<strong>Required pass percentage:</strong>
									{quiz.passPercent}%
								</p>

								<form method="POST" action="?/submitQuizAttempt">
									<input
										type="hidden"
										name="quizId"
										value={quiz.id}
									/>
									<input
										type="hidden"
										name="lessonId"
										value={currentLesson.id}
									/>
									<input
										type="hidden"
										name="questionCount"
										value={quiz.questions
											? quiz.questions.length
											: 0}
									/>

									{#if quiz.questions && quiz.questions.length > 0}
										{#each quiz.questions as question, questionIndex}
											<div class="border rounded p-3 mb-3">
												<h6 class="mb-3">
													Question {questionIndex + 1}: {question.questionText}
												</h6>

												{#if question.options && question.options.length > 0}
													{#each question.options as option, optionIndex}
														<div class="form-check mb-2">
															<input
																class="form-check-input"
																type="radio"
																id={`question-${questionIndex}-option-${optionIndex}`}
																name={`selectedOptionIndex-${questionIndex}`}
																value={optionIndex}
																required
															/>
															<label
																class="form-check-label"
																for={`question-${questionIndex}-option-${optionIndex}`}
															>
																{option}
															</label>
														</div>
													{/each}
												{:else}
													<div class="alert alert-warning mb-0">
														This question has no
														answer options.
													</div>
												{/if}
											</div>
										{/each}

										<button
											class="btn btn-success"
											type="submit"
										>
											Submit Quiz Attempt
										</button>
									{:else}
										<div class="alert alert-warning mb-0">
											This quiz does not contain any
											questions.
										</div>
									{/if}
								</form>
							{:else}
								<div class="alert alert-warning mb-0">
									No quiz exists for this lesson yet.
								</div>
							{/if}
						{:else if lessonState === "PASSED"}
							<div class="alert alert-success mb-0">
								This lesson has been passed. Continue with the
								next unlocked lesson.
							</div>
						{:else}
							<div class="alert alert-secondary mb-0">
								The quiz becomes available after the tutor
								confirms the meeting.
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
									<tr>
										<th>Total lessons</th>
										<td>{progressSummary.totalLessons}</td>
									</tr>
									<tr>
										<th>Passed lessons</th>
										<td>{progressSummary.passedLessons}</td>
									</tr>
									<tr>
										<th>Current lesson</th>
										<td>{progressSummary.currentLessonId || "—"}</td>
									</tr>
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
									<div
										class="list-group-item d-flex justify-content-between align-items-center"
									>
										<span>{progress.lessonId}</span>
										<span class="badge bg-secondary">
											{progress.state}
										</span>
									</div>
								{/each}
							</div>
						{/if}
					</div>
				</div>
			</div>
		</div>
	{:else if !error}
		{#if courseCompleted}
			<div class="card shadow-sm">
				<div class="card-body text-center">
					<h3 class="mb-3">Course completed</h3>

					<p class="text-muted">
						Congratulations! You completed all lessons in this course.
					</p>

					{#if progressSummary}
						<p class="mb-4">
							<strong>{progressSummary.passedLessons}</strong> of
							<strong>{progressSummary.totalLessons}</strong> lessons completed.
						</p>
					{/if}

					<a href="/learner" class="btn btn-primary">
						Back to My Learning
					</a>
				</div>
			</div>
		{:else}
			<div class="card shadow-sm">
				<div class="card-body">
					<p class="text-muted mb-0">
						No current lesson found for this enrollment.
					</p>
				</div>
			</div>
		{/if}
	{/if}
{/if}

<style>
	.lesson-material-card {
		border: 1px solid #dee2e6;
		border-radius: 0.75rem;
		background: #f8f9fa;
		padding: 1rem;
	}

	.lesson-material-content {
		white-space: pre-wrap;
		font-size: 1rem;
		line-height: 1.65;
		background: #ffffff;
		border: 1px solid #e9ecef;
		border-radius: 0.5rem;
		padding: 1rem;
		max-height: 520px;
		overflow-y: auto;
	}
</style>