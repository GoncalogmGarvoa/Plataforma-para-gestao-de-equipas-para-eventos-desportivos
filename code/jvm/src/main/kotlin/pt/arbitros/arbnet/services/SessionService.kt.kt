package pt.arbitros.arbnet.services


import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.UtilsDomain
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.transactionRepo

sealed class SessionError {
    data object SessionNotFound : SessionError()
}

@Component
class SessionService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    private val utilsDomain: UtilsDomain
) {

    fun finishSession(
        sessionId: Int,
    ): Either<SessionError, Boolean> =
        transactionManager.run {
            it.sessionsRepository.getSessionById(sessionId)
                ?: return@run failure(SessionError.SessionNotFound)
            val done = it.sessionsRepository.finishSession(sessionId)
            success(done)
        }

    fun updateSessionReferees(

    ) {

    }
}


