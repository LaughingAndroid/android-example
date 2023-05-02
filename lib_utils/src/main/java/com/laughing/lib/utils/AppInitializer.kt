
@file:Suppress("unused")

package com.laughing.lib.utils

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

class AppInitializer : Initializer<Unit> {
  private var started = 0

  override fun create(context: Context) {

    application = context as Application
    application.doOnActivityLifecycle(
      onActivityCreated = { activity, _ ->
        activityCache.add(activity)
      },
      onActivityStarted = { activity ->
        started++
        if (started == 1) {
          onAppStatusChangedListener?.onForeground(activity)
        }
      },
      onActivityStopped = { activity ->
        started--
        if (started == 0) {
          onAppStatusChangedListener?.onBackground(activity)
        }
      },
      onActivityDestroyed = { activity ->
        activityCache.remove(activity)
      }
    )
  }

  override fun dependencies() = emptyList<Class<Initializer<*>>>()

  companion object {
    internal var onAppStatusChangedListener: OnAppStatusChangedListener? = null
  }
}
