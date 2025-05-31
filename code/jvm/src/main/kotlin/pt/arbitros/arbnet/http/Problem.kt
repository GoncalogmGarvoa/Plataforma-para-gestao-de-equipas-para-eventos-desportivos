package pt.arbitros.arbnet.http

import org.springframework.http.ResponseEntity

private const val MEDIA_TYPE = "application/problem+json"

data class Problem(
    val typeUri: String,
    val title: String,
    val status: Int,
    val detail: String,
) {

    companion object {
        fun from(error: ApiError) = Problem(
            typeUri = error.typeUri.toString(),
            title = error.title,
            status = error.status,
            detail = error.detail
        )

        fun fromApiErrorToProblemResponse(error: ApiError) = from(error).toResponse()
    }

    fun toResponse(): ResponseEntity<Any> =
        ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .header("date", java.time.Instant.now().toString())
            .body(this)
}