package com.winfriedweis.nup.service;

import com.winfriedweis.nup.dao.UserDAO;
import com.winfriedweis.nup.dao.UserDAOImpl;
import com.winfriedweis.nup.model.User;
import com.winfriedweis.nup.util.PasswordUtil;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_SECONDS = 300; // 5 Minuten

    private final UserDAO userDAO;
    private final Map<String, LoginAttemptInfo> loginAttempts = new ConcurrentHashMap<>();

    public AuthService() {
        this.userDAO = new UserDAOImpl();
    }

    public record LoginResult(boolean success, User user, String errorMessage) {
        public static LoginResult success(User user) {
            return new LoginResult(true, user, null);
        }
        public static LoginResult failure(String message) {
            return new LoginResult(false, null, message);
        }
    }

    public LoginResult login(String identifier, String password) {
        if (identifier == null || identifier.isBlank()) {
            return LoginResult.failure("Benutzername/Email darf nicht leer sein");
        }

        String normalizedIdentifier = identifier.toLowerCase().trim();

        if (isLockedOut(normalizedIdentifier)) {
            long remainingSeconds = getRemainingLockoutSeconds(normalizedIdentifier);
            return LoginResult.failure("Account gesperrt. Versuche es in " +
                formatDuration(remainingSeconds) + " erneut.");
        }

        Optional<User> userOpt = userDAO.findByUsernameOrEmail(identifier);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                resetLoginAttempts(normalizedIdentifier);
                return LoginResult.success(user);
            }
        }

        recordFailedAttempt(normalizedIdentifier);
        int remainingAttempts = MAX_LOGIN_ATTEMPTS - getFailedAttempts(normalizedIdentifier);

        if (remainingAttempts <= 0) {
            return LoginResult.failure("Account gesperrt für " +
                formatDuration(LOCKOUT_DURATION_SECONDS) + " wegen zu vieler Fehlversuche.");
        }

        return LoginResult.failure("Ungültige Anmeldedaten. Noch " + remainingAttempts + " Versuche.");
    }

    public boolean register(String username, String email, String password,
                           StringBuilder errorMessage) {
        if (!ValidationService.isValidUsername(username)) {
            errorMessage.append("Username muss 3-20 Zeichen lang sein (nur Buchstaben, Zahlen, _)");
            return false;
        }

        if (!ValidationService.isValidEmail(email)) {
            errorMessage.append("Ungültige Email-Adresse");
            return false;
        }

        List<String> passwordErrors = ValidationService.getPasswordErrors(password);
        if (!passwordErrors.isEmpty()) {
            errorMessage.append("Passwort-Anforderungen:\n");
            for (String error : passwordErrors) {
                errorMessage.append("• ").append(error).append("\n");
            }
            return false;
        }

        if (userDAO.findByUsername(username).isPresent()) {
            errorMessage.append("Username bereits vergeben");
            return false;
        }

        if (userDAO.findByEmail(email).isPresent()) {
            errorMessage.append("Email bereits registriert");
            return false;
        }

        String passwordHash = PasswordUtil.hashPassword(password);
        User newUser = new User(username, email, passwordHash);

        return userDAO.createUser(newUser);
    }

    private boolean isLockedOut(String identifier) {
        LoginAttemptInfo info = loginAttempts.get(identifier);
        if (info == null) {
            return false;
        }

        if (info.attempts >= MAX_LOGIN_ATTEMPTS) {
            long elapsedSeconds = Instant.now().getEpochSecond() - info.lastAttemptTime;
            if (elapsedSeconds < LOCKOUT_DURATION_SECONDS) {
                return true;
            }
            loginAttempts.remove(identifier);
        }
        return false;
    }

    private long getRemainingLockoutSeconds(String identifier) {
        LoginAttemptInfo info = loginAttempts.get(identifier);
        if (info == null) {
            return 0;
        }
        long elapsedSeconds = Instant.now().getEpochSecond() - info.lastAttemptTime;
        return Math.max(0, LOCKOUT_DURATION_SECONDS - elapsedSeconds);
    }

    private void recordFailedAttempt(String identifier) {
        loginAttempts.compute(identifier, (key, info) -> {
            if (info == null) {
                return new LoginAttemptInfo(1, Instant.now().getEpochSecond());
            }
            return new LoginAttemptInfo(info.attempts + 1, Instant.now().getEpochSecond());
        });
    }

    private int getFailedAttempts(String identifier) {
        LoginAttemptInfo info = loginAttempts.get(identifier);
        return info != null ? info.attempts : 0;
    }

    private void resetLoginAttempts(String identifier) {
        loginAttempts.remove(identifier);
    }

    private String formatDuration(long seconds) {
        if (seconds >= 60) {
            long minutes = seconds / 60;
            return minutes + " Minute" + (minutes > 1 ? "n" : "");
        }
        return seconds + " Sekunden";
    }

    private record LoginAttemptInfo(int attempts, long lastAttemptTime) {}
}
