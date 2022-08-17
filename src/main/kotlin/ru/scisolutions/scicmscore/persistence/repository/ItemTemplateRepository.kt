package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.ItemTemplate

interface ItemTemplateRepository : CrudRepository<ItemTemplate, String> {
    fun findByName(name: String): ItemTemplate?
}