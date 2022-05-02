package ru.scisolutions.scicmscore.engine.data

import org.springframework.core.io.ByteArrayResource
import org.springframework.web.multipart.MultipartFile
import ru.scisolutions.scicmscore.engine.data.model.input.CustomMethodInput
import ru.scisolutions.scicmscore.engine.data.model.response.CustomMethodResponse
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.MediaInfo
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponse
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection
import ru.scisolutions.scicmscore.engine.data.model.UserInfo
import ru.scisolutions.scicmscore.engine.data.model.input.ResponseCollectionInput
import ru.scisolutions.scicmscore.persistence.entity.Media

interface DataEngine {
    fun me(): UserInfo?

    fun upload(file: MultipartFile): MediaInfo

    fun uploadMultiple(files: List<MultipartFile>): List<MediaInfo>

    fun download(media: Media): ByteArrayResource

    fun getResponse(itemName: String, selectAttrNames: Set<String>, id: String): Response

    fun getRelationResponse(itemName: String, selectAttrNames: Set<String>, sourceItemRec: ItemRec, attrName: String): RelationResponse

    fun getResponseCollection(itemName: String, selectAttrNames: Set<String>, input: ResponseCollectionInput): ResponseCollection

    fun getCustomMethods(itemName: String): Set<String>

    fun callCustomMethod(itemName: String, methodName: String, customMethodInput: CustomMethodInput): CustomMethodResponse
}