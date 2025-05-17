package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.domain.Report
import pt.arbitros.arbnet.repository.ReportSQLRepository

class ReportRepositoryJdbi(private val handle: Handle) : ReportSQLRepository{

    override fun createReport(
        reportId: String,
        reportType: String,
        competitionId: Int
    ): Boolean {
        return handle.createUpdate(
            """ insert into dbp.reports (id, report_type, competition_id) values (:reportId, :reportType, :competitionId)"""
        ).bind("reportId", reportId)
        .bind("reportType", reportType)
        .bind("competitionId", competitionId)
        .execute() > 0
    }

    override fun getReportById(id: String): Report? {
        return handle.createQuery(
            """ select id, report_type, competition_id from dbp.reports where id = :id"""
        ).bind("id", id)
        .mapTo(Report::class.java)
        .findOne()
        .orElse(null)
    }

    override fun getAllReports(): List<Report> {
        return handle.createQuery(
            """ select id, report_type, competition_id from dbp.reports"""
        ).mapTo(Report::class.java)
        .list()
    }

    override fun updateReport(reportId: String, reportType: String): Boolean {
        return handle.createUpdate(
            """ update dbp.reports set report_type = :reportType where id = :reportId"""
        ).bind("reportId", reportId)
        .bind("reportType", reportType)
        .execute() > 0
    }


}
