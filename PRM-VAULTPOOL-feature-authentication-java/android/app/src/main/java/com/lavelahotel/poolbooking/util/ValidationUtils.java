package com.lavelahotel.poolbooking.util;

import android.util.Patterns;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        String cleaned = phone.replace("+84", "0")
                .replace(" ", "")
                .replace("-", "");
        return cleaned.matches("^0[0-9]{9,10}$");
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }

        return hasUpper && hasLower && hasDigit;
    }

    public static boolean isValidOtp(String otp) {
        if (otp == null || otp.length() != 6) {
            return false;
        }
        for (int i = 0; i < otp.length(); i++) {
            if (!Character.isDigit(otp.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}

