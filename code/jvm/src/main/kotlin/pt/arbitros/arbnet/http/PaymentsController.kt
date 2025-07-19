package pt.arbitros.arbnet.http

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import pt.arbitros.arbnet.http.model.payment_report.PaymentReportInputModel
import pt.arbitros.arbnet.services.Failure
import pt.arbitros.arbnet.services.payment.PaymentReportService
import pt.arbitros.arbnet.services.Success
import pt.arbitros.arbnet.services.UsersService

// TODO verify if useful -> @RestControllerAdvice
@RestController
class PaymentsController(
    private val paymentReportService : PaymentReportService,
    private val usersService: UsersService,
) {

    //TODO check errors they are not being handled,so i can test the controller

    @GetMapping(Uris.PaymentsUris.GET_CALL_LISTS_FOR_PAYMENT_BY_USER_ID)
    fun getPaymentsForUserId(
        @RequestHeader token: String
    ): ResponseEntity<*> {
        val userResult = usersService.getUserByToken(token)
        return if (userResult is Success) {
            when (
                val result = paymentReportService.getCallListsForPaymentByUserId(userResult.value.id)
            ) {
                is Success -> ResponseEntity.ok(result.value)
                is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
            }
        } else {
            Problem.fromApiErrorToProblemResponse(
                ApiError.NotFound(
                    "User not found or not authorized to get payments for user",
                )
            )
        }
    }

    @PostMapping(Uris.PaymentsUris.CREATE_PAYMENT_REPORT)
    fun createPaymentReport(@RequestBody report: PaymentReportInputModel): ResponseEntity<*> =
        when (val result = paymentReportService.createPaymentReport(report)) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }


    @GetMapping(Uris.PaymentsUris.GET_ALL_PAYMENT_REPORTS)
    fun getAllPaymentReports(): ResponseEntity<*> =
        when (val result = paymentReportService.getAllPaymentReports()) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @GetMapping(Uris.PaymentsUris.GET_PAYMENT_REPORTS_BY_COMPETITION)
    fun getPaymentReportByCompetition(@PathVariable competitionId: Int): ResponseEntity<*> =
        when (val result = paymentReportService.getPaymentReportByCompetition(competitionId)) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @GetMapping(Uris.PaymentsUris.GET_PAYMENT_REPORT_BY_ID)
    fun getPaymentReportById(@PathVariable id: String): ResponseEntity<*> =
        when (val result = paymentReportService.getPaymentReportById(id)) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @PutMapping(Uris.PaymentsUris.UPDATE_PAYMENT_REPORT)
    fun updatePaymentReport(@RequestBody report: PaymentReportInputModel): ResponseEntity<*> =
        when (val result = paymentReportService.updatePaymentReport(report)) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }

    @PutMapping(Uris.PaymentsUris.SEAL_PAYMENT_REPORT)
    fun sealPaymentReport(@PathVariable id: String): ResponseEntity<*> =
        when (val result = paymentReportService.sealPaymentReport(id)) {
            is Success -> ResponseEntity.ok(result)
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }
}
