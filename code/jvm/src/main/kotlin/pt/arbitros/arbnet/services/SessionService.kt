package pt.arbitros.arbnet.services


import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import pt.arbitros.arbnet.domain.SessionReferee
import pt.arbitros.arbnet.domain.UtilsDomain
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.http.model.SessionRefereeInputModel
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.transactionRepo

@Component
class SessionService(
    @Qualifier(transactionRepo) private val transactionManager: TransactionManager,
    private val utilsDomain: UtilsDomain
) {

    fun finishSession(
        sessionId: Int,
    ): Either<ApiError, Boolean> =
        transactionManager.run {
            it.sessionsRepository.getSessionById(sessionId)
                ?: return@run failure(ApiError.NotFound(
                    "Session not found",
                    "No session found with the provided ID"
                ))
            val done = it.sessionsRepository.finishSession(sessionId)
            success(done)
        }

    fun updateSessionReferees(sessionReferees: List<SessionRefereeInputModel>)
    : Either<ApiError, Boolean> =
        transactionManager.run {

            val userIds = sessionReferees.map{
                it.userId
            }
            it.usersRepository.getUsersAndCheckIfReferee(userIds)

            val sessionRefereesFull = sessionReferees.map{ sessionReferee ->
                val session = it.sessionsRepository.getSessionById(sessionReferee.sessionId)
                    ?: return@run failure(ApiError.NotFound(
                        "Session not found",
                        "No session found with the provided ID"
                    ))

                it.usersRepository.getUserById(sessionReferee.userId)
                    ?: return@run failure(ApiError.NotFound(
                        "User not found",
                        "No user found with the provided ID"
                    ))

                it.positionRepository.getPositionById(sessionReferee.positionId)
                    ?: return@run failure(ApiError.NotFound(
                        "Position not found",
                        "No position found with the provided ID"
                    ))

                SessionReferee(
                    session.id,
                    sessionReferee.positionId,
                    sessionReferee.userId,
                    session.matchDayId,
                    session.competitionIdMatchDay,
                )
            }

            val done = it.sessionRefereesRepository.updateSessionReferees(sessionRefereesFull)
            success(done)
    }
}


