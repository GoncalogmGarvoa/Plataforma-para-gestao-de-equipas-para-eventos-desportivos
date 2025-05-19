package pt.arbitros.arbnet.http

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.arbitros.arbnet.domain.universal.Equipment
import pt.arbitros.arbnet.http.model.EquipmentSelectModel
import pt.arbitros.arbnet.services.EquipmentError
import pt.arbitros.arbnet.services.EquipmentService
import pt.arbitros.arbnet.services.Failure
import pt.arbitros.arbnet.services.Success

@RestController
class EquipmentController(
    private val equipmentService: EquipmentService,
) {
    //TODO verify this error

    @GetMapping(Uris.EquipmentUris.GET_EQUIPMENT)
    fun getEquipment(): ResponseEntity<*> =
        when (val result = equipmentService.getEquipment()) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure ->
                when (result.value) {
                    is EquipmentError.NotFound -> Problem.EquipmentNotFound.response(HttpStatus.NOT_FOUND)
                    else -> Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }

    @PostMapping(Uris.EquipmentUris.SELECT_EQUIPMENT)
    fun selectEquipment(
        @RequestBody equipmentSelectModel: EquipmentSelectModel,
    ): ResponseEntity<*> =
        when (val result = equipmentService.selectEquipment(equipmentSelectModel)) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure ->
                when (result.value) {
                    is EquipmentError.NotFound -> Problem.EquipmentNotFound.response(HttpStatus.NOT_FOUND)
                    else -> Problem.InternalError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
        }
}