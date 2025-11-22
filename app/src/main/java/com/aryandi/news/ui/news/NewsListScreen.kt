package com.aryandi.news.ui.news

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun NewsListScreen(viewModel: NewsListViewModel= hiltViewModel()) {
    val newsList by viewModel.newsList.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        modifier = Modifier.fillMaxSize(),
        ) { padding ->
        when (newsList) {
            is ApiResponse.Success -> {
                LazyColumn(contentPadding = padding) {
                    items(
                        (newsList as? ApiResponse.Success<List<Article>>)?.data ?: emptyList()
                    ) { news ->
                        news.NewsListItem()
                    }
                }
            }
            is ApiResponse.Error -> {
                LoadStatusText(
                    modifier = Modifier.padding(padding),
                    "Error loading data: ${(newsList as? ApiResponse.Error)?.message ?: "Unknown Error"}"
                )
            }
            is ApiResponse.Loading -> {
                LoadStatusText(modifier = Modifier.padding(padding), "Loading news..")
            }
        }
    }
}

@Composable
fun LoadStatusText(modifier: Modifier, message: String) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Text(text = message)
    }
}

@Composable
fun Article.NewsListItem() {
    Card(modifier = Modifier.padding(16.dp)) {
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