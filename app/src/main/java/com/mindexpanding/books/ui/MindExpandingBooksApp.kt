package com.mindexpanding.books.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindexpanding.books.R
import com.mindexpanding.books.model.AppScreen
import com.mindexpanding.books.model.Category
import com.mindexpanding.books.model.SortOption
import com.mindexpanding.books.ui.components.BookCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindExpandingBooksApp(
    viewModel: BooksViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(
        enabled = uiState.currentScreen == AppScreen.ReadingList || uiState.selectedCategory != null
    ) {
        if (uiState.currentScreen == AppScreen.ReadingList) {
            viewModel.openFeed()
        } else if (uiState.selectedCategory != null) {
            viewModel.selectCategory(null)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CategoryDrawerSheet(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                readingListCount = uiState.bookmarkedKeys.size,
                isReadingListSelected = uiState.currentScreen == AppScreen.ReadingList,
                onSelectAllBooks = {
                    viewModel.selectCategory(null)
                    scope.launch { drawerState.close() }
                },
                onSelectCategory = { category ->
                    viewModel.selectCategory(category)
                    scope.launch { drawerState.close() }
                },
                onOpenReadingList = {
                    viewModel.openReadingList()
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (uiState.currentScreen) {
                                AppScreen.ReadingList -> stringResource(R.string.reading_list_title)
                                AppScreen.Feed -> uiState.selectedCategory?.let { "${it.emoji} ${it.name}" }
                                    ?: stringResource(R.string.app_name)
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        if (uiState.currentScreen == AppScreen.ReadingList) {
                            IconButton(
                                onClick = { viewModel.openFeed() },
                                modifier = Modifier.testTag("back_to_home_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.reading_list_back)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("open_drawer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(R.string.categories_drawer_title)
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (uiState.currentScreen == AppScreen.ReadingList) {
                                    viewModel.openFeed()
                                } else {
                                    viewModel.openReadingList()
                                }
                            },
                            modifier = Modifier.testTag("reading_list_nav_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (uiState.bookmarkedKeys.isNotEmpty()) {
                                        Badge {
                                            Text(text = uiState.bookmarkedKeys.size.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = stringResource(R.string.reading_list_title)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.currentScreen == AppScreen.ReadingList -> {
                        ReadingListScreen(
                            uiState = uiState,
                            onBackToHome = { viewModel.openFeed() },
                            onToggleBookmark = { viewModel.toggleBookmark(it) }
                        )
                    }

                    else -> {
                        BookFeedScreen(
                            uiState = uiState,
                            onSelectCategory = { viewModel.selectCategory(it) },
                            onSelectSort = { viewModel.selectSortOption(it) },
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onToggleBookmark = { viewModel.toggleBookmark(it) },
                            onLoadMore = { viewModel.loadMoreBooks() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryDrawerSheet(
    categories: List<Category>,
    selectedCategory: Category?,
    readingListCount: Int,
    isReadingListSelected: Boolean,
    onSelectAllBooks: () -> Unit,
    onSelectCategory: (Category) -> Unit,
    onOpenReadingList: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )

            NavigationDrawerItem(
                label = {
                    Text(
                        text = "🔖 Reading List ($readingListCount)",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                selected = isReadingListSelected,
                onClick = onOpenReadingList,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .testTag("drawer_reading_list_item")
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            NavigationDrawerItem(
                label = { Text(text = "📚 " + stringResource(R.string.all_books)) },
                selected = !isReadingListSelected && selectedCategory == null,
                onClick = onSelectAllBooks,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .testTag("drawer_all_books_item")
            )

            categories.forEach { category ->
                NavigationDrawerItem(
                    label = { Text(text = "${category.emoji} ${category.name}") },
                    selected = !isReadingListSelected && selectedCategory?.name == category.name,
                    onClick = { onSelectCategory(category) },
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .testTag("drawer_category_${category.name}")
                )
            }
        }
    }
}

@Composable
private fun BookFeedScreen(
    uiState: BooksUiState,
    onSelectCategory: (Category?) -> Unit,
    onSelectSort: (SortOption) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleBookmark: (com.mindexpanding.books.model.Book) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 720.dp)
    ) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("search_books_input"),
            placeholder = { Text(stringResource(R.string.search_books_hint)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear_search)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = uiState.selectedCategory == null,
                    onClick = { onSelectCategory(null) },
                    label = { Text("📚 " + stringResource(R.string.all_books)) },
                    modifier = Modifier.testTag("chip_all_books")
                )
            }
            items(uiState.categories, key = { it.name }) { category ->
                FilterChip(
                    selected = uiState.selectedCategory?.name == category.name,
                    onClick = { onSelectCategory(category) },
                    label = { Text("${category.emoji} ${category.name}") },
                    modifier = Modifier.testTag("chip_category_${category.name}")
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                OutlinedButton(
                    onClick = { sortMenuExpanded = true },
                    modifier = Modifier.testTag("sort_by_dropdown_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.sort_by_prefix, uiState.sortOption.label),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                onSelectSort(option)
                                sortMenuExpanded = false
                            },
                            modifier = Modifier.testTag("sort_option_${option.name}")
                        )
                    }
                }
            }

            Text(
                text = "${uiState.filteredAndSortedBooks.size} books",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val displayed = uiState.displayedBooks
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("books_feed_list")
        ) {
            items(displayed, key = { it.id }) { book ->
                val isBookmarked = BooksUiState.bookmarkKey(book) in uiState.bookmarkedKeys
                BookCard(
                    book = book,
                    isBookmarked = isBookmarked,
                    onToggleBookmark = { onToggleBookmark(book) }
                )
            }

            if (uiState.visibleLimit < uiState.filteredAndSortedBooks.size) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        OutlinedButton(
                            onClick = onLoadMore,
                            modifier = Modifier.testTag("load_more_books_button")
                        ) {
                            Text(text = stringResource(R.string.load_more))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadingListScreen(
    uiState: BooksUiState,
    onBackToHome: () -> Unit,
    onToggleBookmark: (com.mindexpanding.books.model.Book) -> Unit
) {
    val bookmarkedBooks = uiState.bookmarkedBooks

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 720.dp)
            .padding(horizontal = 16.dp)
    ) {
        TextButton(
            onClick = onBackToHome,
            modifier = Modifier.testTag("reading_list_back_link")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = stringResource(R.string.reading_list_back))
        }

        if (bookmarkedBooks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔖",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        text = stringResource(R.string.reading_list_empty_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = stringResource(R.string.reading_list_empty_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("reading_list_items")
            ) {
                items(bookmarkedBooks, key = { it.id }) { book ->
                    BookCard(
                        book = book,
                        isBookmarked = true,
                        onToggleBookmark = { onToggleBookmark(book) }
                    )
                }
            }
        }
    }
}
