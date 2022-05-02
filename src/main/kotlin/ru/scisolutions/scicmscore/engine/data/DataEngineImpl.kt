package ru.scisolutions.scicmscore.engine.data

import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.scisolutions.scicmscore.engine.data.handler.CustomMethodHandler
import ru.scisolutions.scicmscore.engine.data.handler.MediaHandler
import ru.scisolutions.scicmscore.engine.data.handler.ResponseCollectionHandler
import ru.scisolutions.scicmscore.engine.data.handler.ResponseHandler
import ru.scisolutions.scicmscore.engine.data.handler.UserHandler
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.MediaInfo
import ru.scisolutions.scicmscore.engine.data.model.UserInfo
import ru.scisolutions.scicmscore.engine.data.model.input.CustomMethodInput
import ru.scisolutions.scicmscore.engine.data.model.input.ResponseCollectionInput
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponse
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection
import ru.scisolutions.scicmscore.persistence.entity.Media

@Service
class DataEngineImpl(
    private val userHandler: UserHandler,
    private val mediaHandler: MediaHandler,
    private val responseHandler: ResponseHandler,
    private val responseCollectionHandler: ResponseCollectionHandler,
    private val customMethodHandler: CustomMethodHandler
) : DataEngine {
    override fun me(): UserInfo? = userHandler.me()

    override fun upload(file: MultipartFile): MediaInfo = mediaHandler.upload(file)

    override fun uploadMultiple(files: List<MultipartFile>): List<MediaInfo> = mediaHandler.uploadMultiple(files)

    override fun download(media: Media): ByteArrayResource = mediaHandler.download(media)

    override fun getResponse(itemName: String, selectAttrNames: Set<String>, id: String): Response =
        responseHandler.getResponse(itemName, selectAttrNames, id)

    override fun getRelationResponse(itemName: String, selectAttrNames: Set<String>, sourceItemRec: ItemRec, attrName: String): RelationResponse =
        responseHandler.getRelationResponse(itemName, selectAttrNames, sourceItemRec, attrName)

    override fun getResponseCollection(itemName: String, selectAttrNames: Set<String>, input: ResponseCollectionInput): ResponseCollection =
        responseCollectionHandler.getResponseCollection(itemName, selectAttrNames, input)

    override fun getCustomMethods(itemName: String): Set<String> = customMethodHandler.getCustomMethods(itemName)

    override fun callCustomMethod(itemName: String, methodName: String, customMethodInput: CustomMethodInput) =
        customMethodHandler.callCustomMethod(itemName, methodName, customMethodInput)
}