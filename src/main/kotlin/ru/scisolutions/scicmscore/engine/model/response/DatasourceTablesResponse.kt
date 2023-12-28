package ru.scisolutions.scicmscore.engine.model.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import ru.scisolutions.scicmscore.engine.model.Table

@JsonInclude(Include.NON_NULL)
class DatasourceTablesResponse(
    val data: List<Table>,
    val meta: ResponseCollectionMeta = ResponseCollectionMeta()
)