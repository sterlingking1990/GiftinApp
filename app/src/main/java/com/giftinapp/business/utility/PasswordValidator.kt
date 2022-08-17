package com.giftinapp.business.utility

class PasswordValidator {

        public companion object Validator {
            fun validPassword(password: String): Boolean {
                return password.length>6
            }
        }
}