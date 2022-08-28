package ru.scisolutions.scicmscore.engine.handler.util

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.LocationService
import ru.scisolutions.scicmscore.model.Attribute.Type as AttrType

@Component
class DeleteLocationHelper(
    private val locationService: LocationService
) {
    fun processLocations(item: Item, itemRec: ItemRec) {
        item.spec.attributes.asSequence()
            .filter { (attrName, attribute) -> attribute.type == AttrType.location && itemRec[attrName] != null }
            .forEach { (attrName, _) ->
                val locationId = itemRec[attrName] as String
                deleteLocationById(locationId)
            }
    }

    private fun deleteLocationById(id: String) {
        val location = locationService.findByIdForDelete(id)
        if (location == null) {
            logger.warn("Cannot delete location with ID [$id] (not found).", id)
            return
        }

        locationService.delete(location)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DeleteLocationHelper::class.java)
    }
}