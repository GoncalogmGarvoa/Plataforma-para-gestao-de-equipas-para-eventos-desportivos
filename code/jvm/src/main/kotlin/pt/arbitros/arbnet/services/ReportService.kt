package pt.arbitros.arbnet.services

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.CallListDomain
import pt.arbitros.arbnet.domain.Report
import pt.arbitros.arbnet.domain.UtilsDomain
import pt.arbitros.arbnet.repository.ReportMongoRepository
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.transactionRepo

sealed class ReportError {
    data object NotFound : ReportError()
    data object AlreadyExists : ReportError()
}

@Component
class ReportService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    private val reportMongoRepository: ReportMongoRepository,
    private val utilsDomain: UtilsDomain,
) {

    fun createReport(
        report: Report,
    ): Either<ReportError, String> {
        val reportCreated = reportMongoRepository.save(report)

        if (reportCreated.id == null) {
            return failure(ReportError.AlreadyExists)
        }

        transactionManager.run {
            it.reportRepository.createReport(
                reportId = reportCreated.id,
                reportType = report.reportType,
                competitionId = report.competitionId,
            )
        }

        return success(reportCreated.id)
    }

    fun getAllReports(): Either<ReportError, List<Report>> {
        val result = transactionManager.run {
             return@run it.reportRepository.getAllReports()
        }
        return success(result)
    }

    fun getReportById(id: String): Either<ReportError, Report?> {
        TODO()
    }

    fun updateReport(report: Report): Either<ReportError, Report> {
        TODO()
    }

    fun sealReport(id: String): Either<ReportError, Report> {
        TODO()
    }
}