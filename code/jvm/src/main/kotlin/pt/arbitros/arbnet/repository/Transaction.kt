package pt.arbitros.arbnet.repository

interface Transaction {
    val usersRepository: UsersRepository
    val callListRepository: CallListRepository
    val competitionRepository: CompetitionRepository

    fun rollback()
}
