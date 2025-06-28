package pt.arbitros.arbnet.services.report

import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.CoverSheet
import pt.arbitros.arbnet.domain.JurySheet
import pt.arbitros.arbnet.domain.RefereeEvaluation
import pt.arbitros.arbnet.domain.ReportMongo
import pt.arbitros.arbnet.domain.SessionReportInfo
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.repository.CompetitionRepository
import pt.arbitros.arbnet.repository.MatchDayRepository
import pt.arbitros.arbnet.repository.SessionsRepository
import pt.arbitros.arbnet.repository.adaptable_repos.CategoryRepository
import pt.arbitros.arbnet.repository.adaptable_repos.FunctionRepository
import pt.arbitros.arbnet.repository.adaptable_repos.PositionRepository
import pt.arbitros.arbnet.services.Either
import pt.arbitros.arbnet.services.Failure
import pt.arbitros.arbnet.services.failure
import pt.arbitros.arbnet.services.success
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import kotlin.collections.iterator

@Component
class ReportServiceInputValidation {

    fun validateReportValues(
        report: ReportMongo,
        createOrUpdate: Boolean,
        competitionRepository: CompetitionRepository,
        categoryRepository: CategoryRepository,
        functionRepository: FunctionRepository,
        sessionRepository: SessionsRepository,
        matchDayRepository: MatchDayRepository,
        positionRepository: PositionRepository
    ): Either<ApiError, Unit> {

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

        else if (!createOrUpdate && report.id == null) {
            return failure(
                ApiError.InvalidField(
                    "Report ID is required for update",
                    "The report ID must be provided when updating an existing report."
                )
            )
        }

        if (report.reportType.isBlank())
            return failure(
                ApiError.InvalidField(
                    "Report type is required",
                    "The report type must not be empty."
                )
            )
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

        val coverSheetValidationResult = validateReportCoverSheet(report.competitionId,report.coverSheet, matchDayRepository, sessionRepository)
        if (coverSheetValidationResult is Failure) return coverSheetValidationResult

        val registerValidationResult = validateRegisters(report.registers)
        if (registerValidationResult is Failure) return registerValidationResult

        val refereeEvaluationValidationResult = validateRefereeEvaluations(
            report.refereeEvaluations,
            categoryRepository,
            functionRepository,
            sessionRepository
        )
        if (refereeEvaluationValidationResult is Failure) return refereeEvaluationValidationResult

        val juryValidationResult = validateJurySheets(
            report.jury,
            categoryRepository,
            matchDayRepository,
            sessionRepository,
            positionRepository,
            )
        if (juryValidationResult is Failure) return juryValidationResult

        return success(Unit)
    }


    private fun validateReportCoverSheet(
        competitionId : Int,
        coverSheet: CoverSheet,
        matchDayRepository: MatchDayRepository,
        sessionRepository: SessionsRepository
    ) : Either<ApiError, Unit> {

        if (coverSheet.style.isBlank())
            return failure(
                ApiError.InvalidField(
                    "Cover sheet style is required",
                    "The cover sheet style must not be empty."
                )
            )
        if (coverSheet.councilName.isBlank())
            return failure(
                ApiError.InvalidField(
                    "Council name is required",
                    "The council name must not be empty."
                )
            )
        if (coverSheet.sportsSeason.isBlank())
            return failure(
                ApiError.InvalidField(
                    "Sports season is required",
                    "The sports season must not be empty."
                )
            )
        if (coverSheet.authorName.isBlank())
            return failure(
                ApiError.InvalidField(
                    "Author name is required",
                    "The author name must not be empty."
                )
            )
        if (coverSheet.location.isBlank())
            return failure(
                ApiError.InvalidField(
                    "Location is required",
                    "The location must not be empty."
                )
            )
        if (coverSheet.year <= 0)
            return failure(
                ApiError.InvalidField(
                    "Invalid year",
                    "The year must be a positive integer."
                )
            )
        if (coverSheet.month !in 1..12)
            return failure(
                ApiError.InvalidField(
                    "Invalid month",
                    "The month must be between 1 and 12."
                )
            )

        if (matchDayRepository.getMatchDaysByCompetition(competitionId).size != coverSheet.numMatchDays)
            return failure(
                ApiError.InvalidField(
                    "Match day count mismatch",
                    "The number of match days must match the numMatchDays field in the cover sheet."
                )
            )

        if (sessionRepository.getSessionsByCompetitionId(competitionId).size != coverSheet.numSessions)
            return failure(
                ApiError.InvalidField(
                    "Session count mismatch",
                    "The number of sessions must match the numSessions field in the cover sheet."
                )
            )

        if (coverSheet.sessions.size != coverSheet.numSessions)
            return failure(
                ApiError.InvalidField(
                    "Session count mismatch",
                    "The number of sessions must match the numSessions field in the cover sheet."
                )
            )

        for (session in coverSheet.sessions) {
            val validateSessionResult = validateCoverSheetSession(session)
            if (validateSessionResult is Failure) {
                return validateSessionResult
            }
        }

        return success(Unit)
    }

    fun validateCoverSheetSession(
        session: SessionReportInfo
    ): Either<ApiError, Unit> {
        if (session.sessionId <= 0)
            return failure(
                ApiError.InvalidField(
                    "Invalid session ID",
                    "The session ID must be a positive integer."
                )
            )
        if (session.date.isBlank())
            return failure(
                ApiError.InvalidField(
                    "Session date is required",
                    "The session date must not be empty."
                )
            )


        //TODO REView date format
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT)
        try {
            LocalDate.parse(session.date, dateFormatter)
        } catch (e: Exception) {
            return failure(
                ApiError.InvalidField(
                    "Invalid session date format",
                    "The session date must be a valid date in the format DD/MM/YYYY."
                )
            )
        }
        if (session.startTime.isBlank())
            return failure(
                ApiError.InvalidField(
                    "Session start time is required",
                    "The session start time must not be empty."
                )
            )
        if (session.endTime.isBlank())
            return failure(
                ApiError.InvalidField(
                    "Session end time is required",
                    "The session end time must not be empty."
                )
            )

        //TODO add endtime to sessions sql

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT)
        try {
            LocalTime.parse(session.startTime, timeFormatter)
            LocalTime.parse(session.endTime, timeFormatter)
        } catch (e: Exception) {
            return failure(
                ApiError.InvalidField(
                    "Invalid start or end time format",
                    "It must be a valid time in the format HH:mm (24-hour)."
                )
            )
        }

        if (session.durationMinutes <= 0)
            return failure(
                ApiError.InvalidField(
                    "Invalid session duration",
                    "The session duration must be a positive integer."
                )
            )

        return success(Unit)
    }

    fun validateRegisters(
        register: Map<String, String>
    ): Either<ApiError, Unit> {

        for (entry in register) {
            val key = entry.key
            val value = entry.value

            if (key.isBlank())
                return failure(
                    ApiError.InvalidField(
                        "Register key is required",
                        "The register key must not be empty."
                    )
                )
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

    fun validateRefereeEvaluations(
        evaluations: List<RefereeEvaluation>,
        categoryRepository: CategoryRepository,
        functionRepository: FunctionRepository,
        sessionRepository: SessionsRepository
    ): Either<ApiError, Unit> {
        if (evaluations.isEmpty())
            return failure(
                ApiError.InvalidField(
                    "At least one referee evaluation is required",
                    "The report must contain at least one referee evaluation."
                )
            )

        for (evaluation in evaluations) {
            if (evaluation.name.isBlank())
                return failure(
                    ApiError.InvalidField(
                        "Referee name is required",
                        "The referee name must not be empty."
                    )
                )
            if (evaluation.category.isBlank())
                return failure(
                    ApiError.InvalidField(
                        "Referee category is required",
                        "The referee category must not be empty."
                    )
                )

            if (categoryRepository.getCategoryIdByName(evaluation.category) == null)
                return failure(
                    ApiError.InvalidField(
                        "Invalid referee category",
                        "The provided referee category does not exist or is invalid."
                    )
                )

            if (evaluation.grade !in 0..5)
                return failure(
                    ApiError.InvalidField(
                        "Invalid referee grade",
                        "The referee grade must be an integer between 0 and 5."
                    )
                )
            if (evaluation.notes.isBlank())
                return failure(
                    ApiError.InvalidField(
                        "Referee notes are required",
                        "The referee notes must not be empty."
                    )
                )
            if (evaluation.functionBySession != null) {

                for ((sessionId, function) in evaluation.functionBySession) {
                    if (sessionId <= 0)
                        return failure(
                            ApiError.InvalidField(
                                "Invalid session ID in function by session",
                                "The session ID must be a positive integer for session $sessionId in referee evaluation."
                            )
                        )

                    if (sessionRepository.getSessionById(sessionId) == null) {
                        return failure(
                            ApiError.InvalidField(
                                "Invalid session ID for referee evaluation",
                                "The provided session ID does not exist or is invalid for session $sessionId in referee evaluation."
                            )
                        )
                    }

                    if (function.isBlank())
                        return failure(
                            ApiError.InvalidField(
                                "Function is required for session $sessionId",
                                "The function must not be empty for session $sessionId in referee evaluation."
                            )
                        )
                    if (functionRepository.getFunctionIdByName(function) == null) {
                        return failure(
                            ApiError.InvalidField(
                                "Invalid function for session $sessionId",
                                "The provided function does not exist or is invalid for session $sessionId in referee evaluation."
                            )
                        )
                    }
                }
            }
        }

        return success(Unit)
    }

    private fun validateJurySheets(
        jury: List<JurySheet>,
        categoryRepository: CategoryRepository,
        matchDayRepository: MatchDayRepository,
        sessionRepository: SessionsRepository,
        positionRepository: PositionRepository
    ): Either<ApiError, Unit> {
        if (jury.isEmpty()) {
            return success(Unit) // No jury sheets to validate
        }

        //TODO verify if we use the data we have or reafirme it or verify it

        for (sheet in jury) {
            if (sheet.matchDayId <= 0)
                return failure(
                    ApiError.InvalidField(
                        "Invalid match day ID",
                        "The match day ID must be a positive integer."
                    )
                )
            if (sheet.sessionId <= 0)
                return failure(
                    ApiError.InvalidField(
                        "Invalid session ID",
                        "The session ID must be a positive integer."
                    )
                )
            if (sheet.juryMembers.isEmpty())
                return failure(
                    ApiError.InvalidField(
                        "At least one jury member is required",
                        "The jury sheet must contain at least one jury member."
                    )
                )

            //TODO make this batch isntead of various calls

            for (member in sheet.juryMembers) {
                if (member.name.isBlank())
                    return failure(
                        ApiError.InvalidField(
                            "Jury member name is required",
                            "The jury member name must not be empty."
                        )
                    )

                if (member.category.isBlank())
                    return failure(
                        ApiError.InvalidField(
                            "Jury member category is required",
                            "The jury member category must not be empty."
                        )
                    )
                if (categoryRepository.getCategoryIdByName(member.category) == null) {
                    return failure(
                        ApiError.InvalidField(
                            "Invalid jury member category",
                            "The provided jury member category does not exist or is invalid."
                        )
                    )
                }

                if (member.position.isBlank())
                    return failure(
                        ApiError.InvalidField(
                            "Jury member position is required",
                            "The jury member position must not be empty."
                        )
                    )

                if (positionRepository.getPositionIdByName(member.position) == null) {
                    return failure(
                        ApiError.InvalidField(
                            "Invalid jury member position",
                            "The provided jury member position does not exist or is invalid."
                        )
                    )
                }
            }
        }

        return success(Unit)
    }
}