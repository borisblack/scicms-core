package ru.scisolutions.scicmscore.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.scisolutions.scicmscore.schema.applier.ModelApplier
import ru.scisolutions.scicmscore.schema.applier.ModelsApplier
import ru.scisolutions.scicmscore.schema.applier.impl.ModelsApplierImpl

@Configuration
class ModelsApplierConfig(
    private val itemTemplateApplier: ModelApplier,
    private val itemApplier: ModelApplier
) {
    @Bean
    fun modelsApplier(): ModelsApplier = ModelsApplierImpl().apply {
        this.registerApplier(itemTemplateApplier)
        this.registerApplier(itemApplier)
    }
}