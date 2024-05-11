package ru.scisolutions.scicmscore.engine.model.response

import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec

class FlaggedResponse(
    val success: Boolean,
    val data: ItemRec? = null
)