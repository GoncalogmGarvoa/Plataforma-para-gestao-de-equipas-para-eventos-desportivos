package pt.arbitros.arbnet

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@RestController
class ArbNetApplication

fun main(args: Array<String>) {
    runApplication<ArbNetApplication>(*args)
}
