package com.giftinapp.merchant

class PhoneNumberValidator {

        fun validatePhoneNumber(phoneNumber: String): Boolean {
            return when {
                phoneNumber.isEmpty() -> false
                else -> true
            }
        }
    }