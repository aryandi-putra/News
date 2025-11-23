package com.aryandi.news.ui.source

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aryandi.data.model.Source
import com.aryandi.data.network.ApiResponse
import com.aryandi.news.ui.LoadStatusText
import com.aryandi.news.ui.news.EXTRA_SOURCE_KEY
import com.aryandi.news.ui.news.NewsListActivity

@Composable
fun SourceListScreen(viewModel: SourceListViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
        }
    }

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
                        source.SourceListItem(onItemClick = { id ->
                            val intent = Intent(context, NewsListActivity::class.java).apply {
                                putExtra(EXTRA_SOURCE_KEY, id)
                            }
                            launcher.launch(intent)
                        })
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
fun Source.SourceListItem(onItemClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .clickable { onItemClick(id ?: "") }
            .padding(16.dp)) {
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