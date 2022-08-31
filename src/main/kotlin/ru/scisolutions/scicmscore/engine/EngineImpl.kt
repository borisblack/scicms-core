package ru.scisolutions.scicmscore.engine

import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.handler.CreateHandler
import ru.scisolutions.scicmscore.engine.handler.CreateLocalizationHandler
import ru.scisolutions.scicmscore.engine.handler.CreateVersionHandler
import ru.scisolutions.scicmscore.engine.handler.CustomMethodHandler
import ru.scisolutions.scicmscore.engine.handler.DeleteHandler
import ru.scisolutions.scicmscore.engine.handler.FindAllHandler
import ru.scisolutions.scicmscore.engine.handler.FindOneHandler
import ru.scisolutions.scicmscore.engine.handler.LockHandler
import ru.scisolutions.scicmscore.engine.handler.MediaHandler
import ru.scisolutions.scicmscore.engine.handler.PromoteHandler
import ru.scisolutions.scicmscore.engine.handler.PurgeHandler
import ru.scisolutions.scicmscore.engine.handler.UpdateHandler
import ru.scisolutions.scicmscore.engine.handler.UserHandler
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.MediaInfo
import ru.scisolutions.scicmscore.engine.model.input.CreateInput
import ru.scisolutions.scicmscore.engine.model.input.CreateLocalizationInput
import ru.scisolutions.scicmscore.engine.model.input.CreateVersionInput
import ru.scisolutions.scicmscore.engine.model.input.CustomMethodInput
import ru.scisolutions.scicmscore.engine.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.model.input.FindAllInput
import ru.scisolutions.scicmscore.engine.model.input.FindAllRelationInput
import ru.scisolutions.scicmscore.engine.model.input.PromoteInput
import ru.scisolutions.scicmscore.engine.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.model.input.UploadInput
import ru.scisolutions.scicmscore.engine.model.response.FlaggedResponse
import ru.scisolutions.scicmscore.engine.model.response.RelationResponse
import ru.scisolutions.scicmscore.engine.model.response.RelationResponseCollection
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollection
import ru.scisolutions.scicmscore.model.UserInfo
import java.util.UUID

/**
 * General facade for all operations with data
 */
@Service
class EngineImpl(
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
) : Engine {
    override fun me(): UserInfo? = userHandler.me()

    override fun upload(uploadInput: UploadInput): MediaInfo = mediaHandler.upload(uploadInput)

    override fun uploadMultiple(uploadInputList: List<UploadInput>): List<MediaInfo> = mediaHandler.uploadMultiple(uploadInputList)

    override fun downloadById(id: UUID): ByteArrayResource = mediaHandler.downloadById(id)

    override fun findOne(itemName: String, id: UUID, selectAttrNames: Set<String>): Response =
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
        if (itemName == MEDIA_ITEM_NAME)
            mediaHandler.deleteById(input.id)

        return deleteHandler.delete(itemName, input, selectAttrNames)
    }

    override fun purge(itemName: String, input: DeleteInput, selectAttrNames: Set<String>): ResponseCollection =
        purgeHandler.purge(itemName, input, selectAttrNames)

    override fun lock(itemName: String, id: UUID, selectAttrNames: Set<String>): FlaggedResponse =
        lockHandler.lock(itemName, id, selectAttrNames)

    override fun unlock(itemName: String, id: UUID, selectAttrNames: Set<String>): FlaggedResponse =
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