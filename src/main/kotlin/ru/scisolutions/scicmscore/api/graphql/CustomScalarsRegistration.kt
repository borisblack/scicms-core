package ru.scisolutions.scicmscore.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsRuntimeWiring
import graphql.scalars.ExtendedScalars
import graphql.schema.idl.RuntimeWiring
import java.util.regex.Pattern

@DgsComponent
class CustomScalarsRegistration {
    @DgsRuntimeWiring
    fun addScalars(builder: RuntimeWiring.Builder): RuntimeWiring.Builder =
        builder
            .scalar(
                ExtendedScalars.newRegexScalar("Email")
                    .addPattern(Pattern.compile("\\w+@\\w+\\.\\w+"))
                    .build()
            )
}