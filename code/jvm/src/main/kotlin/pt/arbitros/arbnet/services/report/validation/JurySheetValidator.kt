package pt.arbitros.arbnet.services.report.validation

import pt.arbitros.arbnet.domain.JurySheet
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.repository.MatchDayRepository
import pt.arbitros.arbnet.repository.SessionsRepository
import pt.arbitros.arbnet.repository.adaptable_repos.CategoryRepository
import pt.arbitros.arbnet.repository.adaptable_repos.PositionRepository
import pt.arbitros.arbnet.services.*

object JurySheetValidator {
    fun validate(
        jury: List<JurySheet>,
        categoryRepository: CategoryRepository,
        matchDayRepository: MatchDayRepository,
        sessionRepository: SessionsRepository,
        positionRepository: PositionRepository
    ): Either<ApiError, Unit> {
        if (jury.isEmpty()) return success(Unit)

        for (sheet in jury) {
            if (sheet.matchDayId <= 0)
                return failure(ApiError.InvalidField("Invalid match day ID", "The match day ID must be a positive integer."))

            if (sheet.sessionId <= 0)
                return failure(ApiError.InvalidField("Invalid session ID", "The session ID must be a positive integer."))

            if (sheet.juryMembers.isEmpty())
                return failure(ApiError.InvalidField("At least one jury member is required", "The jury sheet must contain at least one jury member."))

            for (member in sheet.juryMembers) {
                if (member.name.isBlank())
                    return failure(ApiError.InvalidField("Jury member name is required", "The jury member name must not be empty."))

                if (member.category.isBlank())
                    return failure(ApiError.InvalidField("Jury member category is required", "The jury member category must not be empty."))

                if (categoryRepository.getCategoryIdByName(member.category) == null)
                    return failure(ApiError.InvalidField("Invalid jury member category", "The provided jury member category does not exist or is invalid."))

                if (member.position.isBlank())
                    return failure(ApiError.InvalidField("Jury member position is required", "The jury member position must not be empty."))

                if (positionRepository.getPositionIdByName(member.position) == null)
                    return failure(ApiError.InvalidField("Invalid jury member position", "The provided jury member position does not exist or is invalid."))
            }
        }

        return success(Unit)
    }
}
