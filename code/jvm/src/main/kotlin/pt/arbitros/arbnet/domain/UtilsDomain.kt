package pt.arbitros.arbnet.domain

import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class UtilsDomain {

    /// Functions to validate things that are common to various domains

    // Valid only for portuguese phone numbers
    // +351 followed by 9 digits
    // Used for all phone number validation
    fun validPhoneNumber(phoneNumber: String): Boolean {
        val regex = Regex("^\\+351\\d{9}$") // Matches +351 followed by 9 digits
        return phoneNumber.isNotBlank() &&
                phoneNumber.length == 13 &&
                regex.matches(phoneNumber)
    }

    // Used for all email validation
    fun validEmail(email: String): Boolean {
        val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,10}$")
        return email.length <= 100 && emailRegex.matches(email)
    }

    // Used for all address validation
    fun validAddress(address: String): Boolean {
        val regex = Regex("^[\\wÀ-ÿ0-9.,'\\-/\\s]+$") // Allows letters, numbers, common punctuation
        return address.isNotBlank() &&
                address.length in 5..255 &&
                regex.matches(address)
    }

    // Used for all name validation, all names are required to have a max of 100 characters
    fun validName(name: String): Boolean {
        val regex = Regex("^[A-Za-zÀ-ÿ ]+$") // Allows letters (including accented) and spaces
        return name.isNotBlank() &&
                name.length in 3..100 &&
                // name.trim().contains(" ") &&
                regex.matches(name)
    }

    fun dateIsAfterToday(date: String): Boolean {
        return try {
            val parsedDate = LocalDate.parse(date)
            val today = LocalDate.now()
            parsedDate.isAfter(today)
        } catch (e: Exception) {
            false // Parsing failed → invalid format or date
        }
    }

    fun validDate(date: String): Boolean {
        return try {
            val parsedDate = LocalDate.parse(date)
            true
        } catch (e: Exception) {
            false // Parsing failed → invalid format or date
        }
    }

}