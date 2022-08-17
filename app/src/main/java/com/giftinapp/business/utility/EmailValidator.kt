package com.giftinapp.business.utility

class EmailValidator {

    /** validate email **/
    fun validateEmail(email:String): Boolean {

        val emailPattern = Regex("""^(\w+\.?(\w+)?@([a-zA-Z_]+\.){1,2}[a-zA-Z]{2,6})${'$'}""")
        return email.matches(emailPattern)
    }

    fun validateNames(firstName:String,lastName:String):Boolean{
        return firstName.isEmpty() && lastName.isEmpty()
    }
}