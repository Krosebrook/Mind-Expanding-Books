# Mind Expanding Books (Android)

An Android application built with Kotlin and Jetpack Compose for browsing, sorting, searching, and bookmarking curated mind-expanding books across 18 categories.

## Features

- **Curated Book Feed**: Browse hundreds of curated books with cover artwork, author, publication year, 5-star Goodreads ratings, and expandable descriptions (`Show More` / `Show Less`).
- **18 Categories**: Filter books by category (Startups and Business, Philosophy And Psychology, History, Science and Medicine, Logic and Problem Solving, Economics, and more) via the navigation drawer or horizontal filter chips.
- **Sorting & Search**: Sort books by rating (high to low / low to high), publication year (new to old / old to new), or title (A-Z / Z-A), and filter by title, author, or category.
- **Personal Reading List**: Bookmark books (`🔖`) to save them locally to your persistent Reading List.
- **External Links**: Open book pages directly on Amazon or Goodreads.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (`BooksViewModel` + `BookRepository`)
- **Data & Persistence**: `kotlinx.serialization` (bundled `books.json` & `categories.json` in `assets/`) and `SharedPreferences` for persistent bookmarks
- **Image Loading**: Coil Compose
