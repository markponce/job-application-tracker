@param boolean resendSent
@param com.example.job_application_tracker.account.ResendVerificationForm form
@param java.util.List<String> emailErrors
@param org.springframework.security.web.csrf.CsrfToken csrfToken
<!doctype html>
<html lang="en">
@template.layouts.head(pageTitle = "Resend verification")
<body class="min-h-screen bg-slate-50 text-slate-900 antialiased">
<main class="mx-auto flex min-h-screen max-w-md flex-col justify-center px-5 py-12">
    <section class="rounded-xl border border-slate-200 bg-white p-7 shadow-sm">
        <h1 class="text-xl font-semibold">Resend verification email</h1>
        @if(resendSent)
            <p class="mt-4 rounded-lg bg-emerald-50 p-3 text-sm text-emerald-800">If the address has an unverified account, a new link will be sent.</p>
        @endif
        @if(!emailErrors.isEmpty())
            <ul role="alert" class="mt-4 list-inside list-disc rounded-lg bg-rose-50 p-3 text-sm text-rose-700">
                @for(String error : emailErrors)
                    <li>${error}</li>
                @endfor
            </ul>
        @endif
        <form method="post" action="/verification/resend" class="mt-6 space-y-4">
            <input type="hidden" name="${csrfToken.getParameterName()}" value="${csrfToken.getToken()}">
            <div>
                <label for="email" class="mb-1.5 block text-sm font-medium">Email</label>
                <input id="email" name="email" type="email" autocomplete="email" value="${form.email()}"
                       aria-invalid="${!emailErrors.isEmpty()}"
                       class="w-full rounded-lg border ${!emailErrors.isEmpty() ? "border-rose-500" : "border-slate-300"} px-3 py-2.5 text-sm">
            </div>
            <button type="submit" class="w-full rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white">Send verification email</button>
        </form>
        <a href="/login" class="mt-5 block text-center text-sm text-indigo-700">Back to sign in</a>
    </section>
</main>
</body>
</html>
