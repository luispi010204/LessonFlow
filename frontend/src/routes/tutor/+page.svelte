<script>
    let { data } = $props();

    let user = data.user;
    let isAuthenticated = data.isAuthenticated;
    let isTutor =
        isAuthenticated && user.user_roles && user.user_roles.includes("tutor");
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
        This page is intended for tutors. Learners can browse courses and
        continue learning from their dashboard.
    </div>
    <a href="/courses" class="btn btn-primary">Browse Courses</a>
{:else}
    <div class="row g-3 mb-4">
        <div class="col-md-4">
            <div class="card shadow-sm h-100">
                <div class="card-body">
                    <h5 class="card-title">My Courses</h5>
                    <p class="display-6 mb-0">—</p>
                    <p class="text-muted mb-0">
                        Will be loaded from the backend.
                    </p>
                </div>
            </div>
        </div>

        <div class="col-md-4">
            <div class="card shadow-sm h-100">
                <div class="card-body">
                    <h5 class="card-title">Lessons</h5>
                    <p class="display-6 mb-0">—</p>
                    <p class="text-muted mb-0">Lessons across your courses.</p>
                </div>
            </div>
        </div>

        <div class="col-md-4">
            <div class="card shadow-sm h-100">
                <div class="card-body">
                    <h5 class="card-title">Quizzes</h5>
                    <p class="display-6 mb-0">—</p>
                    <p class="text-muted mb-0">
                        Manual or AI-generated quizzes later.
                    </p>
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
                    <p>
                        This form will create new courses for the current tutor.
                    </p>

                    <form>
                        <div class="mb-3">
                            <label for="title" class="form-label">Title</label>
                            <input
                                id="title"
                                type="text"
                                class="form-control"
                                disabled
                            />
                        </div>

                        <div class="mb-3">
                            <label for="description" class="form-label"
                                >Description</label
                            >
                            <textarea
                                id="description"
                                class="form-control"
                                rows="3"
                                disabled
                            ></textarea>
                        </div>

                        <p class="text-muted">
                            New courses are created as <span
                                class="badge bg-secondary">DRAFT</span
                            >.
                        </p>

                        <button class="btn btn-primary" disabled
                            >Create Course</button
                        >
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
                    <p>
                        This section will list all courses owned by the current
                        tutor.
                    </p>

                    <div class="border rounded p-3 bg-light">
                        <h6>Example Tutor Course Card</h6>
                        <p class="mb-2">
                            Course title, status and lesson count will be
                            displayed here.
                        </p>
                        <button class="btn btn-primary" disabled
                            >Manage Course</button
                        >
                    </div>
                </div>
            </div>
        </div>
    </div>
{/if}
