package com.lavelahotel.poolbooking.util

object ValidationUtils {
    
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun isValidPhone(phone: String): Boolean {
        // Vietnamese phone format: 10-11 digits, may start with 0 or +84
        val cleaned = phone.replace("+84", "0").replace(" ", "").replace("-", "")
        return cleaned.matches(Regex("^0[0-9]{9,10}$"))
    }
    
    fun isValidPassword(password: String): Boolean {
        // At least 8 characters, contains uppercase, lowercase, and number
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() }
    }
    
    fun isValidOtp(otp: String): Boolean {
        return otp.length == 6 && otp.all { it.isDigit() }
    }
}
