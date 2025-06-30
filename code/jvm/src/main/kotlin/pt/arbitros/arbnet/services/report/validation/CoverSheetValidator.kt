package pt.arbitros.arbnet.services.report.validation

import pt.arbitros.arbnet.domain.CoverSheet
import pt.arbitros.arbnet.domain.SessionReportInfo
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.repository.MatchDayRepository
import pt.arbitros.arbnet.repository.SessionsRepository
import pt.arbitros.arbnet.services.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

object CoverSheetValidator {
    fun validate(
        competitionId: Int,
        coverSheet: CoverSheet,
        matchDayRepository: MatchDayRepository,
        sessionRepository: SessionsRepository
    ): Either<ApiError, Unit> {
        if (coverSheet.style.isBlank())
            return failure(ApiError.InvalidField("Cover sheet style is required", "The cover sheet style must not be empty."))

        if (coverSheet.councilName.isBlank())
            return failure(ApiError.InvalidField("Council name is required", "The council name must not be empty."))

        if (coverSheet.sportsSeason.isBlank())
            return failure(ApiError.InvalidField("Sports season is required", "The sports season must not be empty."))

        if (coverSheet.authorName.isBlank())
            return failure(ApiError.InvalidField("Author name is required", "The author name must not be empty."))

        if (coverSheet.location.isBlank())
            return failure(ApiError.InvalidField("Location is required", "The location must not be empty."))

        if (coverSheet.year <= 0)
            return failure(ApiError.InvalidField("Invalid year", "The year must be a positive integer."))

        if (coverSheet.month !in 1..12)
            return failure(ApiError.InvalidField("Invalid month", "The month must be between 1 and 12."))

        if (matchDayRepository.getMatchDaysByCompetition(competitionId).size != coverSheet.numMatchDays)
            return failure(ApiError.InvalidField("Match day count mismatch", "The number of match days must match the numMatchDays field in the cover sheet."))

        if (sessionRepository.getSessionsByCompetitionId(competitionId).size != coverSheet.numSessions)
            return failure(ApiError.InvalidField("Session count mismatch", "The number of sessions must match the numSessions field in the cover sheet."))

        if (coverSheet.sessions.size != coverSheet.numSessions)
            return failure(ApiError.InvalidField("Session count mismatch", "The number of sessions must match the numSessions field in the cover sheet."))

        for (session in coverSheet.sessions) {
            validateSession(session).let {
                if (it is Failure) return it
            }
        }

        return success(Unit)
    }

    fun validateSession(session: SessionReportInfo): Either<ApiError, Unit> {
        if (session.sessionId <= 0)
            return failure(ApiError.InvalidField("Invalid session ID", "The session ID must be a positive integer."))

        if (session.date.isBlank())
            return failure(ApiError.InvalidField("Session date is required", "The session date must not be empty."))

        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)
        try {
            LocalDate.parse(session.date, dateFormatter)
        } catch (e: Exception) {
            return failure(ApiError.InvalidField("Invalid session date format", "The session date must be a valid date in the format DD/MM/YYYY."))
        }

        if (session.startTime.isBlank())
            return failure(ApiError.InvalidField("Session start time is required", "The session start time must not be empty."))

        if (session.endTime.isBlank())
            return failure(ApiError.InvalidField("Session end time is required", "The session end time must not be empty."))

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT)
        try {
            LocalTime.parse(session.startTime, timeFormatter)
            LocalTime.parse(session.endTime, timeFormatter)
        } catch (e: Exception) {
            return failure(ApiError.InvalidField("Invalid start or end time format", "It must be a valid time in the format HH:mm (24-hour)."))
        }

        if (session.durationMinutes <= 0)
            return failure(ApiError.InvalidField("Invalid session duration", "The session duration must be a positive integer."))

        return success(Unit)
    }
}