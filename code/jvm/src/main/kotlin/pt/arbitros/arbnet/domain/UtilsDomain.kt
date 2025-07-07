package pt.arbitros.arbnet.domain

import org.springframework.stereotype.Component
import java.time.LocalDate

val portugalPhoneNumberRegex = Regex("^(\\+351)?\\d{9}$") // Matches +351 followed by 9 digits

@Component
class UtilsDomain {
    // / Functions to validate things that are common to various domains

    // Valid only for portuguese phone numbers
    // +351 followed by 9 digits
    // Used for all phone number validation
    fun validPhoneNumber(phoneNumber: String): Boolean {
        val regex = portugalPhoneNumberRegex // Matches +351 followed by 9 digits
        return phoneNumber.isNotBlank() &&
            // phoneNumber.length == 13 &&
            regex.matches(phoneNumber)
    }

    // Used for all email validation
    fun validEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return email.length <= 100 && emailRegex.matches(email)
    }

    // Used for all address validation
    fun validAddress(address: String): Boolean {
        val regex = Regex("^[\\wÀ-ÿ0-9.,'ºª\\-/\\s]+$") // Inclui º e ª
        return address.isNotBlank() &&
            address.length in 5..255 &&
            regex.matches(address)
    }

    fun validName(name: String): Boolean {
        val regex = Regex("^[A-Za-zÀ-ÿ0-9 ]+$") // permite espaços
        return name.isNotBlank() &&
             name.length in 3..100 &&
             regex.matches(name)
    }

    fun dateIsAfterToday(date: String): Boolean =
        try {
            val parsedDate = LocalDate.parse(date)
            val today = LocalDate.now()
            parsedDate.isAfter(today)
        } catch (e: Exception) {
            false // Parsing failed → invalid format or date
        }

    fun validDate(date: String): Boolean =
        try {
            val parsedDate = LocalDate.parse(date)
            true
        } catch (e: Exception) {
            false // Parsing failed → invalid format or date
        }
}
