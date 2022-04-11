package ru.scisolutions.scicmscore.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.entity.Item

interface ItemRepository : CrudRepository<Item, String>