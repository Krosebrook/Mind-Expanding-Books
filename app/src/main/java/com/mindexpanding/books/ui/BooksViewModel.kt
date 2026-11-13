package com.mindexpanding.books.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mindexpanding.books.data.BookRepository
import com.mindexpanding.books.model.AppScreen
import com.mindexpanding.books.model.Book
import com.mindexpanding.books.model.Category
import com.mindexpanding.books.model.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BooksUiState(
    val isLoading: Boolean = true,
    val allBooks: List<Book> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val sortOption: SortOption = SortOption.RATING_DESC,
    val searchQuery: String = "",
    val bookmarkedKeys: Set<String> = emptySet(),
    val currentScreen: AppScreen = AppScreen.Feed,
    val visibleLimit: Int = PAGE_SIZE
) {
    val filteredAndSortedBooks: List<Book>
        get() {
            val categoryFiltered = if (selectedCategory == null) {
                allBooks
            } else {
                allBooks.filter { it.category.equals(selectedCategory.name, ignoreCase = true) }
            }

            val query = searchQuery.trim()
            val searchFiltered = if (query.isEmpty()) {
                categoryFiltered
            } else {
                categoryFiltered.filter { book ->
                    book.title.contains(query, ignoreCase = true) ||
                        book.author.contains(query, ignoreCase = true) ||
                        book.category.contains(query, ignoreCase = true)
                }
            }

            return when (sortOption) {
                SortOption.RATING_DESC -> searchFiltered.sortedByDescending { it.rating }
                SortOption.RATING_ASC -> searchFiltered.sortedBy { it.rating }
                SortOption.YEAR_DESC -> searchFiltered.sortedByDescending { it.yearInt }
                SortOption.YEAR_ASC -> searchFiltered.sortedWith(
                    compareBy<Book> { if (it.yearInt == 0) Int.MAX_VALUE else it.yearInt }
                )
                SortOption.TITLE_A_Z -> searchFiltered.sortedBy { it.title.lowercase() }
                SortOption.TITLE_Z_A -> searchFiltered.sortedByDescending { it.title.lowercase() }
            }
        }

    val displayedBooks: List<Book>
        get() = filteredAndSortedBooks.take(visibleLimit)

    val bookmarkedBooks: List<Book>
        get() = allBooks
            .filter { bookmarkKey(it) in bookmarkedKeys }
            .distinctBy { bookmarkKey(it) }

    companion object {
        const val PAGE_SIZE = 12

        fun bookmarkKey(book: Book): String =
            "${book.title.lowercase()}|${book.author.lowercase()}"
    }
}

class BooksViewModel(
    private val repository: BookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BooksUiState())
    val uiState: StateFlow<BooksUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val categories = repository.loadCategories()
            val books = repository.loadBooks()
            val bookmarks = repository.getBookmarkedKeys()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    allBooks = books,
                    categories = categories,
                    bookmarkedKeys = bookmarks
                )
            }
        }
    }

    fun selectCategory(category: Category?) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                currentScreen = AppScreen.Feed,
                visibleLimit = BooksUiState.PAGE_SIZE
            )
        }
    }

    fun selectSortOption(option: SortOption) {
        _uiState.update {
            it.copy(
                sortOption = option,
                visibleLimit = BooksUiState.PAGE_SIZE
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                visibleLimit = BooksUiState.PAGE_SIZE
            )
        }
    }

    fun loadMoreBooks() {
        _uiState.update { state ->
            val total = state.filteredAndSortedBooks.size
            if (state.visibleLimit < total) {
                state.copy(visibleLimit = (state.visibleLimit + BooksUiState.PAGE_SIZE).coerceAtMost(total))
            } else {
                state
            }
        }
    }

    fun toggleBookmark(book: Book) {
        val key = BooksUiState.bookmarkKey(book)
        _uiState.update { state ->
            val updated = if (key in state.bookmarkedKeys) {
                state.bookmarkedKeys - key
            } else {
                state.bookmarkedKeys + key
            }
            repository.saveBookmarkedKeys(updated)
            state.copy(bookmarkedKeys = updated)
        }
    }

    fun openReadingList() {
        _uiState.update { it.copy(currentScreen = AppScreen.ReadingList) }
    }

    fun openFeed() {
        _uiState.update { it.copy(currentScreen = AppScreen.Feed) }
    }

    class Factory(private val repository: BookRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BooksViewModel(repository) as T
        }
    }
}
