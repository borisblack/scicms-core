package ru.scisolutions.scicmscore.config

import org.jdbi.v3.core.Jdbi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class PersistenceConfig(private val dataSource: DataSource) {
    @Bean
    fun jdbi(): Jdbi = Jdbi.create(dataSource)
}