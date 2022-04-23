package ru.scisolutions.scicmscore.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import ru.scisolutions.scicmscore.config.props.JwtTokenProps
import ru.scisolutions.scicmscore.security.CustomLogoutHandler
import ru.scisolutions.scicmscore.security.JwtTokenService
import ru.scisolutions.scicmscore.security.filter.JwtTokenAuthenticationFilter
import ru.scisolutions.scicmscore.security.filter.UsernamePasswordAuthenticationFilter
import ru.scisolutions.scicmscore.security.provider.JwtTokenAuthenticationProvider
import ru.scisolutions.scicmscore.security.provider.UsernamePasswordAuthenticationProvider
import javax.servlet.Filter
import javax.servlet.http.HttpServletResponse

@Configuration
@EnableWebSecurity
class ApiSecurityConfig(
    private val jwtTokenProps: JwtTokenProps,
    private val usernamePasswordAuthenticationProvider: UsernamePasswordAuthenticationProvider
) : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http
            .authorizeRequests()
            .antMatchers("/graphiql"/*, "/schema.json"*/).permitAll()
            .anyRequest().authenticated()
            .and()
            .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint())
            .and()
            .csrf().disable()
            .cors()
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .logout()
            .addLogoutHandler(logoutHandler())
            .logoutSuccessHandler(HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))

        http
            .addFilterBefore(jwtTokenAuthenticationFilter(), BasicAuthenticationFilter::class.java)
            .addFilterBefore(usernamePasswordAuthenticationFilter(), BasicAuthenticationFilter::class.java)
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth
            .authenticationProvider(jwtTokenAuthenticationProvider())
            .authenticationProvider(usernamePasswordAuthenticationProvider)
    }

    @Bean
    fun authenticationEntryPoint() = AuthenticationEntryPoint { _, response, _ ->
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is not authorized")
    }

    @Bean
    fun logoutHandler() = CustomLogoutHandler()

    @Bean
    fun usernamePasswordAuthenticationFilter(): Filter =
        UsernamePasswordAuthenticationFilter(authenticationManager(), jwtTokenService())

    @Bean
    fun jwtTokenService(): JwtTokenService = JwtTokenService(jwtTokenProps)

    @Bean
    fun jwtTokenAuthenticationFilter(): Filter = JwtTokenAuthenticationFilter(authenticationManager())

    @Bean
    fun jwtTokenAuthenticationProvider(): AuthenticationProvider = JwtTokenAuthenticationProvider(jwtTokenService())
}