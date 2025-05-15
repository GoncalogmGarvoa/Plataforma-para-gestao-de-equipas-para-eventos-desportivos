package pt.arbitros.arbnet.http

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.arbitros.arbnet.domain.Report
import pt.arbitros.arbnet.repository.ReportRepository
import pt.arbitros.arbnet.utils.Success

@RestController
class ReportController(private val reportRepository: ReportRepository) {

    @PostMapping(Uris.ReportUris.CREATE_REPORT)
    fun createReport(@RequestBody report: Report): Success<Report> {
        return Success(reportRepository.save(report))
    }

    @GetMapping(Uris.ReportUris.GET_ALL_REPORTS)
    fun getAllReports(): List<Report> =
        reportRepository.findAll()

    @GetMapping(Uris.ReportUris.GET_REPORT_BY_ID)
    fun getReportById(@PathVariable id: String): Report? =
        reportRepository.findById(id).orElse(null)
}
