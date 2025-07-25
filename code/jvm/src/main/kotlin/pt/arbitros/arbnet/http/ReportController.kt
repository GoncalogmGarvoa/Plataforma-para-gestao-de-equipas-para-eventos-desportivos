package pt.arbitros.arbnet.http

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.arbitros.arbnet.http.model.report.ReportInputModel
import pt.arbitros.arbnet.repository.mongo.ReportMongoRepository
import pt.arbitros.arbnet.services.Failure
import pt.arbitros.arbnet.services.report.ReportService
import pt.arbitros.arbnet.services.Success

// TODO verify if useful -> @RestControllerAdvice


//  2 tipos de relatorio:
//  - Relatorio do juiz arbitro
//  - Relatorio do delegado


@RestController
class ReportController(private val reportService : ReportService) {

    @PostMapping(Uris.ReportUris.CREATE_REPORT)
    fun createReport(@RequestBody report: ReportInputModel): ResponseEntity<*> =
        when (val result = reportService.createReport(report)) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @GetMapping(Uris.ReportUris.GET_ALL_REPORTS)
    fun getAllReports(): ResponseEntity<*> =
        when (val result = reportService.getAllReports()) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @GetMapping(Uris.ReportUris.GET_ALL_REPORTS_BY_COMPETITION)
    fun getAllReportsByCompetition(@PathVariable competitionId: Int): ResponseEntity<*> =
        when (val result = reportService.getAllReportsByCompetition(competitionId)) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @GetMapping(Uris.ReportUris.GET_REPORT_BY_ID)
    fun getReportById(@PathVariable id: String): ResponseEntity<*> =
        when (val result = reportService.getReportById(id)) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @PutMapping(Uris.ReportUris.UPDATE_REPORT)
    fun updateReport(@RequestBody report: ReportInputModel): ResponseEntity<*> =
        when (val result = reportService.updateReport(report)) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @PutMapping(Uris.ReportUris.SEAL_REPORT)
    fun sealReport(@PathVariable id: String): ResponseEntity<*> =
        when (val result = reportService.sealReport(id)) {
            is Success -> ResponseEntity.ok(result)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @GetMapping(Uris.ReportUris.GET_ALL_REPORTS_BY_TYPE)
    fun getAllReportsByType(@PathVariable reportType: String): ResponseEntity<*> =

        when (val reports = reportService.getAllReportsByType(reportType)) {
            is Success -> ResponseEntity.ok(reports.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(reports.value)
        }

}
