package com.github.zsoltk.composeribs.core.routing


import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner

internal class UpNavigationDispatcher {

    private var upNavigationCallback: UpNavigationCallback? = null

    interface UpNavigationCallback {
        fun onUpNavigationRequested()
    }

    fun upNavigation() {
        upNavigationCallback?.onUpNavigationRequested()
            ?: throw IllegalStateException("Up navigation callback not set")
    }

    fun addUpNavigationCallback(callback: UpNavigationCallback) {
        if (upNavigationCallback != null) {
            throw IllegalStateException("Trying to overwrite up navigation callback")
        }
        this.upNavigationCallback = callback
    }

    fun clearUpNavigationCallback() {
        upNavigationCallback = null
    }
}

@Composable
internal fun UpHandler(
    upDispatcher: UpNavigationDispatcher,
    nodeUpNavigation: () -> Boolean,
    fallbackUpNavigation: () -> Unit,
) {
    val currentNodeUpNavigation by rememberUpdatedState(nodeUpNavigation)
    val currentFallbackUpNavigation by rememberUpdatedState(fallbackUpNavigation)

    val upCallback = remember {
        object : UpNavigationDispatcher.UpNavigationCallback {
            override fun onUpNavigationRequested() {
                if (!currentNodeUpNavigation()) {
                    currentFallbackUpNavigation()
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, upDispatcher) {
        // Add callback to the upDispatcher
        upDispatcher.addUpNavigationCallback(upCallback)
        // When the effect leaves the Composition, remove the callback
        onDispose {
            upDispatcher.clearUpNavigationCallback()
        }
    }
}
