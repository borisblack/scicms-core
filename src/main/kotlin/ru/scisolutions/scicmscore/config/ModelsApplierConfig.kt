package ru.scisolutions.scicmscore.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.scisolutions.scicmscore.schema.applier.ModelsApplier
import ru.scisolutions.scicmscore.schema.applier.impl.ItemApplier
import ru.scisolutions.scicmscore.schema.applier.impl.ModelsApplierImpl

@Configuration
class ModelsApplierConfig(
    private val itemApplier: ItemApplier
) {
    @Bean
    fun modelsApplier(): ModelsApplier = ModelsApplierImpl().apply {
        this.registerApplier(itemApplier)
    }
}