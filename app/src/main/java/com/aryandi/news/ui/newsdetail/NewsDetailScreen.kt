package com.aryandi.news.ui.newsdetail

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

const val EXTRA_URL_KEY = "EXTRA_URL"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen() {
    val activity = LocalActivity.current
    val url = remember { activity?.intent?.getStringExtra(EXTRA_URL_KEY) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        if (!url.isNullOrEmpty()) {
            ArticleWebView(
                modifier = Modifier.padding(paddingValues),
                url = url,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ){
                Text("Error: No URL found")
            }
        }
    }
}

@Composable
fun ArticleWebView(
    modifier: Modifier = Modifier,
    url: String,
) {

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient()

                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.setSupportZoom(true)
                }
            },
            update = { webView ->
                url.let { it1 -> webView.loadUrl(it1) }
            }
        )
    }
}