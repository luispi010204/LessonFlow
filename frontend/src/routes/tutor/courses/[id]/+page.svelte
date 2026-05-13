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

	function getMaterialPreview(material) {
		if (!material) {
			return "";
		}

		const maxLength = 450;

		if (material.length <= maxLength) {
			return material;
		}

		return material.slice(0, maxLength) + "...";
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

	function getOptionClass(optionIndex, correctOptionIndex) {
		if (optionIndex === correctOptionIndex) {
			return "list-group-item-success border-success";
		}

		return "";
	}

	function getQuestionOption(question, optionIndex) {
		if (!question.options || !question.options[optionIndex]) {
			return "";
		}

		return question.options[optionIndex];
	}
</script>

<div class="d-flex justify-content-between align-items-center mb-4">
	<div>
		<h1 class="mb-1">Manage Course</h1>
		<p class="text-muted mb-0">Course ID: {courseId}</p>
	</div>

	<a href="/tutor" class="btn btn-outline-secondary">
		Back to Tutor Dashboard
	</a>
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
				<div class="d-flex justify-content-between align-items-start mb-2">
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
								Lesson numbers are generated automatically based on the existing lessons.
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
							<label class="form-label" for="material">Material</label>
							<textarea
								id="material"
								name="material"
								class="form-control lesson-material-textarea"
								rows="10"
								placeholder={`## Introduction
Shortly explain what this lesson is about.

## Key Concepts
- Concept 1
- Concept 2
- Concept 3

## Example
Add a short example, code snippet or explanation.

## Task
Describe what the learner should understand or try out.`}
								required
							></textarea>
							<div class="form-text">
								Use short sections, bullet points and examples. Line breaks will be preserved for the learner.
							</div>
						</div>

						<div class="mb-3">
							<label class="form-label" for="meetingLink">Meeting Link</label>
							<input
								id="meetingLink"
								name="meetingLink"
								class="form-control"
								type="text"
								required
							/>
						</div>

						<button class="btn btn-primary" type="submit">
							Create Lesson
						</button>
					</form>
				</div>
			</div>

			<div class="card shadow-sm">
				<div class="card-header">
					<h5 class="mb-0">AI Quiz Generation</h5>
				</div>

				<div class="card-body">
					<p class="text-muted mb-3">
						Quizzes are generated from the lesson material using AI. Create a lesson first, then use the
						<strong>Generate AI Quiz</strong> button in the lesson list.
					</p>

					<div class="alert alert-info mb-0">
						The generated quiz uses the existing single-choice format:
						one question, four answer options and exactly one correct answer.
					</div>
				</div>
			</div>
		</div>

		<div class="col-lg-7">
			<div class="card shadow-sm mb-3">
				<div class="card-header d-flex justify-content-between align-items-center">
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
							{#each lessonCards as card, lessonIndex}
								<div class="list-group-item">
									<div class="d-flex justify-content-between align-items-start gap-3">
										<div class="w-100">
											<h6 class="mb-1">
												Lesson {card.lesson.lessonNumber}: {card.lesson.title}
											</h6>

											<div class="border rounded p-3 bg-light mb-2 lesson-material-preview">
												{getMaterialPreview(card.lesson.material)}
											</div>

											{#if card.lesson.material && card.lesson.material.length > 450}
												<small class="text-muted d-block mb-2">
													Material preview shortened. Learners see the full text in the learning flow.
												</small>
											{/if}

											{#if card.lesson.meetingLink}
												<p class="mb-1">
													<strong>Meeting:</strong>
													<a href={card.lesson.meetingLink} target="_blank">
														{card.lesson.meetingLink}
													</a>
												</p>
											{/if}

											<small class="text-muted d-block mb-2">
												Lesson ID: {card.lesson.id}
											</small>

											<div class="collapse" id={`edit-lesson-${lessonIndex}`}>
												<div class="border rounded p-3 mb-3">
													<h6 class="mb-3">Edit Lesson</h6>

													<form method="POST" action="?/updateLesson">
														<input type="hidden" name="lessonId" value={card.lesson.id} />

														<div class="mb-3">
															<label class="form-label" for={`edit-lesson-title-${lessonIndex}`}>
																Title
															</label>
															<input
																id={`edit-lesson-title-${lessonIndex}`}
																name="title"
																class="form-control"
																type="text"
																value={card.lesson.title}
																required
															/>
														</div>

														<div class="mb-3">
															<label class="form-label" for={`edit-lesson-material-${lessonIndex}`}>
																Material
															</label>
															<textarea
																id={`edit-lesson-material-${lessonIndex}`}
																name="material"
																class="form-control lesson-material-textarea"
																rows="8"
																required
															>{card.lesson.material}</textarea>
														</div>

														<div class="mb-3">
															<label class="form-label" for={`edit-lesson-meeting-link-${lessonIndex}`}>
																Meeting Link
															</label>
															<input
																id={`edit-lesson-meeting-link-${lessonIndex}`}
																name="meetingLink"
																class="form-control"
																type="text"
																value={card.lesson.meetingLink}
																required
															/>
														</div>

														<button type="submit" class="btn btn-primary btn-sm">
															Save Lesson Changes
														</button>
													</form>
												</div>
											</div>

											{#if card.quiz}
												<div class="border rounded p-3 bg-light quiz-preview">
													<div class="d-flex justify-content-between align-items-center mb-3">
														<div>
															<strong>AI Quiz ready</strong>
															<div class="text-muted small">
																Pass percent: {card.quiz.passPercent}% · Questions: {card.quiz.questions ? card.quiz.questions.length : 0}
															</div>
														</div>

														<div class="d-flex gap-2 align-items-center">
															<button
																class="btn btn-outline-secondary btn-sm"
																type="button"
																data-bs-toggle="collapse"
																data-bs-target={`#edit-quiz-${lessonIndex}`}
																aria-expanded="false"
																aria-controls={`edit-quiz-${lessonIndex}`}
															>
																Edit Quiz
															</button>

															<span class="badge bg-success">Generated</span>
														</div>
													</div>

													{#if card.quiz.questions && card.quiz.questions.length > 0}
														<div class="accordion" id={`quiz-preview-${card.lesson.id}`}>
															{#each card.quiz.questions as question, questionIndex}
																<div class="accordion-item quiz-question-card">
																	<h2
																		class="accordion-header"
																		id={`quiz-heading-${card.lesson.id}-${questionIndex}`}
																	>
																		<button
																			class="accordion-button {questionIndex === 0 ? '' : 'collapsed'}"
																			type="button"
																			data-bs-toggle="collapse"
																			data-bs-target={`#quiz-collapse-${card.lesson.id}-${questionIndex}`}
																			aria-expanded={questionIndex === 0 ? "true" : "false"}
																			aria-controls={`quiz-collapse-${card.lesson.id}-${questionIndex}`}
																		>
																			Question {questionIndex + 1}: {question.questionText}
																		</button>
																	</h2>

																	<div
																		id={`quiz-collapse-${card.lesson.id}-${questionIndex}`}
																		class="accordion-collapse collapse {questionIndex === 0 ? 'show' : ''}"
																		aria-labelledby={`quiz-heading-${card.lesson.id}-${questionIndex}`}
																		data-bs-parent={`#quiz-preview-${card.lesson.id}`}
																	>
																		<div class="accordion-body">
																			{#if question.options && question.options.length > 0}
																				<div class="list-group">
																					{#each question.options as option, optionIndex}
																						<div
																							class={`list-group-item py-2 ${getOptionClass(optionIndex, question.correctOptionIndex)}`}
																						>
																							<div class="d-flex justify-content-between align-items-start gap-2">
																								<div>
																									<strong>{getOptionLabel(optionIndex)}:</strong>
																									{option}
																								</div>

																								{#if optionIndex === question.correctOptionIndex}
																									<span class="badge bg-success">Correct</span>
																								{/if}
																							</div>
																						</div>
																					{/each}
																				</div>
																			{:else}
																				<div class="alert alert-warning mb-0">
																					This question has no answer options.
																				</div>
																			{/if}
																		</div>
																	</div>
																</div>
															{/each}
														</div>
													{:else}
														<div class="alert alert-warning mb-0">
															This quiz does not contain any questions.
														</div>
													{/if}
												</div>

												<div class="collapse mt-3" id={`edit-quiz-${lessonIndex}`}>
													<div class="border rounded p-3">
														<h6 class="mb-3">Edit Quiz</h6>

														<form method="POST" action="?/updateQuiz">
															<input type="hidden" name="quizId" value={card.quiz.id} />
															<input
																type="hidden"
																name="questionCount"
																value={card.quiz.questions ? card.quiz.questions.length : 0}
															/>

															<div class="mb-3">
																<label class="form-label" for={`edit-quiz-pass-percent-${lessonIndex}`}>
																	Pass Percent
																</label>
																<input
																	id={`edit-quiz-pass-percent-${lessonIndex}`}
																	name="passPercent"
																	class="form-control"
																	type="number"
																	min="1"
																	max="100"
																	value={card.quiz.passPercent}
																	required
																/>
															</div>

															{#if card.quiz.questions && card.quiz.questions.length > 0}
																{#each card.quiz.questions as question, questionIndex}
																	<div class="border rounded p-3 mb-3">
																		<h6 class="mb-3">Question {questionIndex + 1}</h6>

																		<div class="mb-3">
																			<label
																				class="form-label"
																				for={`edit-question-text-${lessonIndex}-${questionIndex}`}
																			>
																				Question Text
																			</label>
																			<input
																				id={`edit-question-text-${lessonIndex}-${questionIndex}`}
																				name={`questionText-${questionIndex}`}
																				class="form-control"
																				type="text"
																				value={question.questionText}
																				required
																			/>
																		</div>

																		<div class="row g-2">
																			<div class="col-md-6">
																				<label
																					class="form-label"
																					for={`edit-question-option-a-${lessonIndex}-${questionIndex}`}
																				>
																					Option A
																				</label>
																				<input
																					id={`edit-question-option-a-${lessonIndex}-${questionIndex}`}
																					name={`questionOptionA-${questionIndex}`}
																					class="form-control"
																					type="text"
																					value={getQuestionOption(question, 0)}
																					required
																				/>
																			</div>

																			<div class="col-md-6">
																				<label
																					class="form-label"
																					for={`edit-question-option-b-${lessonIndex}-${questionIndex}`}
																				>
																					Option B
																				</label>
																				<input
																					id={`edit-question-option-b-${lessonIndex}-${questionIndex}`}
																					name={`questionOptionB-${questionIndex}`}
																					class="form-control"
																					type="text"
																					value={getQuestionOption(question, 1)}
																					required
																				/>
																			</div>

																			<div class="col-md-6">
																				<label
																					class="form-label"
																					for={`edit-question-option-c-${lessonIndex}-${questionIndex}`}
																				>
																					Option C
																				</label>
																				<input
																					id={`edit-question-option-c-${lessonIndex}-${questionIndex}`}
																					name={`questionOptionC-${questionIndex}`}
																					class="form-control"
																					type="text"
																					value={getQuestionOption(question, 2)}
																					required
																				/>
																			</div>

																			<div class="col-md-6">
																				<label
																					class="form-label"
																					for={`edit-question-option-d-${lessonIndex}-${questionIndex}`}
																				>
																					Option D
																				</label>
																				<input
																					id={`edit-question-option-d-${lessonIndex}-${questionIndex}`}
																					name={`questionOptionD-${questionIndex}`}
																					class="form-control"
																					type="text"
																					value={getQuestionOption(question, 3)}
																					required
																				/>
																			</div>
																		</div>

																		<div class="mt-3">
																			<label
																				class="form-label"
																				for={`edit-correct-option-${lessonIndex}-${questionIndex}`}
																			>
																				Correct Answer
																			</label>
																			<select
																				id={`edit-correct-option-${lessonIndex}-${questionIndex}`}
																				name={`correctOptionIndex-${questionIndex}`}
																				class="form-select"
																				required
																			>
																				<option value="0" selected={question.correctOptionIndex === 0}>
																					Option A
																				</option>
																				<option value="1" selected={question.correctOptionIndex === 1}>
																					Option B
																				</option>
																				<option value="2" selected={question.correctOptionIndex === 2}>
																					Option C
																				</option>
																				<option value="3" selected={question.correctOptionIndex === 3}>
																					Option D
																				</option>
																			</select>
																		</div>
																	</div>
																{/each}

																<button type="submit" class="btn btn-primary btn-sm">
																	Save Quiz Changes
																</button>
															{:else}
																<div class="alert alert-warning mb-0">
																	This quiz does not contain editable questions.
																</div>
															{/if}
														</form>
													</div>
												</div>
											{:else}
												<div class="alert alert-warning py-2 mb-3">
													No quiz created for this lesson yet.
												</div>

												<form method="POST" action="?/generateAiQuiz" class="border rounded p-3">
													<input type="hidden" name="lessonId" value={card.lesson.id} />

													<h6 class="mb-3">Generate AI Quiz</h6>

													<div class="row g-2 mb-3">
														<div class="col-md-6">
															<label
																class="form-label"
																for={`question-count-${card.lesson.id}`}
															>
																Questions
															</label>
															<input
																id={`question-count-${card.lesson.id}`}
																name="questionCount"
																class="form-control form-control-sm"
																type="number"
																min="1"
																max="10"
																value="3"
																required
															/>
														</div>

														<div class="col-md-6">
															<label
																class="form-label"
																for={`pass-percent-${card.lesson.id}`}
															>
																Pass Percent
															</label>
															<input
																id={`pass-percent-${card.lesson.id}`}
																name="passPercent"
																class="form-control form-control-sm"
																type="number"
																min="1"
																max="100"
																value="70"
																required
															/>
														</div>
													</div>

													<button type="submit" class="btn btn-primary btn-sm">
														Generate AI Quiz
													</button>
												</form>
											{/if}
										</div>

										<div class="d-flex flex-column gap-2 align-items-end">
											<button
												class="btn btn-outline-secondary btn-sm"
												type="button"
												data-bs-toggle="collapse"
												data-bs-target={`#edit-lesson-${lessonIndex}`}
												aria-expanded="false"
												aria-controls={`edit-lesson-${lessonIndex}`}
											>
												Edit
											</button>

											{#if card.quiz}
												<span class="badge bg-success">Quiz ready</span>
											{:else}
												<span class="badge bg-warning text-dark">No quiz</span>
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
				<div class="card-header d-flex justify-content-between align-items-center">
					<h5 class="mb-0">Learner Progress / Meeting Confirmations</h5>
					<span class="badge bg-secondary">{enrollmentProgressCards.length}</span>
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
									<h2 class="accordion-header" id={`heading-${index}`}>
										<button
											class="accordion-button {index === 0 ? '' : 'collapsed'}"
											type="button"
											data-bs-toggle="collapse"
											data-bs-target={`#collapse-${index}`}
											aria-expanded={index === 0 ? "true" : "false"}
											aria-controls={`collapse-${index}`}
										>
											Learner: {card.enrollment.learnerUserId}
										</button>
									</h2>

									<div
										id={`collapse-${index}`}
										class="accordion-collapse collapse {index === 0 ? 'show' : ''}"
										aria-labelledby={`heading-${index}`}
										data-bs-parent="#learnerProgressAccordion"
									>
										<div class="accordion-body">
											<p class="text-muted small mb-3">
												Enrollment ID: {card.enrollment.id}
											</p>

											{#if card.error}
												<div class="alert alert-danger mb-0">
													{card.error}
												</div>
											{:else if card.progressCards.length === 0}
												<p class="text-muted mb-0">
													No progress entries found for this learner.
												</p>
											{:else}
												<div class="list-group">
													{#each card.progressCards as progressCard}
														<div class="list-group-item">
															<div class="d-flex justify-content-between align-items-start gap-3">
																<div>
																	<h6 class="mb-1">
																		{#if progressCard.lesson}
																			Lesson {progressCard.lesson.lessonNumber}: {progressCard.lesson.title}
																		{:else}
																			Unknown Lesson
																		{/if}
																	</h6>

																	<small class="text-muted d-block mb-2">
																		Progress ID: {progressCard.progress.id}
																	</small>

																	{#if progressCard.progress.state === "MATERIAL_DONE"}
																		<div class="alert alert-warning py-2 mb-2">
																			The learner completed the material and is waiting for meeting confirmation.
																		</div>

																		<form method="POST" action="?/confirmMeeting">
																			<input
																				type="hidden"
																				name="progressId"
																				value={progressCard.progress.id}
																			/>

																			<button type="submit" class="btn btn-success btn-sm">
																				Confirm Meeting
																			</button>
																		</form>
																	{:else if progressCard.progress.state === "MEETING_DONE"}
																		<div class="alert alert-info py-2 mb-0">
																			Meeting confirmed. The learner can now submit the quiz.
																		</div>
																	{:else if progressCard.progress.state === "PASSED"}
																		<div class="alert alert-success py-2 mb-0">
																			Lesson passed.
																		</div>
																	{:else if progressCard.progress.state === "UNLOCKED"}
																		<div class="alert alert-secondary py-2 mb-0">
																			Lesson is unlocked. Learner has not completed the material yet.
																		</div>
																	{:else}
																		<div class="alert alert-secondary py-2 mb-0">
																			Lesson is currently locked.
																		</div>
																	{/if}
																</div>

																<span class={`badge ${getStateBadgeClass(progressCard.progress.state)}`}>
																	{progressCard.progress.state}
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

<style>
	.lesson-material-textarea {
		min-height: 260px;
	}

	.lesson-material-preview {
		white-space: pre-wrap;
		font-size: 0.95rem;
		line-height: 1.5;
		max-height: 220px;
		overflow-y: auto;
	}

	.quiz-preview {
		font-size: 0.95rem;
	}

	.quiz-question-card + .quiz-question-card {
		margin-top: 0.5rem;
	}
</style>