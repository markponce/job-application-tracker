@param String activePage
<aside class="w-full border-b border-slate-200 bg-white lg:min-h-screen lg:w-64 lg:border-b-0 lg:border-r">
    <div class="flex h-16 items-center gap-3 px-6">
        <span class="flex h-9 w-9 items-center justify-center rounded-xl bg-indigo-600 text-sm font-bold text-white">A</span>
        <span class="text-lg font-semibold tracking-tight">ApplyTrack</span>
    </div>
    <div class="px-4 pb-4 lg:pt-5">
        @template.partials.navigation(activePage = activePage)
    </div>
    <div class="hidden border-t border-slate-100 px-6 py-5 text-xs leading-5 text-slate-400 lg:block">
        Personal workspace<br>
        Account features coming later
    </div>
</aside>
