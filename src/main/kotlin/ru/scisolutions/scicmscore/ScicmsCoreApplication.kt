package ru.scisolutions.scicmscore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * @author Boris Chernysh
 * 2022-01-03
 */
@SpringBootApplication
class ScicmsCoreApplication

fun main(args: Array<String>) {
    runApplication<ScicmsCoreApplication>(*args)
}
