package com.example.job_application_tracker.jobapplication;

import java.util.Currency;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class CurrencyCatalog {

	private static final List<Currency> CURRENCIES = Currency.getAvailableCurrencies().stream()
			.sorted(Comparator.comparing(Currency::getCurrencyCode))
			.toList();

	private CurrencyCatalog() {
	}

	public static List<Currency> getCurrencies() {
		return CURRENCIES;
	}

	public static boolean contains(String currencyCode) {
		return CURRENCIES.stream()
				.anyMatch(currency -> currency.getCurrencyCode().equals(currencyCode));
	}

	public static String getDisplayName(Currency currency) {
		return currency.getDisplayName(Locale.ENGLISH);
	}
}
