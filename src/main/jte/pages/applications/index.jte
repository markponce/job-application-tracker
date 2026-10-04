@import com.example.job_application_tracker.jobapplication.model.ApplicationStatus
@import com.example.job_application_tracker.jobapplication.model.ExperienceLevel
@import com.example.job_application_tracker.jobapplication.model.JobApplication
@import com.example.job_application_tracker.jobapplication.model.SalaryPeriod
@import com.example.job_application_tracker.jobapplication.model.WorkSetup
@import com.example.job_application_tracker.jobapplication.dto.PageLink
@param java.util.List<JobApplication> applications
@param String query
@param ApplicationStatus selectedStatus
@param WorkSetup selectedWorkSetup
@param ExperienceLevel selectedExperienceLevel
@param SalaryPeriod selectedSalaryPeriod
@param java.math.BigDecimal minSalary
@param java.math.BigDecimal maxSalary
@param long totalCount
@param int startItem
@param long endItem
@param int currentPage
@param int totalPages
@param int pageSize
@param java.util.List<Integer> pageSizes
@param ApplicationStatus[] statuses
@param WorkSetup[] workSetups
@param ExperienceLevel[] experienceLevels
@param SalaryPeriod[] salaryPeriods
@param String previousPageUrl
@param String nextPageUrl
@param java.util.List<PageLink> pageLinks
@param String successMessage
<!doctype html>
<html lang="en">
@template.layouts.head(pageTitle = "Applications")
<body class="min-h-screen bg-slate-50 text-slate-900 antialiased">
<div class="min-h-screen lg:flex">
    @template.layouts.sidebar(activePage = "applications")
    <div class="min-w-0 flex-1">
        @template.layouts.topbar(pageTitle = "Applications")
        <main class="mx-auto w-full max-w-7xl p-5 sm:p-8">
@if(successMessage != null)
    <div class="mb-6 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">${successMessage}</div>
@endif
<section class="mb-6 grid gap-4 sm:grid-cols-3">
    <div class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
        <p class="text-sm font-medium text-slate-500">Total matching applications</p>
        <p class="mt-2 text-3xl font-semibold tracking-tight">${totalCount}</p>
    </div>
    <div class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
        <p class="text-sm font-medium text-slate-500">Showing</p>
        <p class="mt-2 text-3xl font-semibold tracking-tight">${applications.size()}</p>
    </div>
    <div class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
        <p class="text-sm font-medium text-slate-500">Next step</p>
        <a href="/applications/new" class="mt-2 inline-block text-sm font-semibold text-indigo-600 hover:text-indigo-700">Add a job to track <span aria-hidden="true">→</span></a>
    </div>
</section>
<section class="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
    <div class="border-b border-slate-200 p-5">
        <div class="mb-4">
            <h2 class="font-semibold">Your job applications</h2>
            <p class="mt-1 text-sm text-slate-500">Keep every opportunity moving forward.</p>
        </div>
        <form action="/applications" method="get" class="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            <div class="lg:col-span-2">
                <label for="q" class="mb-1 block text-xs font-medium text-slate-600">Search applications</label>
                <input id="q" name="q" type="search" value="${query}" placeholder="Company, role, URL, address, notes..." class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none placeholder:text-slate-400 focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
            </div>
            <div>
                <label for="status" class="mb-1 block text-xs font-medium text-slate-600">Status</label>
                <select id="status" name="status" class="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                    <option value="">All statuses</option>
                    @for(ApplicationStatus option : statuses)
                        @if(selectedStatus == option)
                            <option value="${option}" selected>${option}</option>
                        @else
                            <option value="${option}">${option}</option>
                        @endif
                    @endfor
                </select>
            </div>
            <div>
                <label for="workSetup" class="mb-1 block text-xs font-medium text-slate-600">Work setup</label>
                <select id="workSetup" name="workSetup" class="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                    <option value="">All work setups</option>
                    @for(WorkSetup option : workSetups)
                        @if(selectedWorkSetup == option)
                            <option value="${option}" selected>${option}</option>
                        @else
                            <option value="${option}">${option}</option>
                        @endif
                    @endfor
                </select>
            </div>
            <div>
                <label for="experienceLevel" class="mb-1 block text-xs font-medium text-slate-600">Experience level</label>
                <select id="experienceLevel" name="experienceLevel" class="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                    <option value="">All experience levels</option>
                    @for(ExperienceLevel option : experienceLevels)
                        @if(selectedExperienceLevel == option)
                            <option value="${option}" selected>${option}</option>
                        @else
                            <option value="${option}">${option}</option>
                        @endif
                    @endfor
                </select>
            </div>
            <div>
                <label for="salaryPeriod" class="mb-1 block text-xs font-medium text-slate-600">Salary period</label>
                <select id="salaryPeriod" name="salaryPeriod" class="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                    <option value="">All salary periods</option>
                    @for(SalaryPeriod option : salaryPeriods)
                        @if(selectedSalaryPeriod == option)
                            <option value="${option}" selected>${option}</option>
                        @else
                            <option value="${option}">${option}</option>
                        @endif
                    @endfor
                </select>
            </div>
            <div>
                <label for="minSalary" class="mb-1 block text-xs font-medium text-slate-600">Minimum salary</label>
                <input id="minSalary" name="minSalary" type="number" min="0" step="0.01" value="${minSalary != null ? minSalary.toPlainString() : ""}" placeholder="No minimum" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
            </div>
            <div>
                <label for="maxSalary" class="mb-1 block text-xs font-medium text-slate-600">Maximum salary</label>
                <input id="maxSalary" name="maxSalary" type="number" min="0" step="0.01" value="${maxSalary != null ? maxSalary.toPlainString() : ""}" placeholder="No maximum" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
            </div>
            <div>
                <label for="size" class="mb-1 block text-xs font-medium text-slate-600">Results per page</label>
                <select id="size" name="size" class="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                    @for(Integer option : pageSizes)
                        @if(pageSize == option)
                            <option value="${option}" selected>${option}</option>
                        @else
                            <option value="${option}">${option}</option>
                        @endif
                    @endfor
                </select>
            </div>
            <div class="flex items-end gap-2">
                <button type="submit" class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700">Apply filters</button>
                <a href="/applications" class="rounded-lg border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50">Clear</a>
            </div>
        </form>
    </div>
    @if(applications.isEmpty())
        <div class="px-6 py-16 text-center">
            <div class="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-indigo-50 text-xl text-indigo-600">▤</div>
            <h3 class="mt-4 font-semibold">No applications found</h3>
            <p class="mx-auto mt-2 max-w-sm text-sm text-slate-500">Add your first opportunity, or adjust your search and filters.</p>
            <a href="/applications/new" class="mt-5 inline-flex rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700">Add application</a>
        </div>
    @else
        <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-slate-200">
                <thead class="bg-slate-50">
                <tr>
                    <th scope="col" class="px-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">Role / Company</th>
                    <th scope="col" class="px-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">Status</th>
                    <th scope="col" class="px-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">Work setup</th>
                    <th scope="col" class="px-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">Added</th>
                    <th scope="col" class="px-5 py-3"><span class="sr-only">Actions</span></th>
                </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                @for(JobApplication application : applications)
                    <tr class="hover:bg-slate-50/70">
                        <td class="whitespace-nowrap px-5 py-4">
                            <a href="/applications/${java.util.Objects.toString(application.getId(), "")}" class="font-semibold text-slate-900 hover:text-indigo-700">${application.getTitle()}</a>
                            <p class="mt-1 text-sm text-slate-500">${application.getCompanyName()}</p>
                        </td>
                        <td class="whitespace-nowrap px-5 py-4">
                            <span class="rounded-full bg-indigo-50 px-2.5 py-1 text-xs font-semibold text-indigo-700">${application.getStatus()}</span>
                        </td>
                        <td class="whitespace-nowrap px-5 py-4 text-sm text-slate-600">${application.getWorkSetup()}</td>
                        <td class="whitespace-nowrap px-5 py-4 text-sm text-slate-500">${application.getCreatedAt() != null ? application.getCreatedAt().toLocalDate().toString() : "—"}</td>
                        <td class="whitespace-nowrap px-5 py-4 text-right">
                            <a href="/applications/${java.util.Objects.toString(application.getId(), "")}/edit" class="text-sm font-semibold text-indigo-600 hover:text-indigo-800">Edit</a>
                        </td>
                    </tr>
                @endfor
                </tbody>
            </table>
        </div>
    @endif
    <div class="flex flex-col gap-3 border-t border-slate-200 px-5 py-4 sm:flex-row sm:items-center sm:justify-between">
        <p class="text-sm text-slate-500">Showing ${startItem}–${endItem} of ${totalCount}</p>
        @if(totalPages > 1)
            <nav aria-label="Pagination" class="flex items-center justify-end gap-1">
                @if(previousPageUrl != null)
                    <a href="${previousPageUrl}" rel="prev" class="rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">Previous</a>
                @endif
                @for(PageLink link : pageLinks)
                    @if(link.current())
                        <span aria-current="page" class="rounded-lg bg-indigo-600 px-3 py-2 text-sm font-semibold text-white">${link.number()}</span>
                    @else
                        <a href="${link.url()}" class="rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">${link.number()}</a>
                    @endif
                @endfor
                @if(nextPageUrl != null)
                    <a href="${nextPageUrl}" rel="next" class="rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">Next</a>
                @endif
            </nav>
        @endif
    </div>
</section>
            @template.layouts.footer()
        </main>
    </div>
</div>
</body>
</html>
