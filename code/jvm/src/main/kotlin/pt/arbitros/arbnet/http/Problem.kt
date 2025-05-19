package pt.arbitros.arbnet.http

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

    // CallList
    data object CallListNotFound : Problem(URI("$PROBLEM_URI_PATH/callList-not-found"))

    data object RoleNotFound : Problem(URI("$PROBLEM_URI_PATH/role-not-found"))

    data object MatchDayNotFound : Problem(URI("$PROBLEM_URI_PATH/matchDay-not-found"))

    data object CompetitionNotFound : Problem(URI("$PROBLEM_URI_PATH/competition-not-found"))

    data object ParticipantNotFound : Problem(URI("$PROBLEM_URI_PATH/participant-not-found"))

    data object ArbitrationCouncilNotFound : Problem(URI("$PROBLEM_URI_PATH/arbitrationCouncil-not-found"))

    // Users
    data object UserNotFound : Problem(URI("$PROBLEM_URI_PATH/user-not-found"))

    data object NeededFullName : Problem(URI("$PROBLEM_URI_PATH/needed-full-name"))

    data object EmailNotFound : Problem(URI("$PROBLEM_URI_PATH/email-not-found"))

    data object EmailAlreadyUsed : Problem(URI("$PROBLEM_URI_PATH/email-already-used"))

    data object PhoneNumberAlreadyUsed : Problem(URI("$PROBLEM_URI_PATH/phone-number-used"))

    data object IbanAlreadyUsed : Problem(URI("$PROBLEM_URI_PATH/iban-already-used"))

    data object UserAlreadyHasRole : Problem(URI("$PROBLEM_URI_PATH/user-already-has-role"))

    data object UserWithoutRole : Problem(URI("$PROBLEM_URI_PATH/user-without-role"))

    data object UserOrPasswordAreInvalid : Problem(URI("$PROBLEM_URI_PATH/user-or-password-are-invalid"))

    data object MissingField : Problem(URI("$PROBLEM_URI_PATH/missing-field"))

    // Request data validation

    data object InvalidName : Problem(URI("$PROBLEM_URI_PATH/invalid-name"))

    data object InvalidAddress : Problem(URI("$PROBLEM_URI_PATH/invalid-address"))

    data object InvalidPassword : Problem(URI("$PROBLEM_URI_PATH/invalid-password"))

    data object InvalidBirthDate : Problem(URI("$PROBLEM_URI_PATH/invalid-birth-date"))

    data object InvalidIban : Problem(URI("$PROBLEM_URI_PATH/invalid-iban"))

    data object InvalidPhoneNumber : Problem(URI("$PROBLEM_URI_PATH/invalid-phone-number"))

    data object InvalidEmail : Problem(URI("$PROBLEM_URI_PATH/invalid-email"))

    data object InvalidAssociation : Problem(URI("$PROBLEM_URI_PATH/invalid-association"))

    data object InvalidLocation : Problem(URI("$PROBLEM_URI_PATH/invalid-location"))

    data object InvalidCompetitionName : Problem(URI("$PROBLEM_URI_PATH/invalid-competition-name"))

    // Report
    data object ReportNotFound : Problem(URI("$PROBLEM_URI_PATH/report-not-found"))

    data object ReportAlreadyExists : Problem(URI("$PROBLEM_URI_PATH/report-already-exists"))

    //Generic

    data object InternalError : Problem(URI("$PROBLEM_URI_PATH/internal-error"))
}
