package ru.scisolutions.scicmscore.engine.data

import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.scisolutions.scicmscore.engine.data.handler.CreateHandler
import ru.scisolutions.scicmscore.engine.data.handler.CustomMethodHandler
import ru.scisolutions.scicmscore.engine.data.handler.MediaHandler
import ru.scisolutions.scicmscore.engine.data.handler.FindAllHandler
import ru.scisolutions.scicmscore.engine.data.handler.FindOneHandler
import ru.scisolutions.scicmscore.engine.data.handler.UserHandler
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.MediaInfo
import ru.scisolutions.scicmscore.engine.data.model.UserInfo
import ru.scisolutions.scicmscore.engine.data.model.input.CustomMethodInput
import ru.scisolutions.scicmscore.engine.data.model.input.ItemInput
import ru.scisolutions.scicmscore.engine.data.model.input.FindAllRelationInput
import ru.scisolutions.scicmscore.engine.data.model.input.FindAllInput
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponse
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponseCollection
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection
import ru.scisolutions.scicmscore.persistence.entity.Media

/**
 * General facade for all operations with data
 */
@Service
class DataEngineImpl(
    private val userHandler: UserHandler,
    private val mediaHandler: MediaHandler,
    private val findOneHandler: FindOneHandler,
    private val findAllHandler: FindAllHandler,
    private val createHandler: CreateHandler,
    private val customMethodHandler: CustomMethodHandler
) : DataEngine {
    override fun me(): UserInfo? = userHandler.me()

    override fun upload(file: MultipartFile): MediaInfo = mediaHandler.upload(file)

    override fun uploadMultiple(files: List<MultipartFile>): List<MediaInfo> = mediaHandler.uploadMultiple(files)

    override fun download(media: Media): ByteArrayResource = mediaHandler.download(media)

    override fun getResponse(itemName: String, id: String, selectAttrNames: Set<String>): Response =
        findOneHandler.getResponse(itemName, id, selectAttrNames)

    override fun getRelationResponse(
        parentItemName: String,
        itemName: String,
        sourceItemRec: ItemRec,
        attrName: String,
        selectAttrNames: Set<String>
    ): RelationResponse =
        findOneHandler.getRelationResponse(parentItemName, itemName, sourceItemRec, attrName, selectAttrNames)

    override fun getResponseCollection(
        itemName: String,
        input: FindAllInput,
        selectAttrNames: Set<String>,
        selectPaginationFields: Set<String>
    ): ResponseCollection =
        findAllHandler.getResponseCollection(itemName, input, selectAttrNames, selectPaginationFields)

    override fun getRelationResponseCollection(
        parentItemName: String,
        itemName: String,
        sourceItemRec: ItemRec,
        attrName: String,
        input: FindAllRelationInput,
        selectAttrNames: Set<String>,
        selectPaginationFields: Set<String>
    ): RelationResponseCollection =
        findAllHandler.getRelationResponseCollection(parentItemName, itemName, sourceItemRec, attrName, input, selectAttrNames, selectPaginationFields)

    override fun create(itemName: String, input: ItemInput, selectAttrNames: Set<String>): Response =
        createHandler.create(itemName, input, selectAttrNames)

    override fun getCustomMethods(itemName: String): Set<String> = customMethodHandler.getCustomMethods(itemName)

    override fun callCustomMethod(itemName: String, methodName: String, customMethodInput: CustomMethodInput) =
        customMethodHandler.callCustomMethod(itemName, methodName, customMethodInput)
}