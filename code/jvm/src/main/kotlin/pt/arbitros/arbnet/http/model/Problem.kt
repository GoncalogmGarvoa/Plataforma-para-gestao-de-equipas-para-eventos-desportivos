package pt.arbitros.arbnet.http.model

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

private const val MEDIA_TYPE = "application/problem+json"
private const val PROBLEM_URI_PATH =
    "https://github.com/GoncalogmGarvoa/Plataforma-para-gestao-de-equipas-para-eventos-desportivos" +
        "/blob/main/code/jvm/docs/problems"

sealed class Problem(
    typeUri: URI,
) {
    val type = typeUri.toString()
    val title = typeUri.toString().split("/").last()

    fun response(status: HttpStatus): ResponseEntity<Any> =
        ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body(this)

    // callList
    data object CallListNotFound : Problem(URI("$PROBLEM_URI_PATH/callList-not-found"))

    data object RoleNotFound : Problem(URI("$PROBLEM_URI_PATH/role-not-found"))

    data object MatchDayNotFound : Problem(URI("$PROBLEM_URI_PATH/matchDay-not-found"))

    data object ParticpantNotFound : Problem(URI("$PROBLEM_URI_PATH/participant-not-found"))

    data object ArbitrationCouncilNotFound : Problem(URI("$PROBLEM_URI_PATH/arbitrationCouncil-not-found"))

    // Users
    data object UserNotFound : Problem(URI("$PROBLEM_URI_PATH/user-not-found"))

    data object EmailNotFound : Problem(URI("$PROBLEM_URI_PATH/email-not-found"))

    data object EmailAlreadyUsed : Problem(URI("$PROBLEM_URI_PATH/email-already-used"))

    data object PhoneNumberAlreadyUsed : Problem(URI("$PROBLEM_URI_PATH/phone-number-used"))

    data object IbanAlreadyUsed : Problem(URI("$PROBLEM_URI_PATH/iban-already-used"))

    data object UserAlreadyHasRole : Problem(URI("$PROBLEM_URI_PATH/user-already-has-role"))

    data object UserWithoutRole : Problem(URI("$PROBLEM_URI_PATH/user-without-role"))
}
