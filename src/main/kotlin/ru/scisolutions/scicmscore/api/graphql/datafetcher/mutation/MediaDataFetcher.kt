package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import graphql.schema.DataFetchingEnvironment
import org.springframework.web.multipart.MultipartFile
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.model.MediaInfo
import ru.scisolutions.scicmscore.engine.model.input.UploadInput

@DgsComponent
class MediaDataFetcher(private val engine: Engine) {
    @DgsMutation
    fun upload(dfe: DataFetchingEnvironment): MediaInfo {
        val inputMap = dfe.getArgument<Map<String, Any?>>("input")
        val uploadInput = mapUploadInput(inputMap)
        return engine.upload(uploadInput)
    }

    @DgsMutation
    fun uploadMultiple(dfe: DataFetchingEnvironment): List<MediaInfo> {
        val inputMapList = dfe.getArgument<List<Map<String, Any?>>>("input")
        val uploadInputList = inputMapList.map { mapUploadInput(it) }
        return engine.uploadMultiple(uploadInputList)
    }

    private fun mapUploadInput(inputMap: Map<String, Any?>) =
        UploadInput(
            file = inputMap["file"] as MultipartFile,
            label = inputMap["label"] as String?,
            description = inputMap["description"] as String?,
            permissionId = inputMap["permission"] as String?,
        )
}