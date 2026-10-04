@param long retryAfterSeconds
@param org.springframework.security.web.csrf.CsrfToken csrfToken
<!doctype html>
<html lang="en">
@template.layouts.head(pageTitle = "Too many requests")
<body class="min-h-screen bg-slate-50 text-slate-900 antialiased">
<main class="mx-auto flex min-h-screen max-w-md flex-col justify-center px-5 py-12">
    <section class="rounded-xl border border-slate-200 bg-white p-7 text-center shadow-sm">
        <h1 class="text-xl font-semibold">Too many requests</h1>
        <p class="mt-3 text-sm text-slate-600">Please wait ${retryAfterSeconds} seconds before trying again.</p>
        <a href="/login" class="mt-5 inline-block text-sm font-medium text-indigo-700">Back to sign in</a>
    </section>
</main>
</body>
</html>
