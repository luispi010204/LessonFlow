<script>
	let { data, form } = $props();

	let courseId = data.courseId;
	let course = data.course;
	let lessons = data.lessons || [];
	let lessonCards = data.lessonCards || [];
	let enrollmentProgressCards = data.enrollmentProgressCards || [];
	let error = data.error;

	let user = data.user;
	let isAuthenticated = data.isAuthenticated;
	let isTutor =
		isAuthenticated && user.user_roles && user.user_roles.includes("tutor");

	function getStateBadgeClass(state) {
		if (state === "PASSED") {
			return "bg-success";
		}

		if (state === "MEETING_DONE") {
			return "bg-primary";
		}

		if (state === "MATERIAL_DONE") {
			return "bg-warning text-dark";
		}

		if (state === "UNLOCKED") {
			return "bg-info text-dark";
		}

		if (state === "LOCKED") {
			return "bg-secondary";
		}

		return "bg-secondary";
	}
</script>

<div class="d-flex justify-content-between align-items-center mb-4">
	<div>
		<h1 class="mb-1">Manage Course</h1>
		<p class="text-muted mb-0">Course ID: {courseId}</p>
	</div>

	<a href="/tutor" class="btn btn-outline-secondary"
		>Back to Tutor Dashboard</a
	>
</div>

{#if !isAuthenticated}
	<div class="alert alert-warning">You need to log in to manage courses.</div>
	<a href="/login" class="btn btn-primary">Login</a>
{:else if !isTutor}
	<div class="alert alert-info">This page is intended for tutors.</div>
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

	{#if course}
		<div class="card shadow-sm mb-4">
			<div class="card-body">
				<div
					class="d-flex justify-content-between align-items-start mb-2"
				>
					<div>
						<h2 class="mb-1">{course.title}</h2>
						<p class="text-muted mb-0">{course.description}</p>
					</div>

					{#if course.status}
						<span class="badge bg-secondary">{course.status}</span>
					{/if}
				</div>
			</div>
		</div>
	{/if}

	<div class="row g-3">
		<div class="col-lg-5">
			<div class="card shadow-sm mb-3">
				<div class="card-header">
					<h5 class="mb-0">Create Lesson</h5>
				</div>

				<div class="card-body">
					<form method="POST" action="?/createLesson">
						<div class="mb-3">
							<label class="form-label">Lesson Number</label>
							<input
								class="form-control"
								type="text"
								value={`Will be created as Lesson ${lessons.length + 1}`}
								disabled
							/>
							<div class="form-text">
								Lesson numbers are generated automatically based
								on the existing lessons.
							</div>
						</div>

						<div class="mb-3">
							<label class="form-label" for="title">Title</label>
							<input
								id="title"
								name="title"
								class="form-control"
								type="text"
								required
							/>
						</div>

						<div class="mb-3">
							<label class="form-label" for="material"
								>Material</label
							>
							<textarea
								id="material"
								name="material"
								class="form-control"
								rows="5"
								required
							></textarea>
						</div>

						<div class="mb-3">
							<label class="form-label" for="meetingLink"
								>Meeting Link</label
							>
							<input
								id="meetingLink"
								name="meetingLink"
								class="form-control"
								type="text"
								required
							/>
						</div>

						<button class="btn btn-primary" type="submit"
							>Create Lesson</button
						>
					</form>
				</div>
			</div>

			<div class="card shadow-sm">
				<div class="card-header">
					<h5 class="mb-0">Create Single-Choice Quiz</h5>
				</div>

				<div class="card-body">
					{#if lessons.length === 0}
						<p class="text-muted mb-0">
							Create at least one lesson before adding a quiz.
						</p>
					{:else}
						<p class="text-muted">
							Create a single-choice quiz for one of the lessons.
							Learners will select one answer per question and the
							backend will calculate the score automatically.
						</p>

						<form method="POST" action="?/createQuiz">
							<div class="mb-3">
								<label class="form-label" for="lessonId"
									>Lesson</label
								>
								<select
									id="lessonId"
									name="lessonId"
									class="form-select"
									required
								>
									<option value="">Select lesson</option>
									{#each lessons as lesson}
										<option value={lesson.id}>
											Lesson {lesson.lessonNumber}: {lesson.title}
										</option>
									{/each}
								</select>
							</div>

							<div class="mb-3">
								<label class="form-label" for="passPercent"
									>Pass Percent</label
								>
								<input
									id="passPercent"
									name="passPercent"
									class="form-control"
									type="number"
									min="1"
									max="100"
									value="70"
									required
								/>
							</div>

							<div class="border rounded p-3 mb-3">
								<h6 class="mb-3">Question 1</h6>

								<div class="mb-3">
									<label class="form-label" for="question1"
										>Question Text</label
									>
									<input
										id="question1"
										name="question1"
										class="form-control"
										type="text"
										required
									/>
								</div>

								<div class="row g-2">
									<div class="col-md-6">
										<label
											class="form-label"
											for="question1OptionA"
											>Option A</label
										>
										<input
											id="question1OptionA"
											name="question1OptionA"
											class="form-control"
											type="text"
											required
										/>
									</div>

									<div class="col-md-6">
										<label
											class="form-label"
											for="question1OptionB"
											>Option B</label
										>
										<input
											id="question1OptionB"
											name="question1OptionB"
											class="form-control"
											type="text"
											required
										/>
									</div>

									<div class="col-md-6">
										<label
											class="form-label"
											for="question1OptionC"
											>Option C</label
										>
										<input
											id="question1OptionC"
											name="question1OptionC"
											class="form-control"
											type="text"
											required
										/>
									</div>

									<div class="col-md-6">
										<label
											class="form-label"
											for="question1OptionD"
											>Option D</label
										>
										<input
											id="question1OptionD"
											name="question1OptionD"
											class="form-control"
											type="text"
											required
										/>
									</div>
								</div>

								<div class="mt-3">
									<label
										class="form-label"
										for="question1CorrectOptionIndex"
										>Correct Answer</label
									>
									<select
										id="question1CorrectOptionIndex"
										name="question1CorrectOptionIndex"
										class="form-select"
										required
									>
										<option value=""
											>Select correct answer</option
										>
										<option value="0">Option A</option>
										<option value="1">Option B</option>
										<option value="2">Option C</option>
										<option value="3">Option D</option>
									</select>
								</div>
							</div>

							<div class="border rounded p-3 mb-3">
								<h6 class="mb-3">Question 2 optional</h6>

								<div class="mb-3">
									<label class="form-label" for="question2"
										>Question Text</label
									>
									<input
										id="question2"
										name="question2"
										class="form-control"
										type="text"
									/>
								</div>

								<div class="row g-2">
									<div class="col-md-6">
										<label
											class="form-label"
											for="question2OptionA"
											>Option A</label
										>
										<input
											id="question2OptionA"
											name="question2OptionA"
											class="form-control"
											type="text"
										/>
									</div>

									<div class="col-md-6">
										<label
											class="form-label"
											for="question2OptionB"
											>Option B</label
										>
										<input
											id="question2OptionB"
											name="question2OptionB"
											class="form-control"
											type="text"
										/>
									</div>

									<div class="col-md-6">
										<label
											class="form-label"
											for="question2OptionC"
											>Option C</label
										>
										<input
											id="question2OptionC"
											name="question2OptionC"
											class="form-control"
											type="text"
										/>
									</div>

									<div class="col-md-6">
										<label
											class="form-label"
											for="question2OptionD"
											>Option D</label
										>
										<input
											id="question2OptionD"
											name="question2OptionD"
											class="form-control"
											type="text"
										/>
									</div>
								</div>

								<div class="mt-3">
									<label
										class="form-label"
										for="question2CorrectOptionIndex"
										>Correct Answer</label
									>
									<select
										id="question2CorrectOptionIndex"
										name="question2CorrectOptionIndex"
										class="form-select"
									>
										<option value=""
											>Select correct answer</option
										>
										<option value="0">Option A</option>
										<option value="1">Option B</option>
										<option value="2">Option C</option>
										<option value="3">Option D</option>
									</select>
								</div>
							</div>

							<div class="border rounded p-3 mb-3">
								<h6 class="mb-3">Question 3 optional</h6>

								<div class="mb-3">
									<label class="form-label" for="question3"
										>Question Text</label
									>
									<input
										id="question3"
										name="question3"
										class="form-control"
										type="text"
									/>
								</div>

								<div class="row g-2">
									<div class="col-md-6">
										<label
											class="form-label"
											for="question3OptionA"
											>Option A</label
										>
										<input
											id="question3OptionA"
											name="question3OptionA"
											class="form-control"
											type="text"
										/>
									</div>

									<div class="col-md-6">
										<label
											class="form-label"
											for="question3OptionB"
											>Option B</label
										>
										<input
											id="question3OptionB"
											name="question3OptionB"
											class="form-control"
											type="text"
										/>
									</div>

									<div class="col-md-6">
										<label
											class="form-label"
											for="question3OptionC"
											>Option C</label
										>
										<input
											id="question3OptionC"
											name="question3OptionC"
											class="form-control"
											type="text"
										/>
									</div>

									<div class="col-md-6">
										<label
											class="form-label"
											for="question3OptionD"
											>Option D</label
										>
										<input
											id="question3OptionD"
											name="question3OptionD"
											class="form-control"
											type="text"
										/>
									</div>
								</div>

								<div class="mt-3">
									<label
										class="form-label"
										for="question3CorrectOptionIndex"
										>Correct Answer</label
									>
									<select
										id="question3CorrectOptionIndex"
										name="question3CorrectOptionIndex"
										class="form-select"
									>
										<option value=""
											>Select correct answer</option
										>
										<option value="0">Option A</option>
										<option value="1">Option B</option>
										<option value="2">Option C</option>
										<option value="3">Option D</option>
									</select>
								</div>
							</div>

							<button class="btn btn-primary" type="submit"
								>Create Quiz</button
							>
						</form>
					{/if}
				</div>
			</div>
		</div>

		<div class="col-lg-7">
			<div class="card shadow-sm mb-3">
				<div
					class="card-header d-flex justify-content-between align-items-center"
				>
					<h5 class="mb-0">Lessons</h5>
					<span class="badge bg-secondary">{lessons.length}</span>
				</div>

				<div class="card-body">
					{#if lessonCards.length === 0}
						<p class="text-muted mb-0">
							This course does not contain any lessons yet.
						</p>
					{:else}
						<div class="list-group">
							{#each lessonCards as card}
								<div class="list-group-item">
									<div
										class="d-flex justify-content-between align-items-start gap-3"
									>
										<div>
											<h6 class="mb-1">
												Lesson {card.lesson
													.lessonNumber}: {card.lesson
													.title}
											</h6>

											<p class="text-muted mb-2">
												{card.lesson.material}
											</p>

											{#if card.lesson.meetingLink}
												<p class="mb-1">
													<strong>Meeting:</strong>
													<a
														href={card.lesson
															.meetingLink}
														target="_blank"
													>
														{card.lesson
															.meetingLink}
													</a>
												</p>
											{/if}

											<small
												class="text-muted d-block mb-2"
											>
												Lesson ID: {card.lesson.id}
											</small>

											{#if card.quiz}
												<div
													class="alert alert-success py-2 mb-0"
												>
													<strong>Quiz exists</strong
													><br />
													Pass percent: {card.quiz
														.passPercent}%<br />
													Questions: {card.quiz
														.questions
														? card.quiz.questions
																.length
														: 0}
												</div>
											{:else}
												<div
													class="alert alert-warning py-2 mb-0"
												>
													No quiz created for this
													lesson yet.
												</div>
											{/if}
										</div>

										{#if card.quiz}
											<span class="badge bg-success"
												>Quiz ready</span
											>
										{:else}
											<span
												class="badge bg-warning text-dark"
												>No quiz</span
											>
										{/if}
									</div>
								</div>
							{/each}
						</div>
					{/if}
				</div>
			</div>

			<div class="card shadow-sm">
				<div
					class="card-header d-flex justify-content-between align-items-center"
				>
					<h5 class="mb-0">
						Learner Progress / Meeting Confirmations
					</h5>
					<span class="badge bg-secondary"
						>{enrollmentProgressCards.length}</span
					>
				</div>

				<div class="card-body">
					{#if enrollmentProgressCards.length === 0}
						<p class="text-muted mb-0">
							No learners are enrolled in this course yet.
						</p>
					{:else}
						<div class="accordion" id="learnerProgressAccordion">
							{#each enrollmentProgressCards as card, index}
								<div class="accordion-item">
									<h2
										class="accordion-header"
										id={`heading-${index}`}
									>
										<button
											class="accordion-button {index === 0
												? ''
												: 'collapsed'}"
											type="button"
											data-bs-toggle="collapse"
											data-bs-target={`#collapse-${index}`}
											aria-expanded={index === 0
												? "true"
												: "false"}
											aria-controls={`collapse-${index}`}
										>
											Learner: {card.enrollment
												.learnerUserId}
										</button>
									</h2>

									<div
										id={`collapse-${index}`}
										class="accordion-collapse collapse {index ===
										0
											? 'show'
											: ''}"
										aria-labelledby={`heading-${index}`}
										data-bs-parent="#learnerProgressAccordion"
									>
										<div class="accordion-body">
											<p class="text-muted small mb-3">
												Enrollment ID: {card.enrollment
													.id}
											</p>

											{#if card.error}
												<div
													class="alert alert-danger mb-0"
												>
													{card.error}
												</div>
											{:else if card.progressCards.length === 0}
												<p class="text-muted mb-0">
													No progress entries found
													for this learner.
												</p>
											{:else}
												<div class="list-group">
													{#each card.progressCards as progressCard}
														<div
															class="list-group-item"
														>
															<div
																class="d-flex justify-content-between align-items-start gap-3"
															>
																<div>
																	<h6
																		class="mb-1"
																	>
																		{#if progressCard.lesson}
																			Lesson
																			{progressCard
																				.lesson
																				.lessonNumber}:
																			{progressCard
																				.lesson
																				.title}
																		{:else}
																			Unknown
																			Lesson
																		{/if}
																	</h6>

																	<small
																		class="text-muted d-block mb-2"
																	>
																		Progress
																		ID: {progressCard
																			.progress
																			.id}
																	</small>

																	{#if progressCard.progress.state === "MATERIAL_DONE"}
																		<div
																			class="alert alert-warning py-2 mb-2"
																		>
																			The
																			learner
																			completed
																			the
																			material
																			and
																			is
																			waiting
																			for
																			meeting
																			confirmation.
																		</div>

																		<form
																			method="POST"
																			action="?/confirmMeeting"
																		>
																			<input
																				type="hidden"
																				name="progressId"
																				value={progressCard
																					.progress
																					.id}
																			/>

																			<button
																				type="submit"
																				class="btn btn-success btn-sm"
																			>
																				Confirm
																				Meeting
																			</button>
																		</form>
																	{:else if progressCard.progress.state === "MEETING_DONE"}
																		<div
																			class="alert alert-info py-2 mb-0"
																		>
																			Meeting
																			confirmed.
																			The
																			learner
																			can
																			now
																			submit
																			the
																			quiz.
																		</div>
																	{:else if progressCard.progress.state === "PASSED"}
																		<div
																			class="alert alert-success py-2 mb-0"
																		>
																			Lesson
																			passed.
																		</div>
																	{:else if progressCard.progress.state === "UNLOCKED"}
																		<div
																			class="alert alert-secondary py-2 mb-0"
																		>
																			Lesson
																			is
																			unlocked.
																			Learner
																			has
																			not
																			completed
																			the
																			material
																			yet.
																		</div>
																	{:else}
																		<div
																			class="alert alert-secondary py-2 mb-0"
																		>
																			Lesson
																			is
																			currently
																			locked.
																		</div>
																	{/if}
																</div>

																<span
																	class={`badge ${getStateBadgeClass(progressCard.progress.state)}`}
																>
																	{progressCard
																		.progress
																		.state}
																</span>
															</div>
														</div>
													{/each}
												</div>
											{/if}
										</div>
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
