@import com.example.job_application_tracker.jobapplication.*
@import com.example.job_application_tracker.jobapplication.dto.JobApplicationForm
@import com.example.job_application_tracker.jobapplication.model.WorkSetup
@import com.example.job_application_tracker.jobapplication.model.ExperienceLevel
@import com.example.job_application_tracker.jobapplication.model.SalaryPeriod
@import com.example.job_application_tracker.jobapplication.model.ApplicationStatus
@import java.util.Currency
@param com.example.job_application_tracker.jobapplication.dto.JobApplicationForm form
@param String pageTitle
@param String submitLabel
@param java.util.UUID applicationId
@param com.example.job_application_tracker.jobapplication.model.ApplicationStatus[] statuses
@param com.example.job_application_tracker.jobapplication.model.WorkSetup[] workSetups
@param com.example.job_application_tracker.jobapplication.model.ExperienceLevel[] experienceLevels
@param com.example.job_application_tracker.jobapplication.model.SalaryPeriod[] salaryPeriods
@param java.util.List<Currency> currencies
@param java.util.List<String> formErrors
<!doctype html>
<html lang="en">
@template.layouts.head(pageTitle = pageTitle)
<body class="min-h-screen bg-slate-50 text-slate-900 antialiased">
<div class="min-h-screen lg:flex">
    @template.layouts.sidebar(activePage = "applications")
    <div class="min-w-0 flex-1">
        @template.layouts.topbar(pageTitle = pageTitle)
        <main class="mx-auto w-full max-w-7xl p-5 sm:p-8">
<div class="mx-auto max-w-4xl">
    <div class="mb-6">
        <a href="/applications" class="text-sm font-medium text-slate-500 hover:text-slate-800">← Back to applications</a>
        <p class="mt-3 text-sm text-slate-500">Add the details you want to remember. You can update them anytime.</p>
    </div>
    @if(!formErrors.isEmpty())
        <div role="alert" class="mb-5 rounded-lg border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800">
            <p class="font-semibold">Please check the following fields:</p>
            <ul class="mt-2 list-inside list-disc">
                @for(String error : formErrors)
                    <li>${error}</li>
                @endfor
            </ul>
        </div>
    @endif
    <form method="post" action="${applicationId == null ? "/applications" : "/applications/" + applicationId}" class="space-y-6">
        <section class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-7">
            <h2 class="font-semibold">Role details</h2>
            <p class="mt-1 text-sm text-slate-500">The basics about this opportunity.</p>
            <div class="mt-6 grid gap-5 sm:grid-cols-2">
                <div>
                    <label for="companyName" class="mb-1.5 block text-sm font-medium text-slate-700">Company name <span class="text-rose-500">*</span></label>
                    <input id="companyName" name="companyName" required maxlength="255" value="${form.companyName()}" class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                </div>
                <div>
                    <label for="title" class="mb-1.5 block text-sm font-medium text-slate-700">Job title <span class="text-rose-500">*</span></label>
                    <input id="title" name="title" required maxlength="255" value="${form.title()}" class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                </div>
                <div class="sm:col-span-2">
                    <label for="url" class="mb-1.5 block text-sm font-medium text-slate-700">Job posting URL <span class="text-rose-500">*</span></label>
                    <input id="url" name="url" type="url" required value="${form.url()}" placeholder="https://company.com/careers/role" class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                </div>
                <div class="sm:col-span-2">
                    <label for="companyAddress" class="mb-1.5 block text-sm font-medium text-slate-700">Company address</label>
                    <input id="companyAddress" name="companyAddress" value="${form.companyAddress()}" class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                </div>
                <div class="sm:col-span-2">
                    <label for="status" class="mb-1.5 block text-sm font-medium text-slate-700">Application status</label>
                    <select id="status" name="status" class="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                        @for(ApplicationStatus option : statuses)
                            @if(form.status() == option)
                                <option value="${option}" selected>${option}</option>
                            @else
                                <option value="${option}">${option}</option>
                            @endif
                        @endfor
                    </select>
                </div>
                @if(form.status() == ApplicationStatus.SAVED)
                    <div id="appliedAtField" hidden class="sm:col-span-2">
                        <label for="appliedAt" class="mb-1.5 block text-sm font-medium text-slate-700">Applied at <span class="text-rose-500">*</span></label>
                        <input id="appliedAt" name="appliedAt" type="datetime-local" value="${form.appliedAt() != null ? form.appliedAt().toString() : ""}" class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                    </div>
                @else
                    <div id="appliedAtField" class="sm:col-span-2">
                        <label for="appliedAt" class="mb-1.5 block text-sm font-medium text-slate-700">Applied at <span class="text-rose-500">*</span></label>
                        <input id="appliedAt" name="appliedAt" type="datetime-local" required value="${form.appliedAt() != null ? form.appliedAt().toString() : ""}" class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                    </div>
                @endif
                <div>
                    <label for="workSetup" class="mb-1.5 block text-sm font-medium text-slate-700">Work setup</label>
                    <select id="workSetup" name="workSetup" class="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                        @for(WorkSetup option : workSetups)
                            @if(form.workSetup() == option)
                                <option value="${option}" selected>${option}</option>
                            @else
                                <option value="${option}">${option}</option>
                            @endif
                        @endfor
                    </select>
                </div>
                <div class="sm:col-span-2">
                    <label for="experienceLevel" class="mb-1.5 block text-sm font-medium text-slate-700">Experience level</label>
                    <select id="experienceLevel" name="experienceLevel" class="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                        <option value="">Not specified</option>
                        @for(ExperienceLevel option : experienceLevels)
                            @if(form.experienceLevel() == option)
                                <option value="${option}" selected>${option}</option>
                            @else
                                <option value="${option}">${option}</option>
                            @endif
                        @endfor
                    </select>
                </div>
            </div>
        </section>
        <section class="rounded-xl border border-slate-200 bg-white p-5 shadow-sm sm:p-7">
            <h2 class="font-semibold">Compensation & notes</h2>
            <p class="mt-1 text-sm text-slate-500">Optional details to help you compare opportunities.</p>
            <div class="mt-6 grid gap-5 sm:grid-cols-2">
                <div>
                    <label for="salaryMin" class="mb-1.5 block text-sm font-medium text-slate-700">Minimum salary</label>
                    <input id="salaryMin" name="salaryMin" type="number" min="0" step="0.01" value="${form.salaryMin()}" class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                </div>
                <div>
                    <label for="salaryMax" class="mb-1.5 block text-sm font-medium text-slate-700">Maximum salary</label>
                    <input id="salaryMax" name="salaryMax" type="number" min="0" step="0.01" value="${form.salaryMax()}" class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                </div>
                <div>
                    <label for="salaryPeriod" class="mb-1.5 block text-sm font-medium text-slate-700">Salary period</label>
                    <select id="salaryPeriod" name="salaryPeriod" class="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                        <option value="">Not specified</option>
                        @for(SalaryPeriod option : salaryPeriods)
                            @if(form.salaryPeriod() == option)
                                <option value="${option}" selected>${option}</option>
                            @else
                                <option value="${option}">${option}</option>
                            @endif
                        @endfor
                    </select>
                </div>
                <div>
                    <label for="salaryCurrency" class="mb-1.5 block text-sm font-medium text-slate-700">Currency</label>
                    <select id="salaryCurrency" name="salaryCurrency" class="w-full rounded-lg border border-slate-300 bg-white px-3 py-2.5 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">
                        <option value="">Not specified</option>
                        @for(Currency currency : currencies)
                            @if(currency.getCurrencyCode().equals(form.salaryCurrency()))
                                <option value="${currency.getCurrencyCode()}" selected>${currency.getCurrencyCode()} — ${com.example.job_application_tracker.jobapplication.CurrencyCatalog.getDisplayName(currency)}</option>
                            @else
                                <option value="${currency.getCurrencyCode()}">${currency.getCurrencyCode()} — ${com.example.job_application_tracker.jobapplication.CurrencyCatalog.getDisplayName(currency)}</option>
                            @endif
                        @endfor
                    </select>
                </div>
                <div>
                    <label for="content" class="mb-1.5 block text-sm font-medium text-slate-700">Job description</label>
                    <textarea id="content" name="content" rows="3" class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">${form.content()}</textarea>
                </div>
                <div class="sm:col-span-2">
                    <label for="notes" class="mb-1.5 block text-sm font-medium text-slate-700">Notes</label>
                    <textarea id="notes" name="notes" rows="4" placeholder="People to follow up with, interview notes, reminders..." class="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm outline-none focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100">${form.notes()}</textarea>
                </div>
            </div>
        </section>
        <div class="flex items-center justify-end gap-3">
            <a href="${applicationId == null ? "/applications" : "/applications/" + applicationId}" class="rounded-lg px-4 py-2.5 text-sm font-semibold text-slate-600 hover:bg-slate-100">Cancel</a>
            <button type="submit" class="rounded-lg bg-indigo-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2">${submitLabel}</button>
        </div>
    </form>
</div>
<script>
    const statusField = document.getElementById("status");
    const appliedAtField = document.getElementById("appliedAtField");
    const appliedAtInput = document.getElementById("appliedAt");
    const updateAppliedAtRequirement = () => {
        const required = statusField.value !== "SAVED";
        appliedAtField.hidden = !required;
        appliedAtInput.required = required;
    };
    statusField.addEventListener("change", updateAppliedAtRequirement);
    updateAppliedAtRequirement();
</script>
            @template.layouts.footer()
        </main>
    </div>
</div>
</body>
</html>
