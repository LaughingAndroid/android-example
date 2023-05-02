/*
 * Copyright (c) 2021. Dylan Cai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package com.laughing.lib.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

fun Application.doOnActivityLifecycle(
    onActivityCreated: ((Activity, Bundle?) -> Unit)? = null,
    onActivityStarted: ((Activity) -> Unit)? = null,
    onActivityResumed: ((Activity) -> Unit)? = null,
    onActivityPaused: ((Activity) -> Unit)? = null,
    onActivityStopped: ((Activity) -> Unit)? = null,
    onActivitySaveInstanceState: ((Activity, Bundle?) -> Unit)? = null,
    onActivityDestroyed: ((Activity) -> Unit)? = null,
): Application.ActivityLifecycleCallbacks =
    object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            onActivityCreated?.invoke(activity, savedInstanceState)
        }

        override fun onActivityStarted(activity: Activity) {
            onActivityStarted?.invoke(activity)
        }

        override fun onActivityResumed(activity: Activity) {
            onActivityResumed?.invoke(activity)
        }

        override fun onActivityPaused(activity: Activity) {
            onActivityPaused?.invoke(activity)
        }

        override fun onActivityStopped(activity: Activity) {
            onActivityStopped?.invoke(activity)
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            onActivitySaveInstanceState?.invoke(activity, outState)
        }

        override fun onActivityDestroyed(activity: Activity) {
            onActivityDestroyed?.invoke(activity)
        }
    }.also {
        registerActivityLifecycleCallbacks(it)
    }

fun Fragment.doOnViewLifecycle(
    onCreateView: (() -> Unit)? = null,
    onStart: (() -> Unit)? = null,
    onResume: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null,
    onStop: (() -> Unit)? = null,
    onDestroyView: (() -> Unit)? = null,
) =
    viewLifecycleOwner.doOnLifecycle(
        onCreateView,
        onStart,
        onResume,
        onPause,
        onStop,
        onDestroyView
    )

fun LifecycleOwner.doOnLifecycle(
    onCreate: (() -> Unit)? = null,
    onStart: (() -> Unit)? = null,
    onResume: (() -> Unit)? = null,
    onPause: (() -> Unit)? = null,
    onStop: (() -> Unit)? = null,
    onDestroy: (() -> Unit)? = null,
) =
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            onCreate?.invoke()
        }

        override fun onStart(owner: LifecycleOwner) {
            onStart?.invoke()
        }

        override fun onResume(owner: LifecycleOwner) {
            onResume?.invoke()
        }

        override fun onPause(owner: LifecycleOwner) {
            onPause?.invoke()
        }

        override fun onStop(owner: LifecycleOwner) {
            onStop?.invoke()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            onDestroy?.invoke()
        }
    })


fun ClearWhenDestroy.bindLifeCircle(owner: LifecycleOwner?) {
    val data = this
    owner?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
        var isCleared = false

        override fun onDestroy(owner: LifecycleOwner) {
            if (isCleared) {
                this@bindLifeCircle.clear()
            }
        }

        override fun onPause(owner: LifecycleOwner) {
            if (owner is Activity) {
                if(owner.isFinishing) {
                    Logs.d("LifeCircles auto destroy $data $owner")
                    if (isCleared) {
                        isCleared = true
                        this@bindLifeCircle.clear()
                    }
                }
            } else if (owner is Fragment) {
                if (owner.activity == null || owner.activity?.isFinishing == true) {
                    Logs.d("LifeCircles auto destroy $data $owner")
                    if(isCleared) {
                        isCleared = true
                        this@bindLifeCircle.clear()
                    }
                }
            } else {
                Logs.d("LifeCircles not clean $data $owner")
            }
        }
    })
}

fun (() -> Any?).onCreateCallback(owner: LifecycleOwner?, onlyOnce: Boolean = true) {
    val lifecycle = owner?.lifecycle ?: return
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            Logs.d("LifeCircles auto onCreated")
            this@onCreateCallback()
            if(onlyOnce) {
                lifecycle.removeObserver(this)
            }
        }
    })
}

fun (() -> Any?).onStartCallback(owner: LifecycleOwner?, onlyOnce: Boolean = true) {
    val lifecycle = owner?.lifecycle ?: return
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            Logs.d("LifeCircles auto onStart")
            this@onStartCallback()
            if(onlyOnce) {
                lifecycle.removeObserver(this)
            }
        }
    })
}

fun (() -> Any?).onResumeCallback(owner: LifecycleOwner?, onlyOnce: Boolean = true) {
    val lifecycle = owner?.lifecycle ?: return
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            Logs.d("LifeCircles", "auto onResume")
            this@onResumeCallback()
            if(onlyOnce) {
                lifecycle.removeObserver(this)
            }
        }
    })
}

fun (() -> Any?).onPauseCallback(owner: LifecycleOwner?, onlyOnce: Boolean = true) {
    val lifecycle = owner?.lifecycle ?: return
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onPause(owner: LifecycleOwner) {
            Logs.d("LifeCircles", "auto onPause")
            this@onPauseCallback()
            if(onlyOnce) {
                lifecycle.removeObserver(this)
            }
        }
    })
}

fun (() -> Any?).onStopCallback(owner: LifecycleOwner?, onlyOnce: Boolean = true) {
    val lifecycle = owner?.lifecycle ?: return
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            Logs.d("LifeCircles", "auto onStop")
            this@onStopCallback()
            if(onlyOnce) {
                lifecycle.removeObserver(this)
            }
        }
    })
}

fun (() -> Any?).onDestroyCallback(owner: LifecycleOwner?, onlyOnce: Boolean = true) {
    val lifecycle = owner?.lifecycle ?: return
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            Logs.d("LifeCircles", "auto onDestroy")
            this@onDestroyCallback()
            if(onlyOnce) {
                lifecycle.removeObserver(this)
            }
        }
    })
}


interface ClearWhenDestroy {
    fun clear()
}
