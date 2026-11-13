package com.mindexpanding.books.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookRaw(
    @SerialName("amazon_url") val amazonUrl: String? = null,
    val author: String = "",
    val category: String = "",
    val description: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val isbn: String? = null,
    val lang: String? = null,
    val pages: String? = null,
    val rating: String = "0.0",
    val title: String = "",
    val url: String = "",
    val year: String? = null
)

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val year: String,
    val yearInt: Int,
    val rating: Double,
    val ratingDisplay: String,
    val category: String,
    val description: String,
    val imageUrl: String,
    val goodreadsUrl: String,
    val amazonUrl: String?,
    val pages: String?
)

@Serializable
data class Category(
    val name: String,
    val emoji: String
)

enum class SortOption(val label: String) {
    RATING_DESC("Rating, high to low"),
    RATING_ASC("Rating, low to high"),
    YEAR_DESC("Publication year, new to old"),
    YEAR_ASC("Publication year, old to new"),
    TITLE_A_Z("Title, A-Z"),
    TITLE_Z_A("Title, Z-A")
}

sealed interface AppScreen {
    data object Feed : AppScreen
    data object ReadingList : AppScreen
}
