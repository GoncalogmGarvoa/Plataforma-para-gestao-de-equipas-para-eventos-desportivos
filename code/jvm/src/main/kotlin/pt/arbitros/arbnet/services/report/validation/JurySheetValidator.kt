package pt.arbitros.arbnet.services.report.validation

import pt.arbitros.arbnet.domain.JurySheet
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.repository.MatchDayRepository
import pt.arbitros.arbnet.repository.SessionsRepository
import pt.arbitros.arbnet.repository.UsersRepository
import pt.arbitros.arbnet.repository.adaptable_repos.CategoryRepository
import pt.arbitros.arbnet.repository.adaptable_repos.PositionRepository
import pt.arbitros.arbnet.services.*

object JurySheetValidator {
    fun validate(
        jury: List<JurySheet>,
        categoryRepository: CategoryRepository,
        matchDayRepository: MatchDayRepository,
        sessionRepository: SessionsRepository,
        positionRepository: PositionRepository,
        usersRepository: UsersRepository
    ): Either<ApiError, Unit> {
        if (jury.isEmpty()) return success(Unit)

        for (sheet in jury) {
            if (matchDayRepository.getMatchDayById(sheet.matchDayId) == null)
                return failure(ApiError.InvalidField("Invalid match day ID", "The provided match day ID does not exist or is invalid."))

            if (sessionRepository.getSessionById(sheet.sessionId) == null)
                return failure(ApiError.InvalidField("Invalid session ID", "The provided session ID does not exist or is invalid."))

            if (sheet.juryMembers.isEmpty())
                return failure(ApiError.InvalidField("At least one jury member is required", "The jury sheet must contain at least one jury member."))

            for (member in sheet.juryMembers) {

                val juryMember = usersRepository.getUserById(member.juryMemberId)
                if (juryMember == null)
                    return failure(ApiError.InvalidField("Invalid jury member ID", "The provided jury member ID does not exist or is invalid."))

                if (juryMember.name != member.name)
                    return failure(ApiError.InvalidField("Jury member name mismatch", "The provided jury member name does not match the ID."))

                if (categoryRepository.getCategoryIdByName(member.category) == null)
                    return failure(ApiError.InvalidField("Invalid jury member category", "The provided jury member category does not exist or is invalid."))

                if (positionRepository.getPositionIdByName(member.position) == null)
                    return failure(ApiError.InvalidField("Invalid jury member position", "The provided jury member position does not exist or is invalid."))
            }
        }

        return success(Unit)
    }
}
