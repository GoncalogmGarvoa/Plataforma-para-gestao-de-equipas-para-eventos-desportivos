package pt.arbitros.arbnet.repository

interface Transaction {

    val usersRepository: UsersRepository
    fun rollback()

}

