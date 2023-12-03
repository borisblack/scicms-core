package ru.scisolutions.scicmscore.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            this.allowedOriginPatterns = listOf("*")
            this.allowedMethods = listOf("*")
            this.allowedHeaders = listOf("*")
            this.allowCredentials = true
        }

        return UrlBasedCorsConfigurationSource().apply {
            this.registerCorsConfiguration("/**", configuration)
        }
    }
}