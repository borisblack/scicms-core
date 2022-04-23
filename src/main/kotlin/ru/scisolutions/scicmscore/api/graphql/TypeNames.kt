package ru.scisolutions.scicmscore.api.graphql

import graphql.language.TypeName

object TypeNames {
    val ID = TypeName("ID")
    val INT = TypeName("Int")
    val FLOAT = TypeName("Float")
    val STRING = TypeName("String")
    val BOOLEAN = TypeName("Boolean")
    val DATE = TypeName("Date")
    val TIME = TypeName("Time")
    val DATETIME = TypeName("DateTime")
    val OBJECT = TypeName("Object")
    val JSON = TypeName("JSON")
    val UPLOAD = TypeName("Upload")

    val ID_FILTER_INPUT = TypeName("IDFilterInput")
    val INT_FILTER_INPUT = TypeName("IntFilterInput")
    val FLOAT_FILTER_INPUT = TypeName("FloatFilterInput")
    val STRING_FILTER_INPUT = TypeName("StringFilterInput")
    val BOOLEAN_FILTER_INPUT = TypeName("BooleanFilterInput")
    val DATE_FILTER_INPUT = TypeName("DateFilterInput")
    val TIME_FILTER_INPUT = TypeName("TimeFilterInput")
    val DATETIME_FILTER_INPUT = TypeName("DateTimeFilterInput")
    val JSON_FILTER_INPUT = TypeName("JSONFilterInput")
}