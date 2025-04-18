package pt.arbitros.arbnet.repository

interface TransactionManager {
    fun <R> run(block: (Transaction) -> R): R
}
