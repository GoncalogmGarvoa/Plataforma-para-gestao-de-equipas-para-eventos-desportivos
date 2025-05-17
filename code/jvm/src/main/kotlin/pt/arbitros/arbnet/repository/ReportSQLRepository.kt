package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.Report

interface ReportSQLRepository {
    fun createReport(
        reportId: String,
        reportType: String,
        competitionId: Int,
    ): Boolean

    fun getReportById(id: String): Report?

    fun getAllReports(): List<Report>

    fun updateReport(
        reportId: String,
        reportType: String,
    ): Boolean
}