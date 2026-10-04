@param String pageTitle
@param org.springframework.security.web.csrf.CsrfToken csrfToken
@param com.example.job_application_tracker.account.AccountProfile accountProfile
<header class="flex min-h-16 items-center justify-between border-b border-slate-200 bg-white px-5 sm:px-8">
    <div>
        <p class="text-xs font-medium uppercase tracking-wider text-slate-400">Workspace</p>
        <h1 class="text-lg font-semibold">${pageTitle}</h1>
    </div>
    <div class="flex items-center gap-2">
        <details class="relative">
            <summary aria-label="Open profile menu"
                     class="flex cursor-pointer list-none items-center gap-2 rounded-lg px-2 py-1.5 hover:bg-slate-100">
                <span data-initials="${accountProfile.initials()}"
                      data-avatar-color="${accountProfile.avatarColorClass()}"
                      class="flex h-9 w-9 items-center justify-center rounded-full text-sm font-semibold ${accountProfile.avatarColorClass()}">
                    ${accountProfile.initials()}
                </span>
                <span class="hidden max-w-40 truncate text-sm font-medium text-slate-700 sm:block">
                    ${accountProfile.displayName()}
                </span>
            </summary>
            <div class="absolute right-0 z-20 mt-2 w-56 rounded-xl border border-slate-200 bg-white p-2 shadow-lg">
                <p class="truncate px-3 py-2 text-sm font-semibold text-slate-900">${accountProfile.displayName()}</p>
                <a href="/account/password"
                   class="block rounded-lg px-3 py-2 text-sm text-slate-700 hover:bg-slate-100">Change password</a>
                @if(csrfToken != null)
                    <form method="post" action="/logout">
                        <input type="hidden" name="${csrfToken.getParameterName()}" value="${csrfToken.getToken()}">
                        <button type="submit"
                                class="w-full rounded-lg px-3 py-2 text-left text-sm text-slate-700 hover:bg-slate-100">
                            Log out
                        </button>
                    </form>
                @endif
            </div>
        </details>
        <a href="/applications/new" class="inline-flex items-center rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2">
            <span class="mr-2 text-lg leading-none">+</span> New application
        </a>
    </div>
</header>
