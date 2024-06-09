package ru.scisolutions.scicmscore.engine

import org.springframework.core.io.ByteArrayResource
import org.springframework.web.multipart.MultipartFile
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.MediaInfo
import ru.scisolutions.scicmscore.engine.model.input.CreateInput
import ru.scisolutions.scicmscore.engine.model.input.CreateLocalizationInput
import ru.scisolutions.scicmscore.engine.model.input.CreateVersionInput
import ru.scisolutions.scicmscore.engine.model.input.CustomMethodInput
import ru.scisolutions.scicmscore.engine.model.input.DatasetInput
import ru.scisolutions.scicmscore.engine.model.input.DatasourceTablesInput
import ru.scisolutions.scicmscore.engine.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.model.input.FindAllInput
import ru.scisolutions.scicmscore.engine.model.input.FindAllRelationInput
import ru.scisolutions.scicmscore.engine.model.input.PromoteInput
import ru.scisolutions.scicmscore.engine.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.model.input.UploadInput
import ru.scisolutions.scicmscore.engine.model.response.CustomMethodResponse
import ru.scisolutions.scicmscore.engine.model.response.DatasetResponse
import ru.scisolutions.scicmscore.engine.model.response.DatasourceTablesResponse
import ru.scisolutions.scicmscore.engine.model.response.FlaggedResponse
import ru.scisolutions.scicmscore.engine.model.response.RelationResponse
import ru.scisolutions.scicmscore.engine.model.response.RelationResponseCollection
import ru.scisolutions.scicmscore.engine.model.response.Response
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollection
import ru.scisolutions.scicmscore.engine.model.response.SessionDataResponse
import ru.scisolutions.scicmscore.engine.model.ChangePasswordRequest
import ru.scisolutions.scicmscore.engine.model.RegistrationRequest
import ru.scisolutions.scicmscore.engine.model.response.TokenResponse
import ru.scisolutions.scicmscore.engine.model.UserInfo

/**
 * General facade for all operations with data
 */
interface Engine {
    fun registerUser(registrationRequest: RegistrationRequest): TokenResponse

    fun changePassword(changePasswordRequest: ChangePasswordRequest)

    fun me(): UserInfo?

    fun updateSessionData(sessionData: Map<String, Any?>?): SessionDataResponse

    fun upload(file: MultipartFile): MediaInfo

    fun uploadData(uploadInput: UploadInput): MediaInfo

    fun uploadMultiple(files: List<MultipartFile>): List<MediaInfo>

    fun uploadDataMultiple(uploadInputList: List<UploadInput>): List<MediaInfo>

    fun downloadById(id: String): ByteArrayResource

    fun findOne(itemName: String, id: String, selectAttrNames: Set<String>): Response

    fun findOneRelated(
        parentItemName: String,
        parentItemRec: ItemRec,
        parentAttrName: String,
        itemName: String,
        selectAttrNames: Set<String>
    ): RelationResponse

    fun findAll(
        itemName: String,
        input: FindAllInput,
        selectAttrNames: Set<String>,
        selectPaginationFields: Set<String>
    ): ResponseCollection

    fun findAllRelated(
        parentItemName: String,
        parentItemRec: ItemRec,
        parentAttrName: String,
        itemName: String,
        input: FindAllRelationInput,
        selectAttrNames: Set<String>,
        selectPaginationFields: Set<String>
    ): RelationResponseCollection

    fun create(itemName: String, input: CreateInput, selectAttrNames: Set<String>): Response

    fun createVersion(itemName: String, input: CreateVersionInput, selectAttrNames: Set<String>): Response

    fun createLocalization(itemName: String, input: CreateLocalizationInput, selectAttrNames: Set<String>): Response

    fun update(itemName: String, input: UpdateInput, selectAttrNames: Set<String>): Response

    fun delete(itemName: String, input: DeleteInput, selectAttrNames: Set<String>): Response

    fun purge(itemName: String, input: DeleteInput, selectAttrNames: Set<String>): ResponseCollection

    fun lock(itemName: String, id: String, selectAttrNames: Set<String>): FlaggedResponse

    fun unlock(itemName: String, id: String, selectAttrNames: Set<String>): FlaggedResponse

    fun promote(itemName: String, input: PromoteInput, selectAttrNames: Set<String>): Response

    fun getCustomMethods(itemName: String): Set<String>

    fun callCustomMethod(itemName: String, methodName: String, customMethodInput: CustomMethodInput): CustomMethodResponse

    fun loadDataset(datasetName: String, input: DatasetInput): DatasetResponse

    fun loadDatasourceTables(datasourceName: String, input: DatasourceTablesInput): DatasourceTablesResponse
}