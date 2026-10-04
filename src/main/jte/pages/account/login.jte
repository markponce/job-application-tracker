@param boolean loginError
@param boolean loggedOut
@param boolean sessionExpired
@param boolean passwordChanged
@param boolean loginThrottled
@param org.springframework.security.web.csrf.CsrfToken csrfToken
<!doctype html>
<html lang="en">
@template.layouts.head(pageTitle = "Sign in")
<body class="min-h-screen bg-slate-50 text-slate-900 antialiased">
<main class="mx-auto flex min-h-screen max-w-md flex-col justify-center px-5 py-12">
    <a href="/login" class="mb-8 text-center text-2xl font-bold tracking-tight text-indigo-700">ApplyTrack</a>
    <section class="rounded-xl border border-slate-200 bg-white p-7 shadow-sm">
        <h1 class="text-xl font-semibold">Sign in</h1>
        @if(loginError)
            <p role="alert" class="mt-4 rounded-lg bg-rose-50 p-3 text-sm text-rose-700">Email or password is incorrect, or your email has not been verified.</p>
        @endif
        @if(loggedOut)
            <p class="mt-4 rounded-lg bg-emerald-50 p-3 text-sm text-emerald-700">You have been signed out.</p>
        @endif
        @if(sessionExpired)
            <p role="alert" class="mt-4 rounded-lg bg-amber-50 p-3 text-sm text-amber-800">Your session ended after a password change. Please sign in again.</p>
        @endif
        @if(passwordChanged)
            <p class="mt-4 rounded-lg bg-emerald-50 p-3 text-sm text-emerald-800">Your password was changed. Sign in with the new password.</p>
        @endif
        @if(loginThrottled)
            <p role="alert" class="mt-4 rounded-lg bg-amber-50 p-3 text-sm text-amber-800">Too many sign-in attempts. Please wait before trying again.</p>
        @endif
        <form method="post" action="/login" class="mt-6 space-y-4">
            <input type="hidden" name="${csrfToken.getParameterName()}" value="${csrfToken.getToken()}">
            <div>
                <label for="email" class="mb-1.5 block text-sm font-medium">Email</label>
                <input id="email" name="email" type="email" required autocomplete="email" class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm">
            </div>
            <div>
                <label for="password" class="mb-1.5 block text-sm font-medium">Password</label>
                <input id="password" name="password" type="password" required autocomplete="current-password" class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm">
            </div>
            <button type="submit" class="w-full rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-indigo-700">Sign in</button>
        </form>
        <p class="mt-5 text-center text-sm text-slate-600">Need an account? <a href="/register" class="font-medium text-indigo-700">Register</a></p>
        <p class="mt-2 text-center text-sm"><a href="/verification/resend" class="text-indigo-700">Resend verification email</a></p>
    </section>
</main>
</body>
</html>
