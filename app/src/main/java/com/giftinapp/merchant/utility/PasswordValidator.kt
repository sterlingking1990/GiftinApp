package com.giftinapp.merchant.utility

class PasswordValidator {

        public companion object Validator {
            fun validPassword(password: String): Boolean {
                return password.isNotEmpty() && password.length>6
            }
        }
}