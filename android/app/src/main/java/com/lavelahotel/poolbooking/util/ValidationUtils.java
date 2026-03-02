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

    /**
     * Converts a Vietnamese phone number to E.164 format required by Firebase Phone Auth.
     * e.g. "0763769393" → "+84763769393"
     *      "+84763769393" → "+84763769393" (already correct)
     */
    public static String formatToE164Vietnam(String phone) {
        if (phone == null) return "";
        String cleaned = phone.replace(" ", "").replace("-", "");
        if (cleaned.startsWith("+84")) {
            return cleaned;
        } else if (cleaned.startsWith("0") && cleaned.length() >= 10) {
            return "+84" + cleaned.substring(1);
        }
        // Return as-is if already in some other international format
        return cleaned;
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

