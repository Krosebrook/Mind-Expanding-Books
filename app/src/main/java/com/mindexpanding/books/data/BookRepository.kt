package com.mindexpanding.books.data

import android.content.Context
import com.mindexpanding.books.model.Book
import com.mindexpanding.books.model.BookRaw
import com.mindexpanding.books.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class BookRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    suspend fun loadCategories(): List<Category> = withContext(Dispatchers.IO) {
        runCatching {
            val rawJson = context.assets.open("categories.json")
                .bufferedReader()
                .use { it.readText() }
            json.decodeFromString<List<Category>>(rawJson)
        }.getOrDefault(emptyList())
    }

    suspend fun loadBooks(): List<Book> = withContext(Dispatchers.IO) {
        runCatching {
            val rawJson = context.assets.open("books.json")
                .bufferedReader()
                .use { it.readText() }
            val rawBooks = json.decodeFromString<List<BookRaw>>(rawJson)
            rawBooks.mapIndexedNotNull { index, raw ->
                val desc = raw.description?.trim().orEmpty()
                if (desc.length < 10 || raw.title.isBlank()) {
                    null
                } else {
                    val cleanYear = raw.year?.trim().orEmpty()
                    val parsedRating = raw.rating.trim().toDoubleOrNull() ?: 0.0
                    val stableId = "${raw.title.trim()}|${raw.author.trim()}|${raw.category.trim()}|$index"
                    Book(
                        id = stableId,
                        title = raw.title.trim(),
                        author = raw.author.trim(),
                        year = cleanYear,
                        yearInt = cleanYear.toIntOrNull() ?: 0,
                        rating = parsedRating,
                        ratingDisplay = String.format("%.2f", parsedRating),
                        category = raw.category.trim(),
                        description = desc,
                        imageUrl = raw.imageUrl?.trim().orEmpty(),
                        goodreadsUrl = raw.url.trim(),
                        amazonUrl = raw.amazonUrl?.trim()?.takeIf { it.isNotEmpty() },
                        pages = raw.pages?.trim()?.takeIf { it.isNotEmpty() }
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun getBookmarkedKeys(): Set<String> {
        return prefs.getStringSet(KEY_BOOKMARKS, emptySet())?.toSet() ?: emptySet()
    }

    fun saveBookmarkedKeys(keys: Set<String>) {
        prefs.edit().putStringSet(KEY_BOOKMARKS, keys).apply()
    }

    companion object {
        private const val PREFS_NAME = "mind_expanding_books_prefs"
        private const val KEY_BOOKMARKS = "bookmarked_keys"
    }
}
