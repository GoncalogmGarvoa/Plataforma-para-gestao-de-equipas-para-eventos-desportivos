package pt.arbitros.arbnet

import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import pt.arbitros.arbnet.repositoryJdbi.configureWithAppRequirements

@SpringBootApplication
class ArbNetApplication {
    @Bean
    fun jdbi() =
        Jdbi
            .create(
                PGSimpleDataSource().apply {
                    setURL(Environment.getDbUrl())
                },
            ).configureWithAppRequirements()
}

fun main(args: Array<String>) {
    runApplication<ArbNetApplication>(*args)
}

@Suppress("ktlint:standard:property-naming")
const val transactionRepo = "transactionManagerJdbi"
// transactionManagerMem
// transactionManagerJdbi
