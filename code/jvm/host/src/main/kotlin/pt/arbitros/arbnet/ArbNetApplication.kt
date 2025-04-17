package pt.arbitros.arbnet

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.transaction.TransactionManager
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@RestController
class ArbNetApplication {
//    @Bean
//    fun jdbi() =
//        Jdbi
//            .create(
//                PGSimpleDataSource().apply {
//                    setURL(Environment.getDbUrl())
//                },
//            ).configureWithAppRequirements()
//
//    @Bean
//    @Profile("jdbi")
//    fun trxManagerJdbi(jdbi: Jdbi): TransactionManager = TransactionManagerJdbi(jdbi)


}
fun main(args: Array<String>) {
    runApplication<ArbNetApplication>(*args)
}


