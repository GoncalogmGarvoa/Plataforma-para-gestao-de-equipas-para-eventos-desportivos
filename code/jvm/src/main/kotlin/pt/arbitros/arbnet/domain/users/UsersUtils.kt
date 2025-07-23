package pt.arbitros.arbnet.domain.users

import org.springframework.stereotype.Component
import java.math.BigInteger
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.Period

@Component
class UsersUtils {

    fun validStatus(status: String): Boolean {
        UserStatus.entries.forEach {
            if (it.status == status) {
                return true
            }
        }
        return false
    }

    fun validBirthDate(birthDate: String): Boolean =
        try {
            val date = LocalDate.parse(birthDate) // Expects ISO format
            val today = LocalDate.now()
            !date.isAfter(today) && Period.between(date, today).years >= 18
        } catch (e: Exception) {
            false // Parsing failed → invalid format or date
        }

//    fun validBirthDate(birthDate: String): Boolean =
//        try {
//            val date = OffsetDateTime.parse(birthDate).toLocalDate()
//            val today = LocalDate.now()
//            !date.isAfter(today) && Period.between(date, today).years >= 18
//        } catch (e: Exception) {
//            false
//        }

    fun validatePortugueseIban(iban: String): Boolean {
        val cleanIban = iban.replace("\\s".toRegex(), "")
        val regex = Regex("^PT\\d{23}$")
        if (!regex.matches(cleanIban)) return false

        val rearranged = cleanIban.substring(4) + cleanIban.substring(0, 4)

        val numericIban =
            buildString {
                for (char in rearranged) {
                    if (char.isLetter()) {
                        append(char.code - 'A'.code + 10)
                    } else {
                        append(char)
                    }
                }
            }

        return BigInteger(numericIban) % BigInteger("97") == BigInteger.ONE
    }
}
