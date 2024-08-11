package ru.scisolutions.scicmscore.config

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import ru.scisolutions.scicmscore.config.props.SecurityProps
import ru.scisolutions.scicmscore.security.CustomLogoutHandler
import ru.scisolutions.scicmscore.security.JwtTokenService
import ru.scisolutions.scicmscore.security.filter.JwtTokenAuthenticationFilter
import ru.scisolutions.scicmscore.security.filter.Oauth2AccessTokenAuthenticationFilter
import ru.scisolutions.scicmscore.security.filter.UsernamePasswordAuthenticationFilter
import ru.scisolutions.scicmscore.security.provider.JwtTokenAuthenticationProvider
import ru.scisolutions.scicmscore.security.provider.Oauth2AccessCodeAuthenticationProvider
import ru.scisolutions.scicmscore.security.provider.UsernamePasswordAuthenticationProvider

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
class ApiSecurityConfig(
    private val securityProps: SecurityProps,
    private val usernamePasswordAuthenticationProvider: UsernamePasswordAuthenticationProvider,
    private val oauth2AccessCodeAuthenticationProvider: Oauth2AccessCodeAuthenticationProvider,
) {
    @Bean
    fun threadPoolTaskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 10
        executor.maxPoolSize = 100
        executor.queueCapacity = 50
        executor.setThreadNamePrefix("async-")
        return executor
    }

    @Bean
    fun taskExecutor(): DelegatingSecurityContextAsyncTaskExecutor = DelegatingSecurityContextAsyncTaskExecutor(threadPoolTaskExecutor())

    @Bean
    fun configureSecurity(http: HttpSecurity, authManager: AuthenticationManager): SecurityFilterChain {
        http
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/api/auth/local/register",
                    "/api/config/security",
                    "/graphiql/**",
                    // "/schema.json",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                )
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }
            .authenticationManager(authManager)
            .exceptionHandling { it.authenticationEntryPoint(authenticationEntryPoint()) }
            .csrf { it.disable() }
            .cors {}
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .logout {
                it.addLogoutHandler(logoutHandler())
                    .logoutSuccessHandler(HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
            }

        http
            .addFilterBefore(JwtTokenAuthenticationFilter(authManager), BasicAuthenticationFilter::class.java)
            .addFilterBefore(
                UsernamePasswordAuthenticationFilter(authManager, jwtTokenService(), securityProps),
                BasicAuthenticationFilter::class.java,
            )
            .addFilterBefore(
                Oauth2AccessTokenAuthenticationFilter(authManager, jwtTokenService(), securityProps),
                BasicAuthenticationFilter::class.java,
            )

        return http.build()
    }

    @Bean
    fun authenticationManager(http: HttpSecurity): AuthenticationManager {
        val authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder::class.java)
        authenticationManagerBuilder
            .authenticationProvider(jwtTokenAuthenticationProvider())
            .authenticationProvider(usernamePasswordAuthenticationProvider)
            .authenticationProvider(oauth2AccessCodeAuthenticationProvider)

        return authenticationManagerBuilder.build()
    }

    // @Bean
    // fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager =
    //     authenticationConfiguration.getAuthenticationManager()

    @Bean
    fun authenticationEntryPoint() = AuthenticationEntryPoint { _, response, _ ->
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is not authorized")
    }

    @Bean
    fun logoutHandler() = CustomLogoutHandler()

    @Bean
    fun jwtTokenService(): JwtTokenService = JwtTokenService(securityProps)

    @Bean
    fun jwtTokenAuthenticationProvider(): AuthenticationProvider = JwtTokenAuthenticationProvider(jwtTokenService())
}
