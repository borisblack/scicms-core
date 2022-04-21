package ru.scisolutions.scicmscore.graphql

import graphql.language.TypeName

class TypeNames {
    companion object {
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
    }
}