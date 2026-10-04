@param com.example.job_application_tracker.account.PasswordChangeForm form
@param java.util.List<String> formErrors
@param org.springframework.security.web.csrf.CsrfToken csrfToken
@param com.example.job_application_tracker.account.AccountProfile accountProfile
<!doctype html>
<html lang="en">
@template.layouts.head(pageTitle = "Change password")
<body class="min-h-screen bg-slate-50 text-slate-900 antialiased">
<div class="min-h-screen lg:flex">
    @template.layouts.sidebar(activePage = "account")
    <div class="min-w-0 flex-1">
        @template.layouts.topbar(pageTitle = "Change password", csrfToken = csrfToken, accountProfile = accountProfile)
        <main class="mx-auto w-full max-w-3xl p-5 sm:p-8">
            <section class="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
                <h2 class="text-lg font-semibold">Update your password</h2>
                @if(!formErrors.isEmpty())
                    <ul role="alert" class="mt-4 list-inside list-disc rounded-lg bg-rose-50 p-3 text-sm text-rose-700">
                        @for(String error : formErrors)
                            <li>${error}</li>
                        @endfor
                    </ul>
                @endif
                <form method="post" action="/account/password" class="mt-6 space-y-4">
                    <input type="hidden" name="${csrfToken.getParameterName()}" value="${csrfToken.getToken()}">
                    <div><label for="currentPassword" class="mb-1.5 block text-sm font-medium">Current password</label><input id="currentPassword" name="currentPassword" type="password" required autocomplete="current-password" class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm"></div>
                    <div><label for="newPassword" class="mb-1.5 block text-sm font-medium">New password</label><input id="newPassword" name="newPassword" type="password" required minlength="12" maxlength="72" autocomplete="new-password" class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm"></div>
                    <div><label for="confirmPassword" class="mb-1.5 block text-sm font-medium">Confirm new password</label><input id="confirmPassword" name="confirmPassword" type="password" required autocomplete="new-password" class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm"></div>
                    <button type="submit" class="rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white">Change password</button>
                </form>
            </section>
        </main>
    </div>
</div>
</body>
</html>
