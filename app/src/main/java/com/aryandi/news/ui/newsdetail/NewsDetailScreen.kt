package com.aryandi.news.ui.newsdetail

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            ) {
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
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var loadedUrl by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false

                        // Performance improvements
                        domStorageEnabled = true

                        // Cache settings for better performance
                        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                            hasError = false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            isLoading = false
                            hasError = true
                            errorMessage = error?.description?.toString()
                                ?: "Failed to load the page"
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            return false
                        }
                    }

                    webView = this
                }.also {
                    // Load URL only once during creation
                    it.loadUrl(url)
                    loadedUrl = url
                }
            },
            update = { view ->
                // Only reload if URL changes (prevents unnecessary reloads)
                if (loadedUrl != url) {
                    view.loadUrl(url)
                    loadedUrl = url
                    isLoading = true
                    hasError = false
                }
            }
        )

        // Loading indicator
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // Error state
        if (hasError && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("❌")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Failed to load article")
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            webView?.reload()
                            isLoading = true
                            hasError = false
                        }
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }

    // Clean up WebView when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            webView?.destroy()
        }
    }
}