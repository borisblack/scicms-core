package ru.scisolutions.scicmscore.engine.data

import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.scisolutions.scicmscore.domain.model.UserInfo
import ru.scisolutions.scicmscore.engine.data.handler.CreateHandler
import ru.scisolutions.scicmscore.engine.data.handler.CreateLocalizationHandler
import ru.scisolutions.scicmscore.engine.data.handler.CreateVersionHandler
import ru.scisolutions.scicmscore.engine.data.handler.CustomMethodHandler
import ru.scisolutions.scicmscore.engine.data.handler.DeleteHandler
import ru.scisolutions.scicmscore.engine.data.handler.FindAllHandler
import ru.scisolutions.scicmscore.engine.data.handler.FindOneHandler
import ru.scisolutions.scicmscore.engine.data.handler.LockHandler
import ru.scisolutions.scicmscore.engine.data.handler.MediaHandler
import ru.scisolutions.scicmscore.engine.data.handler.PromoteHandler
import ru.scisolutions.scicmscore.engine.data.handler.PurgeHandler
import ru.scisolutions.scicmscore.engine.data.handler.UpdateHandler
import ru.scisolutions.scicmscore.engine.data.handler.UserHandler
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.MediaInfo
import ru.scisolutions.scicmscore.engine.data.model.input.CreateInput
import ru.scisolutions.scicmscore.engine.data.model.input.CreateLocalizationInput
import ru.scisolutions.scicmscore.engine.data.model.input.CreateVersionInput
import ru.scisolutions.scicmscore.engine.data.model.input.CustomMethodInput
import ru.scisolutions.scicmscore.engine.data.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.data.model.input.FindAllInput
import ru.scisolutions.scicmscore.engine.data.model.input.FindAllRelationInput
import ru.scisolutions.scicmscore.engine.data.model.input.PromoteInput
import ru.scisolutions.scicmscore.engine.data.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponse
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponseCollection
import ru.scisolutions.scicmscore.engine.data.model.response.Response
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection

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
    private val createVersionHandler: CreateVersionHandler,
    private val createLocalizationHandler: CreateLocalizationHandler,
    private val updateHandler: UpdateHandler,
    private val deleteHandler: DeleteHandler,
    private val purgeHandler: PurgeHandler,
    private val lockHandler: LockHandler,
    private val promoteHandler: PromoteHandler,
    private val customMethodHandler: CustomMethodHandler
) : DataEngine {
    override fun me(): UserInfo? = userHandler.me()

    override fun upload(file: MultipartFile): MediaInfo = mediaHandler.upload(file)

    override fun uploadMultiple(files: List<MultipartFile>): List<MediaInfo> = mediaHandler.uploadMultiple(files)

    override fun downloadById(id: String): ByteArrayResource = mediaHandler.downloadById(id)

    override fun findOne(itemName: String, id: String, selectAttrNames: Set<String>): Response =
        findOneHandler.findOne(itemName, id, selectAttrNames)

    override fun findOneRelated(
        parentItemName: String,
        parentItemRec: ItemRec,
        parentAttrName: String,
        itemName: String,
        selectAttrNames: Set<String>
    ): RelationResponse =
        findOneHandler.findOneRelated(
            parentItemName = parentItemName,
            parentItemRec = parentItemRec,
            parentAttrName = parentAttrName,
            itemName = itemName,
            selectAttrNames = selectAttrNames
        )

    override fun findAll(
        itemName: String,
        input: FindAllInput,
        selectAttrNames: Set<String>,
        selectPaginationFields: Set<String>
    ): ResponseCollection =
        findAllHandler.findAll(
            itemName = itemName,
            input = input,
            selectAttrNames = selectAttrNames,
            selectPaginationFields = selectPaginationFields
        )

    override fun findAllRelated(
        parentItemName: String,
        parentItemRec: ItemRec,
        parentAttrName: String,
        itemName: String,
        input: FindAllRelationInput,
        selectAttrNames: Set<String>,
        selectPaginationFields: Set<String>
    ): RelationResponseCollection =
        findAllHandler.findAllRelated(
            parentItemName = parentItemName,
            parentItemRec = parentItemRec,
            parentAttrName = parentAttrName,
            itemName = itemName,
            input = input,
            selectAttrNames = selectAttrNames,
            selectPaginationFields = selectPaginationFields
        )

    override fun create(itemName: String, input: CreateInput, selectAttrNames: Set<String>): Response =
        createHandler.create(itemName, input, selectAttrNames)

    override fun createVersion(itemName: String, input: CreateVersionInput, selectAttrNames: Set<String>): Response =
        createVersionHandler.createVersion(itemName, input, selectAttrNames)

    override fun createLocalization(itemName: String, input: CreateLocalizationInput, selectAttrNames: Set<String>): Response =
        createLocalizationHandler.createLocalization(itemName, input, selectAttrNames)

    override fun update(itemName: String, input: UpdateInput, selectAttrNames: Set<String>): Response =
        updateHandler.update(itemName, input, selectAttrNames)

    override fun delete(itemName: String, input: DeleteInput, selectAttrNames: Set<String>): Response {
        val result = deleteHandler.delete(itemName, input, selectAttrNames)

        if (itemName == MEDIA_ITEM_NAME)
            mediaHandler.deleteById(input.id)

        return result
    }

    override fun purge(itemName: String, input: DeleteInput, selectAttrNames: Set<String>): ResponseCollection =
        purgeHandler.purge(itemName, input, selectAttrNames)

    override fun lock(itemName: String, id: String, selectAttrNames: Set<String>): Response =
        lockHandler.lock(itemName, id, selectAttrNames)

    override fun unlock(itemName: String, id: String, selectAttrNames: Set<String>): Response =
        lockHandler.unlock(itemName, id, selectAttrNames)

    override fun promote(itemName: String, input: PromoteInput, selectAttrNames: Set<String>): Response =
        promoteHandler.promote(itemName, input, selectAttrNames)

    override fun getCustomMethods(itemName: String): Set<String> = customMethodHandler.getCustomMethods(itemName)

    override fun callCustomMethod(itemName: String, methodName: String, customMethodInput: CustomMethodInput) =
        customMethodHandler.callCustomMethod(itemName, methodName, customMethodInput)

    companion object {
        private const val MEDIA_ITEM_NAME = "media"
    }
}