package pt.arbitros.arbnet

import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.repository.jdbi.configureWithAppRequirements

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
