package com.mindexpanding.books

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindexpanding.books.data.BookRepository
import com.mindexpanding.books.ui.BooksViewModel
import com.mindexpanding.books.ui.MindExpandingBooksApp
import com.mindexpanding.books.ui.theme.MindExpandingBooksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = BookRepository(applicationContext)
        setContent {
            MindExpandingBooksTheme {
                val booksViewModel: BooksViewModel = viewModel(
                    factory = BooksViewModel.Factory(repository)
                )
                MindExpandingBooksApp(viewModel = booksViewModel)
            }
        }
    }
}
