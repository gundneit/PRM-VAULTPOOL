package com.lavelahotel.poolbooking.ui.auth;

import com.google.firebase.auth.FirebaseUser;

public abstract class AuthUiState {

    public static final class Idle extends AuthUiState {
        public static final Idle INSTANCE = new Idle();

        private Idle() {
        }
    }

    public static final class Loading extends AuthUiState {
        public static final Loading INSTANCE = new Loading();

        private Loading() {
        }
    }

    public static final class RegisterSuccess extends AuthUiState {
        public final String message;
        public final FirebaseUser user;

        public RegisterSuccess(String message, FirebaseUser user) {
            this.message = message;
            this.user = user;
        }
    }

    public static final class LoginSuccess extends AuthUiState {
        public final FirebaseUser user;

        public LoginSuccess(FirebaseUser user) {
            this.user = user;
        }
    }

    public static final class EmailNotVerified extends AuthUiState {
        public final String message;
        public final FirebaseUser user;

        public EmailNotVerified(String message, FirebaseUser user) {
            this.message = message;
            this.user = user;
        }
    }

    public static final class EmailVerified extends AuthUiState {
        public final FirebaseUser user;

        public EmailVerified(FirebaseUser user) {
            this.user = user;
        }
    }

    public static final class EmailSent extends AuthUiState {
        public final String message;

        public EmailSent(String message) {
            this.message = message;
        }
    }

    public static final class Error extends AuthUiState {
        public final String message;

        public Error(String message) {
            this.message = message;
        }
    }
}

