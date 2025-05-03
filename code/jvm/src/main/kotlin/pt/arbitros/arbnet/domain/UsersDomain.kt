package pt.arbitros.arbnet.domain

import org.springframework.stereotype.Component
import java.math.BigInteger
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Component
class UsersDomain {
    fun validRole(roles: String): Boolean {
        UserRole.entries.forEach {
            if (it.roleName == roles) {
                return true
            }
        }
        return false
    }

    fun validStatus(status: String): Boolean {
        UserStatus.entries.forEach {
            if (it.status == status) {
                return true
            }
        }
        return false
    }


    //valid only for portuguese phone numbers
    fun validPhoneNumber(phoneNumber: String): Boolean {
        val regex = Regex("^\\+351\\d{9}$") // Matches +351 followed by 9 digits
        return phoneNumber.isNotBlank()
                && phoneNumber.length == 13
                && regex.matches(phoneNumber)
    }

    fun validName(name: String): Boolean {
        val regex = Regex("^[A-Za-zÀ-ÿ ]+$") // Allows letters (including accented) and spaces
        return name.isNotBlank()
                && name.length in 3..100
                && name.trim().contains(" ")
                && regex.matches(name)
    }

    fun validAddress(address: String): Boolean {
        val regex = Regex("^[\\wÀ-ÿ0-9.,'\\-/\\s]+$") // Allows letters, numbers, common punctuation
        return address.isNotBlank()
                && address.length in 5..255
                && regex.matches(address)
    }

    fun validEmail(email: String): Boolean {
        val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{6,}$")
        return email.length <= 100 && emailRegex.matches(email)
    }

    fun validPassword(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@#\$%^&+=!]{8,30}$")
        return passwordRegex.matches(password)
    }

    fun validBirthDate(birthDate: String): Boolean {
        return try {
            val date = LocalDate.parse(birthDate)  // Expects ISO format
            val today = LocalDate.now()
            !date.isAfter(today) && Period.between(date, today).years >= 18
        } catch (e: Exception) {
            false  // Parsing failed → invalid format or date
        }
    }

    fun validatePortugueseIban(iban: String): Boolean {
        // Step 1: Check format
        val regex = Regex("^PT\\d{23}$")
        if (!regex.matches(iban)) return false

        // Step 2: Rearrange (move first 4 chars to the end)
        val rearranged = iban.substring(4) + iban.substring(0, 4)

        // Step 3: Convert letters to numbers (A=10 to Z=35)
        val numericIban = buildString {
            for (char in rearranged) {
                if (char.isLetter()) {
                    append(char.code - 'A'.code + 10)
                } else {
                    append(char)
                }
            }
        }

        // Step 4: Compute mod 97
        return numericIban.toBigInteger() % 97.toBigInteger() == BigInteger.ONE
    }


}
