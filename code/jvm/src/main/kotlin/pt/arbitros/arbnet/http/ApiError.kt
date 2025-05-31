package pt.arbitros.arbnet.http

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

private const val PROBLEM_URI_PATH =
    "https://github.com/GoncalogmGarvoa/Plataforma-para-gestao-de-equipas-para-eventos-desportivos" +
            "/blob/main/code/jvm/docs/problems"

sealed class ApiError(
    val typeUri: URI,
    val title: String,
    val status: Int,
    val detail: String
) {

    data class AlreadyExists(
        val customTitle: String = "Conflict",
        val customDetail: String = "field already exists"
    ) : ApiError(
        URI("$PROBLEM_URI_PATH/already-exists"),
        customTitle,
        HttpStatus.CONFLICT.value(),
        customDetail
    )

    data class NotFound(
        val customTitle: String = "Not Found",
        val customDetail: String = "Resource not found"
    ) : ApiError(
        URI("$PROBLEM_URI_PATH/not-found"),
        customTitle,
        HttpStatus.NOT_FOUND.value(),
        customDetail
    )

    data class InvalidField(
        val customTitle: String = "Invalid field",
        val customDetail: String = "Invalid value for field"
    ) : ApiError(
        URI("$PROBLEM_URI_PATH/invalid-field"),
        customTitle,
        HttpStatus.BAD_REQUEST.value(),
        customDetail
    )

    data class MissingField(
        val customTitle: String = "Missing field",
        val customDetail: String = "Required field is missing"
    ) : ApiError(
        URI("$PROBLEM_URI_PATH/missing-field"),
        customTitle,
        HttpStatus.BAD_REQUEST.value(),
        customDetail
    )

    // No custom detail for these due to safety reasons, don't want to leak information about the system
    data class Unauthorized(
        val customTitle: String = "Unauthorized",
    ) : ApiError(
        URI("$PROBLEM_URI_PATH/unauthorized"),
        customTitle,
        HttpStatus.UNAUTHORIZED.value(),
        "You must be authenticated to access this resource"
    )

    data class Forbidden(
        val customTitle: String = "Forbidden"
    ) : ApiError(
        URI("$PROBLEM_URI_PATH/forbidden"),
        customTitle,
        HttpStatus.FORBIDDEN.value(),
        "You do not have permission to access this resource"
    )

    data class InternalServerError(
        val customTitle: String = "Internal Server Error",
        val customDetail: String = "An unexpected error occurred"
    ) : ApiError(
        URI("$PROBLEM_URI_PATH/internal-server-error"),
        customTitle,
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        customDetail
    )

}

fun invalidFieldError(field: String): ApiError =
    ApiError.InvalidField(
        "Invalid $field",
        "The provided $field is invalid. Please check the format and try again.",
    )

