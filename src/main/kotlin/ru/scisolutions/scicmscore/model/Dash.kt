package ru.scisolutions.scicmscore.model

data class Dash(
    val name: String = "",
    val displayName: String = name,
    val rows: Int = 0,
    val cols: Int = 0,
    val refreshIntervalSeconds: Int = DEFAULT_REFRESH_INTERVAL_SECONDS,
    val items: List<DashItem> = emptyList()
) {
    companion object {
        private const val DEFAULT_REFRESH_INTERVAL_SECONDS: Int = 300
    }
}