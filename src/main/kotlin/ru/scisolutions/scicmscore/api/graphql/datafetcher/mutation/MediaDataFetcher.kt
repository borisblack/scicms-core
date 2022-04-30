package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import graphql.schema.DataFetchingEnvironment
import org.springframework.web.multipart.MultipartFile
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.model.MediaInfo

@DgsComponent
class MediaDataFetcher(
    private val dataEngine: DataEngine
) {
    @DgsMutation
    fun upload(dfe: DataFetchingEnvironment): MediaInfo {
        val file = dfe.getArgument<MultipartFile>("file")
        return dataEngine.upload(file)
    }

    @DgsMutation
    fun uploadMultiple(dfe: DataFetchingEnvironment): List<MediaInfo> {
        val files = dfe.getArgument<List<MultipartFile>>("files")
        return dataEngine.uploadMultiple(files)
    }
}