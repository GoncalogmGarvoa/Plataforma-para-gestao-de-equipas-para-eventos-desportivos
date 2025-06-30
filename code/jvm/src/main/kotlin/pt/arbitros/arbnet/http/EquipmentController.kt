package pt.arbitros.arbnet.http

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import pt.arbitros.arbnet.domain.adaptable.Equipment
import pt.arbitros.arbnet.services.*


@RestController
class EquipmentController(
    private val equipmentService: EquipmentService,
    //private val usersService: UsersService,
) {

    @GetMapping(Uris.EquipmentUris.GET_ALL_EQUIPMENT)
    fun getAllEquipment(
    ): ResponseEntity<*> =
        when (val result: Either<ApiError, List<Equipment>> = equipmentService.getAllEquipment()) {
            is Success -> {
                val value = result.value
                ResponseEntity.ok(value)
            }
            is Failure -> Problem.fromApiErrorToProblemResponse(result.value)
        }


}
