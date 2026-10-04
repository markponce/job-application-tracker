@import com.example.job_application_tracker.jobapplication.model.ApplicationStatus
@param String activePage
<nav class="flex gap-2 overflow-x-auto lg:flex-col">
    <a href="/applications" class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium ${activePage.equals("applications") ? "bg-indigo-50 text-indigo-700" : "text-slate-600 hover:bg-slate-50 hover:text-slate-900"}">
        <span class="w-5 text-center">▤</span> Applications
    </a>
<%--    <a href="/applications?status=SAVED" class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-50 hover:text-slate-900">--%>
<%--        <span class="w-5 text-center">◷</span> Saved--%>
<%--    </a>--%>
<%--    <a href="/applications?status=INTERVIEW" class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-50 hover:text-slate-900">--%>
<%--        <span class="w-5 text-center">✦</span> Interviews--%>
<%--    </a>--%>
</nav>
