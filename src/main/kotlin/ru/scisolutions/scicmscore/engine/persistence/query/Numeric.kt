package ru.scisolutions.scicmscore.engine.persistence.query

object Numeric {
    fun isInt(source: String): Boolean = try {
        source.toInt()
        true
    } catch (e: NumberFormatException) {
        false
    }

    fun isLong(source: String): Boolean = try {
        source.toLong()
        true
    } catch (e: NumberFormatException) {
        false
    }

    fun isFloat(source: String): Boolean = try {
        source.toFloat()
        true
    } catch (e: NumberFormatException) {
        false
    }

    fun isDouble(source: String): Boolean = try {
        source.toDouble()
        true
    } catch (e: NumberFormatException) {
        false
    }
}
