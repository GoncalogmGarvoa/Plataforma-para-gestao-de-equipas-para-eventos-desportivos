package pt.arbitros.arbnet.repository.Aux

import pt.arbitros.arbnet.domain.Report

interface ReportRepository {
    fun createReport(report: Report): Boolean

    fun findReportById(
        id: Int,
        competitionId: Int,
    ): Report?

    fun getReportsByCompetition(competitionId: Int): List<Report>

    fun deleteReport(
        id: Int,
        competitionId: Int,
    ): Boolean
}
