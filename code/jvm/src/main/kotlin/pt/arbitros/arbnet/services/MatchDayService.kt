package pt.arbitros.arbnet.services

import pt.arbitros.arbnet.repository.TransactionManager

class MatchDayService(
    private val transactionManager: TransactionManager,
    // private val clock: Clock
)
