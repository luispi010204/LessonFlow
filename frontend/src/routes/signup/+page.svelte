<script>
	let { form } = $props();
</script>

<div class="row justify-content-center mt-5">
	<div class="col-md-6 col-lg-5">
		<div class="card shadow-sm">
			<div class="card-header">
				<h3 class="mb-0">Sign up</h3>
			</div>

			<div class="card-body">
				{#if form?.error}
					<div class="alert alert-danger" role="alert">
						{form.error}
					</div>
				{/if}

				<form method="POST">
					<div class="mb-3">
						<label for="email" class="form-label">Email</label>
						<input id="email" name="email" type="email" class="form-control" required />
					</div>

					<div class="mb-3">
						<label for="password" class="form-label">Password</label>
						<input id="password" name="password" type="password" class="form-control" required />
					</div>

					<div class="mb-3">
						<label for="firstName" class="form-label">First name</label>
						<input id="firstName" name="firstName" type="text" class="form-control" />
					</div>

					<div class="mb-3">
						<label for="lastName" class="form-label">Last name</label>
						<input id="lastName" name="lastName" type="text" class="form-control" />
					</div>

					<div class="d-flex gap-2">
						<button type="submit" class="btn btn-primary">Sign up</button>
						<a href="/login" class="btn btn-outline-primary">Login</a>
					</div>
				</form>
			</div>
		</div>
	</div>
</div>