package ru.scisolutions.scicmscore.engine.model

enum class FieldType {
    uuid,
    string,
    text,
    enum,
    sequence,
    email,
    password,
    int,
    long,
    float,
    double,
    decimal,
    date,
    time,
    datetime,
    timestamp,
    bool,
    array,
    json,
    media,
    relation,
    unknown
}
