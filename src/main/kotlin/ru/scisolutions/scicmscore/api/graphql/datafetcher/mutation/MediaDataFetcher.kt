package ru.scisolutions.scicmscore.api.graphql.datafetcher.mutation

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import graphql.schema.DataFetchingEnvironment
import org.springframework.web.multipart.MultipartFile
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.model.MediaInfo

@DgsComponent
class MediaDataFetcher(private val engine: Engine) {
    @DgsMutation
    fun upload(dfe: DataFetchingEnvironment): MediaInfo {
        val file = dfe.getArgument<MultipartFile>("file")
        return engine.upload(file)
    }

    @DgsMutation
    fun uploadMultiple(dfe: DataFetchingEnvironment): List<MediaInfo> {
        val files = dfe.getArgument<List<MultipartFile>>("files")
        return engine.uploadMultiple(files)
    }
}