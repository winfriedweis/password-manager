package com.winfriedweis.nup.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ValidationService {
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern USERNAME_PATTERN =
        Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        return UPPERCASE_PATTERN.matcher(password).find() &&
               LOWERCASE_PATTERN.matcher(password).find() &&
               DIGIT_PATTERN.matcher(password).find() &&
               SPECIAL_CHAR_PATTERN.matcher(password).find();
    }

    public static List<String> getPasswordErrors(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("Passwort darf nicht leer sein");
            return errors;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            errors.add("Mindestens " + MIN_PASSWORD_LENGTH + " Zeichen");
        }
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            errors.add("Mindestens ein Großbuchstabe");
        }
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            errors.add("Mindestens ein Kleinbuchstabe");
        }
        if (!DIGIT_PATTERN.matcher(password).find()) {
            errors.add("Mindestens eine Zahl");
        }
        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            errors.add("Mindestens ein Sonderzeichen (!@#$%^&*...)");
        }

        return errors;
    }
}
