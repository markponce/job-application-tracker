@param org.springframework.security.web.csrf.CsrfToken csrfToken
@param com.example.job_application_tracker.account.AccountProfile accountProfile
<!doctype html>
<html lang="en">
@template.layouts.head(pageTitle = "Something went wrong")
<body class="min-h-screen bg-slate-50 text-slate-900 antialiased">
<div class="min-h-screen lg:flex">
    @template.layouts.sidebar(activePage = "applications")
    <div class="min-w-0 flex-1">
        @template.layouts.topbar(pageTitle = "Something went wrong", csrfToken = csrfToken, accountProfile = accountProfile)
        <main class="mx-auto w-full max-w-7xl p-5 sm:p-8">
            <section class="mx-auto max-w-lg rounded-xl border border-slate-200 bg-white px-6 py-14 text-center shadow-sm">
                <p class="text-sm font-semibold uppercase tracking-widest text-rose-600">500</p>
                <h2 class="mt-3 text-2xl font-semibold">Something went wrong</h2>
                <p class="mt-2 text-sm text-slate-500">We couldn't complete your request. Please try again.</p>
                <a href="/" class="mt-6 inline-flex rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700">Back to ApplyTrack</a>
            </section>
            @template.layouts.footer()
        </main>
    </div>
</div>
</body>
</html>
