package pt.arbitros.arbnet.http

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController
import pt.arbitros.arbnet.services.Failure
import pt.arbitros.arbnet.services.SessionError
import pt.arbitros.arbnet.services.SessionService
import pt.arbitros.arbnet.services.Success

@RestController
class SessionController (
    private val sessionService: SessionService,
) {
    @PutMapping(Uris.SessionUris.FINISH_SESSION)
   fun finishSession(
       @PathVariable sessionId: Int
       ) {
        val result = sessionService.finishSession(sessionId)
        when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> when (result.value) {
                is SessionError.SessionNotFound -> Problem.SessionNotFound.response(HttpStatus.NOT_FOUND)
            }
        }
   }
}

