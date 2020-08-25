package com.ana.mybank;

import java.text.NumberFormat;
import java.util.Locale;

public class Constants {
    public static final String DEFAULT_PASSWORDS = "123456";
    public static final String[] ADMIN_MAILS = {"admin@mybank.rs"};

    public static String getMoneyFormat(double value) {
        Locale locale = new Locale("us", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        fmt.setMinimumFractionDigits(2);
        fmt.setMaximumFractionDigits(2);
        return fmt.format(value);
    }
}
