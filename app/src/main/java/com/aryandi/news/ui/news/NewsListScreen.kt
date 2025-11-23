package com.aryandi.news.ui.news

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.aryandi.data.model.Article
import com.aryandi.data.network.ApiResponse
import com.aryandi.news.ui.LoadStatusText

@Composable
fun NewsListScreen(viewModel: NewsListViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
        }
    }

    val newsList by viewModel.newsList.collectAsState()
    val listState = rememberLazyListState() // 1. Create list state

    // 2. Detect end of list
    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) {
                false
            } else {
                val lastVisibleItemIndex = visibleItemsInfo.last().index
                lastVisibleItemIndex >= totalItemsNumber - 3 // Load when 3 items from bottom
            }
        }
    }

    // 3. Trigger load more
    LaunchedEffect(isAtBottom) {
        if (isAtBottom) {
            viewModel.loadMoreSources()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        // 4. Handle UI states
        // We extract the current list data safely regardless of Loading/Error state to keep list visible during pagination
        val currentData = (newsList as? ApiResponse.Success)?.data
            ?: emptyList()

        if (currentData.isNotEmpty()) {
            LazyColumn(
                state = listState, // Attach state
                contentPadding = padding
            ) {
                items(currentData) { article ->
                    article.NewsListItem(onItemClick = { id ->
//                        val intent = Intent(context, NewsListActivity::class.java).apply {
//                            putExtra(EXTRA_SOURCE_KEY, id)
//                        }
//                        launcher.launch(intent)
                    })
                }

                // Optional: Show spinner at bottom when loading next page
                // Note: Ideally pass a specific 'isPaginating' boolean from VM for this
                item {
                    if (newsList is ApiResponse.Loading && currentData.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        } else {
            // Handle initial empty states
            when (newsList) {
                is ApiResponse.Loading -> {
                    LoadStatusText(modifier = Modifier.padding(padding), "Loading sources..")
                }

                is ApiResponse.Error -> {
                    LoadStatusText(
                        modifier = Modifier.padding(padding),
                        "Error loading data: ${(newsList as? ApiResponse.Error)?.message ?: "Unknown Error"}"
                    )
                }

                else -> {} // Success but empty list handled above or empty view here
            }
        }
    }
}

@Composable
fun Article.NewsListItem(onItemClick: (String) -> Unit) {
    Card(modifier = Modifier.padding(16.dp)
        .clickable { onItemClick(url ?: "") }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(urlToImage)
                    .transformations(RoundedCornersTransformation(16.dp.value)).build(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentDescription = "news image",
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(Color.DarkGray),
                error = ColorPainter(Color.DarkGray)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = title ?: "", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(text = description ?: "", style = MaterialTheme.typography.bodyMedium)

            author?.let { author ->
                Spacer(Modifier.height(12.dp))
                Text(
                    "- $author",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}