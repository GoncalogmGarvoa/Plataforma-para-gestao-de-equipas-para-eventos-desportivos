package pt.arbitros.arbnet.http

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.arbitros.arbnet.domain.Report
import pt.arbitros.arbnet.repository.ReportMongoRepository
import pt.arbitros.arbnet.services.Either
import pt.arbitros.arbnet.services.Failure
import pt.arbitros.arbnet.services.ReportError
import pt.arbitros.arbnet.services.ReportService
import pt.arbitros.arbnet.services.Success


@RestController
class ReportController(private val reportService : ReportService, private val reportRepository: ReportMongoRepository) {

    @PostMapping(Uris.ReportUris.CREATE_REPORT)
    fun createReport(
        @RequestBody report: Report
    ): ResponseEntity<*> =
        when (val result = reportService.createReport(report)) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure ->
                when (result.value) {
                    is ReportError.AlreadyExists -> Problem.ReportAlreadyExists.response(HttpStatus.BAD_REQUEST)
                    is ReportError.NotFound -> Problem.ReportNotFound.response(HttpStatus.NOT_FOUND)
                }
        }


    @GetMapping(Uris.ReportUris.GET_ALL_REPORTS)
    fun getAllReports(): List<Report> =
        when (val result = reportService.getAllReports()) {
            is Success -> result.value
            is Failure -> TODO()
        }

    @GetMapping(Uris.ReportUris.GET_REPORT_BY_ID)
    fun getReportById(@PathVariable id: String): ResponseEntity<*> =
        when (val result = reportService.getReportById(id)) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.ReportNotFound.response(HttpStatus.NOT_FOUND)
        }

    @PutMapping(Uris.ReportUris.UPDATE_REPORT)
    fun updateReport(@RequestBody report: Report): ResponseEntity<*> =
        when (val result = reportService.updateReport(report)) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure ->
                when (result.value) {
                    is ReportError.NotFound -> Problem.ReportNotFound.response(HttpStatus.NOT_FOUND)
                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }

    @PutMapping(Uris.ReportUris.SEAL_REPORT)
    fun sealReport(@PathVariable id: String): ResponseEntity<*> =
        when (val result = reportService.sealReport(id)) {
            is Success -> ResponseEntity.ok(result)
            is Failure -> Problem.ReportNotFound.response(HttpStatus.NOT_FOUND)
        }
}
