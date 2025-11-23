package com.aryandi.news.ui.category

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.aryandi.news.ui.source.EXTRA_CATEGORY_KEY
import com.aryandi.news.ui.source.SourceListActivity

@Composable
fun CategoryListScreen() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.systemBars,
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(
                listOf(
                    "business",
                    "entertainment",
                    "general",
                    "health",
                    "science",
                    "sports",
                    "technology"
                )
            ) { categoryName ->
                CategoryListItem(
                    categoryName,
                    onItemClick = { selectedCategory ->
                        val intent = Intent(context, SourceListActivity::class.java).apply {
                            putExtra(EXTRA_CATEGORY_KEY, selectedCategory)
                        }
                        launcher.launch(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun CategoryListItem(categoryName: String, onItemClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                onItemClick(categoryName)
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(categoryName)
    }
}