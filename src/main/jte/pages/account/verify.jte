@param String verificationToken
@param boolean verificationComplete
@param boolean verificationSucceeded
@param org.springframework.security.web.csrf.CsrfToken csrfToken
<!doctype html>
<html lang="en">
@template.layouts.head(pageTitle = "Verify email")
<body class="min-h-screen bg-slate-50 text-slate-900 antialiased">
<main class="mx-auto flex min-h-screen max-w-md flex-col justify-center px-5 py-12">
    <section class="rounded-xl border border-slate-200 bg-white p-7 text-center shadow-sm">
        <h1 class="text-xl font-semibold">Email verification</h1>
        @if(verificationComplete)
            @if(verificationSucceeded)
                <p class="mt-4 text-sm text-emerald-800">Your email is verified. You can now sign in.</p>
                <a href="/login" class="mt-5 inline-block rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white">Sign in</a>
            @else
                <p role="alert" class="mt-4 text-sm text-rose-700">This verification link is invalid, expired, or has already been used.</p>
                <a href="/verification/resend" class="mt-5 inline-block text-sm font-medium text-indigo-700">Request another verification email</a>
            @endif
        @elseif(!verificationToken.isBlank())
            <p class="mt-3 text-sm text-slate-600">Confirm your email address to activate your account.</p>
            <form method="post" action="/verify" class="mt-5">
                <input type="hidden" name="${csrfToken.getParameterName()}" value="${csrfToken.getToken()}">
                <input type="hidden" name="token" value="${verificationToken}">
                <button type="submit" class="rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white">Verify email</button>
            </form>
        @else
            <p class="mt-3 text-sm text-slate-600">Use the verification link sent to your email address.</p>
        @endif
        <a href="/login" class="mt-5 block text-sm text-indigo-700">Back to sign in</a>
    </section>
</main>
</body>
</html>
