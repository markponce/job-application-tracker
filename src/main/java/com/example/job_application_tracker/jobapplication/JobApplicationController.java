package com.example.job_application_tracker.jobapplication;

import com.example.job_application_tracker.jobapplication.dto.JobApplicationForm;
import com.example.job_application_tracker.jobapplication.dto.JobApplicationSearchCriteria;
import com.example.job_application_tracker.jobapplication.dto.PageLink;
import com.example.job_application_tracker.jobapplication.model.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.security.Principal;

@Controller
public class JobApplicationController {

	private static final int DEFAULT_PAGE_SIZE = 10;
	private static final List<Integer> PAGE_SIZES = List.of(10, 25, 50, 100);
	private static final Sort APPLICATION_SORT = Sort.by(
			Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

	private final JobApplicationService service;

	public JobApplicationController(JobApplicationService service) {
		this.service = service;
	}

	@GetMapping("/")
	public String home() {
		return "redirect:/applications";
	}

	@GetMapping("/applications")
	public String index(
			Principal principal,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) ApplicationStatus status,
			@RequestParam(required = false) WorkSetup workSetup,
			@RequestParam(required = false) ExperienceLevel experienceLevel,
			@RequestParam(required = false) SalaryPeriod salaryPeriod,
			@RequestParam(required = false) BigDecimal minSalary,
			@RequestParam(required = false) BigDecimal maxSalary,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size,
			Model model) {
		int pageSize = PAGE_SIZES.contains(size) ? size : DEFAULT_PAGE_SIZE;
		int pageNumber = Math.max(page, 1);
		JobApplicationSearchCriteria criteria = new JobApplicationSearchCriteria(
				q, status, workSetup, experienceLevel, salaryPeriod, minSalary, maxSalary);
		Page<JobApplication> results = service.search(principal.getName(), criteria,
				PageRequest.of(pageNumber - 1, pageSize, APPLICATION_SORT));
		if (results.getTotalPages() > 0 && pageNumber > results.getTotalPages()) {
			pageNumber = results.getTotalPages();
			results = service.search(principal.getName(), criteria,
					PageRequest.of(pageNumber - 1, pageSize, APPLICATION_SORT));
		} else if (results.getTotalPages() == 0) {
			pageNumber = 1;
		}

		List<PageLink> pageLinks = pageLinks(criteria, pageNumber, pageSize, results.getTotalPages());
		model.addAttribute("applications", results.getContent());
		model.addAttribute("statuses", ApplicationStatus.values());
		model.addAttribute("workSetups", WorkSetup.values());
		model.addAttribute("experienceLevels", ExperienceLevel.values());
		model.addAttribute("salaryPeriods", SalaryPeriod.values());
		model.addAttribute("query", q == null ? "" : q);
		model.addAttribute("selectedStatus", status);
		model.addAttribute("selectedWorkSetup", workSetup);
		model.addAttribute("selectedExperienceLevel", experienceLevel);
		model.addAttribute("selectedSalaryPeriod", salaryPeriod);
		model.addAttribute("minSalary", minSalary);
		model.addAttribute("maxSalary", maxSalary);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("pageSizes", PAGE_SIZES);
		model.addAttribute("currentPage", pageNumber);
		model.addAttribute("totalPages", results.getTotalPages());
		model.addAttribute("totalCount", results.getTotalElements());
		model.addAttribute("startItem", results.getNumberOfElements() == 0
				? 0 : (pageNumber - 1) * pageSize + 1);
		model.addAttribute("endItem", (long) (pageNumber - 1) * pageSize + results.getNumberOfElements());
		model.addAttribute("previousPageUrl", pageNumber > 1
				? pageUrl(criteria, pageNumber - 1, pageSize) : null);
		model.addAttribute("nextPageUrl", pageNumber < results.getTotalPages()
				? pageUrl(criteria, pageNumber + 1, pageSize) : null);
		model.addAttribute("pageLinks", pageLinks);
		return "pages/applications/index";
	}

	@GetMapping("/applications/new")
	public String newApplication(Model model) {
		prepareForm(model, JobApplicationForm.empty(), "Create application", "Save application", null, List.of());
		return "pages/applications/form";
	}

	@PostMapping("/applications")
	public String create(
			Principal principal,
            @Valid @ModelAttribute("form") JobApplicationForm form,
			BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
		if (bindingResult.hasErrors()) {
			prepareForm(model, form, "Create application", "Save application", null, errors(bindingResult));
			return "pages/applications/form";
		}

		service.create(principal.getName(), form.toEntity());
		redirectAttributes.addFlashAttribute("successMessage", "Application created.");
		return "redirect:/applications";
	}

	@GetMapping("/applications/{id}")
	public String show(Principal principal, @PathVariable UUID id, Model model) {
		model.addAttribute("application", service.getById(principal.getName(), id));
		return "pages/applications/show";
	}

	@GetMapping("/applications/{id}/edit")
	public String edit(Principal principal, @PathVariable UUID id, Model model) {
		prepareForm(model, JobApplicationForm.from(service.getById(principal.getName(), id)),
				"Edit application", "Save changes", id, List.of());
		return "pages/applications/form";
	}

	@PostMapping("/applications/{id}")
	public String update(Principal principal, @PathVariable UUID id, @Valid @ModelAttribute("form") JobApplicationForm form,
			BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			prepareForm(model, form, "Edit application", "Save changes", id, errors(bindingResult));
			return "pages/applications/form";
		}

		service.update(principal.getName(), id, form.toEntity());
		redirectAttributes.addFlashAttribute("successMessage", "Application updated.");
		return "redirect:/applications/" + id;
	}

	@PostMapping("/applications/{id}/delete")
	public String delete(Principal principal, @PathVariable UUID id, RedirectAttributes redirectAttributes) {
		service.delete(principal.getName(), id);
		redirectAttributes.addFlashAttribute("successMessage", "Application deleted.");
		return "redirect:/applications";
	}

	private void prepareForm(Model model, JobApplicationForm form, String pageTitle,
                             String submitLabel, UUID applicationId, List<String> formErrors) {
		model.addAttribute("form", form);
		model.addAttribute("pageTitle", pageTitle);
		model.addAttribute("submitLabel", submitLabel);
		model.addAttribute("applicationId", applicationId);
		model.addAttribute("statuses", ApplicationStatus.values());
		model.addAttribute("workSetups", WorkSetup.values());
		model.addAttribute("experienceLevels", ExperienceLevel.values());
		model.addAttribute("salaryPeriods", SalaryPeriod.values());
		model.addAttribute("currencies", CurrencyCatalog.getCurrencies());
		model.addAttribute("formErrors", formErrors);
	}

	private List<String> errors(BindingResult bindingResult) {
		return bindingResult.getAllErrors().stream()
				.map(error -> error.getDefaultMessage())
				.toList();
	}

	private List<PageLink> pageLinks(
			JobApplicationSearchCriteria criteria, int currentPage, int pageSize, int totalPages) {
		if (totalPages == 0) {
			return List.of();
		}
		int firstPage = Math.max(1, currentPage - 2);
		int lastPage = Math.min(totalPages, firstPage + 4);
		firstPage = Math.max(1, lastPage - 4);
		List<PageLink> links = new ArrayList<>();
		for (int page = firstPage; page <= lastPage; page++) {
			links.add(new PageLink(page, pageUrl(criteria, page, pageSize), page == currentPage));
		}
		return links;
	}

	private String pageUrl(JobApplicationSearchCriteria criteria, int page, int size) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/applications")
				.queryParam("page", page)
				.queryParam("size", size);
		addQueryParam(builder, "q", criteria.query());
		addQueryParam(builder, "status", criteria.status());
		addQueryParam(builder, "workSetup", criteria.workSetup());
		addQueryParam(builder, "experienceLevel", criteria.experienceLevel());
		addQueryParam(builder, "salaryPeriod", criteria.salaryPeriod());
		addQueryParam(builder, "minSalary", criteria.minSalary());
		addQueryParam(builder, "maxSalary", criteria.maxSalary());
		return builder.build().encode().toUriString();
	}

	private void addQueryParam(UriComponentsBuilder builder, String name, Object value) {
		if (value != null && (!(value instanceof String text) || !text.isBlank())) {
			builder.queryParam(name, value);
		}
	}
}
