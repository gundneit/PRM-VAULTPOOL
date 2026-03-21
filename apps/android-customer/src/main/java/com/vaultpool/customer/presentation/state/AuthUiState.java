package com.vaultpool.customer.presentation.state;

/**
 * UI State classes for Authentication screen.
 * Represents all possible states the Auth screen can be in.
 */
public abstract class AuthUiState {

    private AuthUiState() {
    }

    // Initial idle state
    public static final class Idle extends AuthUiState {
        private static final Idle INSTANCE = new Idle();

        private Idle() {
        }

        public static Idle getInstance() {
            return INSTANCE;
        }
    }

    // Loading state
    public static final class Loading extends AuthUiState {
        private static final Loading INSTANCE = new Loading();

        private Loading() {
        }

        public static Loading getInstance() {
            return INSTANCE;
        }
    }

    // Login success state
    public static final class LoginSuccess extends AuthUiState {
        private final Object user;

        public LoginSuccess(Object user) {
            this.user = user;
        }

        public Object getUser() {
            return user;
        }
    }

    // Registration success state
    public static final class RegisterSuccess extends AuthUiState {
        private final String message;
        private final Object user;

        public RegisterSuccess(String message, Object user) {
            this.message = message;
            this.user = user;
        }

        public String getMessage() {
            return message;
        }

        public Object getUser() {
            return user;
        }
    }

    // Email not verified state
    public static final class EmailNotVerified extends AuthUiState {
        private final String message;
        private final Object user;

        public EmailNotVerified(String message, Object user) {
            this.message = message;
            this.user = user;
        }

        public String getMessage() {
            return message;
        }

        public Object getUser() {
            return user;
        }
    }

    // Email verified state
    public static final class EmailVerified extends AuthUiState {
        private final Object user;

        public EmailVerified(Object user) {
            this.user = user;
        }

        public Object getUser() {
            return user;
        }
    }

    // Verification email sent state
    public static final class EmailSent extends AuthUiState {
        private final String message;

        public EmailSent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    // Error state
    public static final class Error extends AuthUiState {
        private final String message;

        public Error(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
