package com.example.job_application_tracker.account;

import org.springframework.security.core.Authentication;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public record AccountProfile(
		String displayName,
		String initials,
		String avatarColorClass) {

	private static final List<String> AVATAR_COLORS = List.of(
			"bg-rose-100 text-rose-700",
			"bg-amber-100 text-amber-700",
			"bg-emerald-100 text-emerald-700",
			"bg-cyan-100 text-cyan-700",
			"bg-indigo-100 text-indigo-700",
			"bg-violet-100 text-violet-700",
			"bg-pink-100 text-pink-700",
			"bg-teal-100 text-teal-700");

	public static AccountProfile from(UserAccount user) {
		String displayName = user.getFirstName() + " " + user.getLastName();
		return new AccountProfile(
				displayName,
				initials(user.getFirstName(), user.getLastName()),
				user.getId() == null ? colorFor(displayName) : colorFor(user.getId()));
	}

	public static AccountProfile fromUsername(String username) {
		String name = username == null || username.isBlank() ? "Account User" : username;
		String[] parts = name.split("[^\\p{L}\\p{N}]+");
		String first = parts.length > 0 ? parts[0] : name;
		String last = parts.length > 1 ? parts[1] : "";
		return new AccountProfile(name, initials(first, last),
				AVATAR_COLORS.get(Math.floorMod(name.hashCode(), AVATAR_COLORS.size())));
	}

	public static AccountProfile fromPrincipal(Principal principal) {
		if (principal instanceof Authentication authentication) {
			if (authentication.getPrincipal() instanceof UserAccount userAccount) {
				return from(userAccount);
			}
			return fromUsername(authentication.getName());
		}
		return fromUsername(principal == null ? "Account User" : principal.getName());
	}

	private static String initials(String firstName, String lastName) {
		return firstCodePoint(firstName) + firstCodePoint(lastName);
	}

	private static String firstCodePoint(String name) {
		if (name == null || name.isBlank()) {
			return "";
		}
		int codePoint = name.strip().codePointAt(0);
		return new String(Character.toChars(codePoint)).toUpperCase(Locale.ROOT);
	}

	private static String colorFor(UUID userId) {
		return AVATAR_COLORS.get(Math.floorMod(userId.hashCode(), AVATAR_COLORS.size()));
	}

	private static String colorFor(String stableValue) {
		return AVATAR_COLORS.get(Math.floorMod(stableValue.hashCode(), AVATAR_COLORS.size()));
	}
}
