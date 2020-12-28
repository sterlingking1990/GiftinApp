package com.giftinapp.merchant

class PasswordValidator {

        public companion object Validator {
            fun validPassword(password: String): Boolean {
                return password.isNotEmpty() && password.length>6
            }
        }
}