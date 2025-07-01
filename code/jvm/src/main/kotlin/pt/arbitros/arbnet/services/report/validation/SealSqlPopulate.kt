package pt.arbitros.arbnet.services.report.validation

import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.ReportMongo
import pt.arbitros.arbnet.domain.Session
import pt.arbitros.arbnet.repository.Transaction
import java.time.LocalTime

@Component
object SealSqlPopulate {

    fun reportSealSqlPopulate(report: ReportMongo, transaction: Transaction): Boolean {

        if (!transaction.reportRepository.createReport(
                report.id!!, //Will not be null, as this is called after the report is created
                report.reportType,
                report.competitionId
            )
        ) return false

        return completeSessionsEndTime(report, transaction)
    }

    private fun completeSessionsEndTime(
        report: ReportMongo,
        transaction: Transaction
    ): Boolean {
        val sessionRepository = transaction.sessionsRepository
        val sessions = sessionRepository.getSessionsByCompetitionId(report.competitionId)

        sessions.forEach { session ->
            sessionRepository.setEndTime(
                session.id,
                LocalTime.parse(report.coverSheet.sessions.find { it.sessionId == session.id }?.endTime ?: return false))
        }

        return true
    }
}