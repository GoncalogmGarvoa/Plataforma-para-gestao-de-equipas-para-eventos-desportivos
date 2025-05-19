package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.arbitros.arbnet.domain.ReportMongo
import pt.arbitros.arbnet.domain.ReportSQL
import pt.arbitros.arbnet.repository.ReportRepository

class ReportRepositoryJdbi(private val handle: Handle) : ReportRepository{

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

    override fun getReportById(id: String): ReportSQL? {
        return handle.createQuery(
            """ select id, report_type, competition_id from dbp.reports where id = :id"""
        ).bind("id", id)
        .mapTo(ReportSQL::class.java)
        .findOne()
        .orElse(null)
    }

    override fun getAllReports(): List<ReportSQL> {
        return handle.createQuery(
            """ select id, report_type, competition_id from dbp.reports"""
        ).mapTo(ReportSQL::class.java)
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
