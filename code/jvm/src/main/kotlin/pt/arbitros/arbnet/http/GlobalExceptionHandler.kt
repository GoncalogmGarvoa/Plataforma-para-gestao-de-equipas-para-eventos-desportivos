package pt.arbitros.arbnet.http

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.format.DateTimeParseException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<Map<String, Any>> {
        val error = ApiError.InternalServerError("Unexpected error: ${ex.message}")
        return ResponseEntity.status(error.status)
            .contentType(MediaType.valueOf("application/problem+json"))
            .body(buildProblemBody(error))
    }

    private fun buildProblemBody(error: ApiError): Map<String, Any> =
        mapOf(
            "type" to error.typeUri,
            "title" to error.title,
            "status" to error.status,
            "detail" to error.detail
        )
}
