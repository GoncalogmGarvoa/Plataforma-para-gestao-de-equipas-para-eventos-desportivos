package pt.arbitros.arbnet.services.report.validation

import pt.arbitros.arbnet.domain.RefereeEvaluation
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.repository.adaptable_repos.*
import pt.arbitros.arbnet.repository.SessionsRepository
import pt.arbitros.arbnet.services.*

object RefereeEvaluationValidator {
    fun validate(
        evaluations: List<RefereeEvaluation>,
        categoryRepository: CategoryRepository,
        functionRepository: FunctionRepository,
        sessionRepository: SessionsRepository
    ): Either<ApiError, Unit> {
        if (evaluations.isEmpty())
            return failure(ApiError.InvalidField("At least one referee evaluation is required", "The report must contain at least one referee evaluation."))

        for (evaluation in evaluations) {
            if (evaluation.name.isBlank())
                return failure(ApiError.InvalidField("Referee name is required", "The referee name must not be empty."))

            if (evaluation.category.isBlank())
                return failure(ApiError.InvalidField("Referee category is required", "The referee category must not be empty."))

            if (categoryRepository.getCategoryIdByName(evaluation.category) == null)
                return failure(ApiError.InvalidField("Invalid referee category", "The provided referee category does not exist or is invalid."))

            if (evaluation.grade !in 0..5)
                return failure(ApiError.InvalidField("Invalid referee grade", "The referee grade must be an integer between 0 and 5."))

            evaluation.functionBySession?.forEach { (sessionId, function) ->
                if (sessionId <= 0)
                    return failure(ApiError.InvalidField("Invalid session ID in function by session", "The session ID must be a positive integer for session $sessionId in referee evaluation."))

                if (sessionRepository.getSessionById(sessionId) == null)
                    return failure(ApiError.InvalidField("Invalid session ID for referee evaluation", "The provided session ID does not exist or is invalid for session $sessionId in referee evaluation."))

                if (function.isBlank())
                    return failure(ApiError.InvalidField("Function is required for session $sessionId", "The function must not be empty for session $sessionId in referee evaluation."))

                if (functionRepository.getFunctionIdByName(function) == null)
                    return failure(ApiError.InvalidField("Invalid function for session $sessionId", "The provided function does not exist or is invalid for session $sessionId in referee evaluation."))
            }
        }

        return success(Unit)
    }
}
