package pt.arbitros.arbnet.services.report.validation

import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.ReportMongo
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.repository.CompetitionRepository
import pt.arbitros.arbnet.repository.MatchDayRepository
import pt.arbitros.arbnet.repository.SessionsRepository
import pt.arbitros.arbnet.repository.Transaction
import pt.arbitros.arbnet.repository.adaptable_repos.CategoryRepository
import pt.arbitros.arbnet.repository.adaptable_repos.FunctionRepository
import pt.arbitros.arbnet.repository.adaptable_repos.PositionRepository
import pt.arbitros.arbnet.services.Either
import pt.arbitros.arbnet.services.Failure
import pt.arbitros.arbnet.services.failure
import pt.arbitros.arbnet.services.success

@Component
object ReportValidator {
    fun validate(
        report: ReportMongo,
        createOrUpdate: Boolean,
        jdbiTransaction: Transaction
    ): Either<ApiError, Unit> {

        val competitionRepository = jdbiTransaction.competitionRepository
        val matchDayRepository = jdbiTransaction.matchDayRepository
        val sessionRepository = jdbiTransaction.sessionsRepository
        val categoryRepository = jdbiTransaction.categoryRepository
        val functionRepository = jdbiTransaction.functionRepository
        val positionRepository = jdbiTransaction.positionRepository
        val usersRepository = jdbiTransaction.usersRepository


        if (competitionRepository.getCompetitionById(report.competitionId) == null) {
            return failure(
                ApiError.InvalidField(
                    "Invalid competition ID",
                    "The provided competition ID does not exist or is invalid."
                )
            )
        }

        if (createOrUpdate && report.id != null)
            return failure(
                ApiError.InvalidField(
                    "Report ID should not be provided for creation",
                    "The report ID must be null when creating a new report."
                )
            )

        if (report.sealed)
            return failure(
                ApiError.InvalidField(
                    "Report is sealed",
                    "Sealed reports cannot be modified, and new reports cant be sealed."
                )
            )

        if (!createOrUpdate && report.id == null)
            return failure(
                ApiError.InvalidField(
                    "Report ID is required for update",
                    "The report ID must be provided when updating an existing report."
                )
            )

        if (report.reportType.isBlank())
            return failure(ApiError.InvalidField("Report type is required", "The report type must not be empty."))

        if (report.reportType.length > 50)
            return failure(
                ApiError.InvalidField(
                    "Report type is too long",
                    "The report type must not exceed 50 characters."
                )
            )

        if (report.competitionId <= 0)
            return failure(
                ApiError.InvalidField(
                    "Invalid competition ID",
                    "The competition ID must be a positive integer."
                )
            )

        CoverSheetValidator.validate(
            report.competitionId,
            report.coverSheet,
            matchDayRepository,
            sessionRepository
        ).let {
            if (it is Failure) return it
        }

        RegisterValidator.validate(report.registers).let {
            if (it is Failure) return it
        }

        RefereeEvaluationValidator.validate(
            report.refereeEvaluations,
            categoryRepository,
            functionRepository,
            sessionRepository
        ).let {
            if (it is Failure) return it
        }

        JurySheetValidator.validate(
            report.jury,
            categoryRepository,
            matchDayRepository,
            sessionRepository,
            positionRepository,
            usersRepository
        ).let {
            if (it is Failure) return it
        }

        return success(Unit)
    }
}