package pt.arbitros.arbnet.services.report.validation

import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.services.Either
import pt.arbitros.arbnet.services.failure
import pt.arbitros.arbnet.services.success
import kotlin.collections.iterator

object RegisterValidator {
    fun validate(register: Map<String, String>): Either<ApiError, Unit> {
        for ((key, value) in register) {
            if (key.isBlank())
                return failure(ApiError.InvalidField("Register key is required", "The register key must not be empty."))

            if (value.isBlank())
                return failure(
                    ApiError.InvalidField(
                        "Register value is required",
                        "The register value must not be empty."
                    )
                )
        }
        return success(Unit)
    }
}