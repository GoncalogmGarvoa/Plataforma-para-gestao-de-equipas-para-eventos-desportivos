package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.ReportSQL
import pt.arbitros.arbnet.repository.ReportRepository

class ReportRepositoryMem: ReportRepository {
    override fun createReport(
        reportId: String,
        reportType: String,
        competitionId: Int
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun getReportById(id: String): ReportSQL? {
        TODO("Not yet implemented")
    }

    override fun getAllReports(): List<ReportSQL> {
        TODO("Not yet implemented")
    }

    override fun updateReport(reportId: String, reportType: String): Boolean {
        TODO("Not yet implemented")
    }
}