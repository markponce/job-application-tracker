@param com.example.job_application_tracker.account.RegistrationForm form
@param java.util.List<String> formErrors
@param java.util.List<String> emailErrors
@param java.util.List<String> firstNameErrors
@param java.util.List<String> lastNameErrors
@param java.util.List<String> passwordErrors
@param java.util.List<String> confirmPasswordErrors
@param boolean registrationSubmitted
@param boolean emailVerificationEnabled
@param org.springframework.security.web.csrf.CsrfToken csrfToken
<!doctype html>
<html lang="en">
@template.layouts.head(pageTitle = "Register")
<body class="min-h-screen bg-slate-50 text-slate-900 antialiased">
<main class="mx-auto flex min-h-screen max-w-md flex-col justify-center px-5 py-12">
    <a href="/login" class="mb-8 text-center text-2xl font-bold tracking-tight text-indigo-700">ApplyTrack</a>
    <section class="rounded-xl border border-slate-200 bg-white p-7 shadow-sm">
        <h1 class="text-xl font-semibold">Create your account</h1>
        @if(registrationSubmitted)
            <p class="mt-4 rounded-lg bg-emerald-50 p-3 text-sm text-emerald-800">If the address can be registered, a
                @if(emailVerificationEnabled)
                    verification email will be sent shortly.
                @else
                    your account is ready to sign in.
                @endif
            </p>
        @endif
        @if(!formErrors.isEmpty())
            <ul role="alert" aria-live="polite" class="mt-4 list-inside list-disc rounded-lg bg-rose-50 p-3 text-sm text-rose-700">
                @for(String error : formErrors)
                    <li>${error}</li>
                @endfor
            </ul>
        @endif
        <form method="post" action="/register" class="mt-6 space-y-4">
            <input type="hidden" name="${csrfToken.getParameterName()}" value="${csrfToken.getToken()}">
            <div>
                <label for="email" class="mb-1.5 block text-sm font-medium">Email</label>
                <input id="email" name="email" type="email" required maxlength="320" autocomplete="email"
                       value="${form.email()}" class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm">
                @if(!emailErrors.isEmpty())
                    <ul id="email-errors" data-field-error="email" class="mt-1 text-sm text-rose-700">
                        @for(String error : emailErrors)
                            <li>${error}</li>
                        @endfor
                    </ul>
                @endif
            </div>
            <div>
                <label for="firstName" class="mb-1.5 block text-sm font-medium">First name</label>
                <input id="firstName" name="firstName" type="text" required maxlength="100" autocomplete="given-name"
                       value="${form.firstName()}" aria-invalid="${!firstNameErrors.isEmpty()}"
                       aria-describedby="${!firstNameErrors.isEmpty() ? "first-name-errors" : ""}"
                       class="w-full rounded-lg border ${!firstNameErrors.isEmpty() ? "border-rose-500" : "border-slate-300"} px-3 py-2.5 text-sm">
                @if(!firstNameErrors.isEmpty())
                    <ul id="first-name-errors" data-field-error="firstName" class="mt-1 text-sm text-rose-700">
                        @for(String error : firstNameErrors)
                            <li>${error}</li>
                        @endfor
                    </ul>
                @endif
            </div>
            <div>
                <label for="lastName" class="mb-1.5 block text-sm font-medium">Last name</label>
                <input id="lastName" name="lastName" type="text" required maxlength="100" autocomplete="family-name"
                       value="${form.lastName()}" aria-invalid="${!lastNameErrors.isEmpty()}"
                       aria-describedby="${!lastNameErrors.isEmpty() ? "last-name-errors" : ""}"
                       class="w-full rounded-lg border ${!lastNameErrors.isEmpty() ? "border-rose-500" : "border-slate-300"} px-3 py-2.5 text-sm">
                @if(!lastNameErrors.isEmpty())
                    <ul id="last-name-errors" data-field-error="lastName" class="mt-1 text-sm text-rose-700">
                        @for(String error : lastNameErrors)
                            <li>${error}</li>
                        @endfor
                    </ul>
                @endif
            </div>
            <div>
                <label for="password" class="mb-1.5 block text-sm font-medium">Password</label>
                <input id="password" name="password" type="password" required minlength="12" maxlength="72"
                       autocomplete="new-password"
                       aria-invalid="${!passwordErrors.isEmpty()}"
                       aria-describedby="${!passwordErrors.isEmpty() ? "password-errors" : ""}"
                       class="w-full rounded-lg border ${!passwordErrors.isEmpty() ? "border-rose-500" : "border-slate-300"} px-3 py-2.5 text-sm">
                @if(!passwordErrors.isEmpty())
                    <ul id="password-errors" data-field-error="password" class="mt-1 text-sm text-rose-700">
                        @for(String error : passwordErrors)
                            <li>${error}</li>
                        @endfor
                    </ul>
                @endif
            </div>
            <div>
                <label for="confirmPassword" class="mb-1.5 block text-sm font-medium">Confirm password</label>
                <input id="confirmPassword" name="confirmPassword" type="password" required minlength="12"
                       maxlength="72" autocomplete="new-password"
                       aria-invalid="${!confirmPasswordErrors.isEmpty()}"
                       aria-describedby="${!confirmPasswordErrors.isEmpty() ? "confirm-password-errors" : ""}"
                       class="w-full rounded-lg border ${!confirmPasswordErrors.isEmpty() ? "border-rose-500" : "border-slate-300"} px-3 py-2.5 text-sm">
                @if(!confirmPasswordErrors.isEmpty())
                    <ul id="confirm-password-errors" data-field-error="confirmPassword" class="mt-1 text-sm text-rose-700">
                        @for(String error : confirmPasswordErrors)
                            <li>${error}</li>
                        @endfor
                    </ul>
                @endif
            </div>
            <button type="submit"
                    class="w-full rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-indigo-700">
                Register
            </button>
        </form>
        <p class="mt-5 text-center text-sm text-slate-600">Already registered? <a href="/login"
                                                                                  class="font-medium text-indigo-700">Sign
                in</a></p>
    </section>
</main>
</body>
</html>
