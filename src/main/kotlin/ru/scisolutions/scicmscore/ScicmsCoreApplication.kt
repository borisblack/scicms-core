/*-
 SciCMS Core
 Copyright 2022 Boris Chernysh
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 */
package ru.scisolutions.scicmscore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["ru.scisolutions"])
class ScicmsCoreApplication

fun main(args: Array<String>) {
    runApplication<ScicmsCoreApplication>(*args)
}
