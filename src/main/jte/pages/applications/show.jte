@param com.example.job_application_tracker.jobapplication.model.JobApplication application
@param String successMessage
@param org.springframework.security.web.csrf.CsrfToken csrfToken
@param com.example.job_application_tracker.account.AccountProfile accountProfile
<!doctype html>
<html lang="en">
@template.layouts.head(pageTitle = "Application details")
<body class="min-h-screen bg-slate-50 text-slate-900 antialiased">
<div class="min-h-screen lg:flex">
    @template.layouts.sidebar(activePage = "applications")
    <div class="min-w-0 flex-1">
        @template.layouts.topbar(pageTitle = "Application details", csrfToken = csrfToken, accountProfile = accountProfile)
        <main class="mx-auto w-full max-w-7xl p-5 sm:p-8">
@if(successMessage != null)
    <div class="mb-6 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">${successMessage}</div>
@endif
<div class="mx-auto max-w-5xl">
    <a href="/applications" class="text-sm font-medium text-slate-500 hover:text-slate-800">← Back to applications</a>
    <section class="mt-5 rounded-xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
        <div class="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
            <div>
                <span class="rounded-full bg-indigo-50 px-2.5 py-1 text-xs font-semibold text-indigo-700">${application.getStatus()}</span>
                <h2 class="mt-4 text-2xl font-semibold tracking-tight">${application.getTitle()}</h2>
                <p class="mt-1 text-lg text-slate-500">${application.getCompanyName()}</p>
            </div>
            <div class="flex gap-2">
                <a href="/applications/${java.util.Objects.toString(application.getId(), "")}/edit" class="rounded-lg border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50">Edit</a>
                <form method="post" action="/applications/${java.util.Objects.toString(application.getId(), "")}/delete" onsubmit="return confirm('Delete this application? This cannot be undone.')">
                    <input type="hidden" name="${csrfToken.getParameterName()}" value="${csrfToken.getToken()}">
                    <button type="submit" class="rounded-lg border border-rose-200 px-4 py-2 text-sm font-semibold text-rose-700 hover:bg-rose-50">Delete</button>
                </form>
            </div>
        </div>
        <div class="mt-8 grid gap-x-8 gap-y-6 border-t border-slate-100 pt-7 sm:grid-cols-2 lg:grid-cols-3">
            <div><p class="text-xs font-semibold uppercase tracking-wide text-slate-400">Work setup</p><p class="mt-2 text-sm text-slate-800">${application.getWorkSetup()}</p></div>
            <div><p class="text-xs font-semibold uppercase tracking-wide text-slate-400">Experience</p><p class="mt-2 text-sm text-slate-800">${application.getExperienceLevel() != null ? application.getExperienceLevel().name() : "—"}</p></div>
            <div><p class="text-xs font-semibold uppercase tracking-wide text-slate-400">Applied at</p><p class="mt-2 text-sm text-slate-800">${application.getAppliedAt() != null ? application.getAppliedAt().toString() : "—"}</p></div>
            <div><p class="text-xs font-semibold uppercase tracking-wide text-slate-400">Salary range</p><p class="mt-2 text-sm text-slate-800">${application.getSalaryMin() != null ? application.getSalaryMin().toPlainString() : "—"} – ${application.getSalaryMax() != null ? application.getSalaryMax().toPlainString() : "—"} ${application.getSalaryCurrency() != null ? application.getSalaryCurrency() : ""} ${application.getSalaryPeriod() != null ? application.getSalaryPeriod().name() : ""}</p></div>
            <div><p class="text-xs font-semibold uppercase tracking-wide text-slate-400">Company address</p><p class="mt-2 text-sm text-slate-800">${application.getCompanyAddress() != null ? application.getCompanyAddress() : "—"}</p></div>
            <div><p class="text-xs font-semibold uppercase tracking-wide text-slate-400">Job posting</p><a href="${application.getUrl()}" target="_blank" rel="noopener noreferrer" class="mt-2 block break-all text-sm font-medium text-indigo-600 hover:text-indigo-800">Open job posting ↗</a></div>
        </div>
        @if(application.getContent() != null && !application.getContent().isBlank())
            <div class="mt-8 border-t border-slate-100 pt-6">
                <h3 class="text-sm font-semibold">Job description</h3>
                <p class="mt-3 whitespace-pre-wrap text-sm leading-6 text-slate-600">${application.getContent()}</p>
            </div>
        @endif
        @if(application.getNotes() != null && !application.getNotes().isBlank())
            <div class="mt-8 border-t border-slate-100 pt-6">
                <h3 class="text-sm font-semibold">Notes</h3>
                <p class="mt-3 whitespace-pre-wrap text-sm leading-6 text-slate-600">${application.getNotes()}</p>
            </div>
        @endif
        <p class="mt-8 border-t border-slate-100 pt-4 text-xs text-slate-400">Created ${application.getCreatedAt() != null ? application.getCreatedAt().toString() : "—"} · Updated ${application.getUpdatedAt() != null ? application.getUpdatedAt().toString() : "—"}</p>
    </section>
</div>
            @template.layouts.footer()
        </main>
    </div>
</div>
</body>
</html>
