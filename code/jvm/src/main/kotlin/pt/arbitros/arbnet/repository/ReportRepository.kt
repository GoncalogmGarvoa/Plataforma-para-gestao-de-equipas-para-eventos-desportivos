package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.ReportMongo
import pt.arbitros.arbnet.domain.ReportSQL

interface ReportRepository {
    fun createReport(
        reportId: String,
        reportType: String,
        competitionId: Int,
    ): Boolean

    fun getReportById(id: String): ReportSQL?

    fun getAllReports(): List<ReportSQL>

    fun updateReport(
        reportId: String,
        reportType: String,
    ): Boolean
}