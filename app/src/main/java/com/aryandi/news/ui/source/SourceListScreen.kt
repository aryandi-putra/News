package com.aryandi.news.ui.source

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aryandi.data.model.Source
import com.aryandi.data.network.ApiResponse

@Composable
fun SourceListScreen(viewModel: SourceListViewModel = hiltViewModel()) {
    val sourceList by viewModel.sourceList.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        modifier = Modifier.fillMaxSize(),
        ) { padding ->
        when (sourceList) {
            is ApiResponse.Success -> {
                LazyColumn(contentPadding = padding) {
                    items(
                        (sourceList as? ApiResponse.Success<List<Source>>)?.data ?: emptyList()
                    ) { source ->
                        source.SourceListItem()
                    }
                }
            }
            is ApiResponse.Error -> {
                LoadStatusText(
                    modifier = Modifier.padding(padding),
                    "Error loading data: ${(sourceList as? ApiResponse.Error)?.message ?: "Unknown Error"}"
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
fun Source.SourceListItem() {
    Card(modifier = Modifier.padding(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = name ?: "", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(text = description ?: "", style = MaterialTheme.typography.bodyMedium)
        }
    }
}