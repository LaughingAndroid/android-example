package com.laughing.lib.utils

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.template.IProvider
import com.alibaba.android.arouter.launcher.ARouter


object Router {
    init {
        ARouter.init(application)
    }

    const val ACTIVITY = "/activity"
    const val FRAGMENT = "/fragment"

    const val A_ROUTER_PATH = "router_path"
    const val STRING = "string"

    private const val LIB_BASE = "/lib_base"

    const val PAGE_CONTAINER = "$LIB_BASE$ACTIVITY/ContainerActivity"
    const val PAGE_PLAYER = "$LIB_BASE$ACTIVITY/VideoActivity"
    const val PAGE_PLAYER_FRAGMENT = "$LIB_BASE$FRAGMENT/VideoPlayFragment"

    fun navToActivity(
        pathUrl: String?,
        mActivity: Activity? = topActivity,
        requestCode: Int = 0, bundleBuilder: Postcard.() -> Unit = {}
    ) {
        if (pathUrl.isNullOrEmpty()) {
            return
        }

        if (isWebRouterUrl(pathUrl)) {
            navToWeb(pathUrl, "")
            return
        }
        val postcard = ARouter.getInstance().build(pathUrl)
        bundleBuilder(postcard)
        if (mActivity == null) {
            postcard.navigation()
        } else {
            postcard.navigation(mActivity, requestCode)
        }
    }

    fun navToFragment(
        fragmentPathUrl: String?,
        mActivity: Activity? = null,
        requestCode: Int = 0,
        bundleBuilder: Postcard.() -> Unit = {}
    ) {
        if (fragmentPathUrl.isNullOrEmpty()) {
            return
        }
        if (isWebRouterUrl(fragmentPathUrl)) {
            navToWeb(fragmentPathUrl, "", bundleBuilder = bundleBuilder)
            return
        }
        ARouter.getInstance().build(PAGE_CONTAINER)
            .withString(A_ROUTER_PATH, fragmentPathUrl)
            .apply {
                bundleBuilder(this)
                if (mActivity == null) {
                    navigation()
                } else {
                    navigation(mActivity, requestCode)
                }
            }
    }

    fun getFragment(fragmentPathUrl: String): Fragment {
        return ARouter.getInstance().build(fragmentPathUrl).navigation() as Fragment
    }

    fun getFragment(fragmentPathUrl: String, strData: String): Fragment {
        val fragment = ARouter.getInstance().build(fragmentPathUrl).navigation() as Fragment
        val bundle = Bundle()
        bundle.putString(STRING, strData)
        fragment.arguments = bundle
        return fragment
    }

    fun getFragment(fragmentPathUrl: String, bundle: Bundle): Fragment {
        val fragment = ARouter.getInstance().build(fragmentPathUrl).navigation() as Fragment
        fragment.arguments = bundle
        return fragment
    }


    private fun isWebRouterUrl(pathUrl: String): Boolean {
        return pathUrl.startsWith("http://")
                || pathUrl.startsWith("https://")
                || pathUrl.startsWith("file:///")
    }

    fun navToWeb(url: String, title: String, bundleBuilder: Postcard.() -> Unit = {}) {
//        val webEvent = WebEvent()
//        webEvent.url = url
//        webEvent.title = title
//        navToActivity(PAGE_ACTIVITY_WEB) {
//            bundleBuilder(this)
//            withSerializable(IntentConstant.WEBEVENT, webEvent)
//        }
    }

    fun inject(any: Any) {
        ARouter.getInstance().inject(any)
    }
}

fun openPage(
    path: String,
    activity: Activity? = topActivity,
    requestCode: Int = 0,
    bundleBuilder: Postcard.() -> Unit = {}
) {
    if (path.isEmpty()) return "path is empty".log()
    if (path.contains(Router.ACTIVITY)) {
        Router.navToActivity(path, activity, requestCode, bundleBuilder)
    } else {
        Router.navToFragment(path, activity, requestCode, bundleBuilder)
    }
}


fun Fragment.addFragment(fragmentPath: String, id: Int, bundleBuilder: Bundle.() -> Unit) {
    val fragment: Fragment = Router.getFragment(fragmentPath)
    fragment.arguments = arguments ?: Bundle()
    bundleBuilder(fragment.requireArguments())

    parentFragmentManager.beginTransaction()
        .add(id, fragment).commitAllowingStateLoss()
}

fun Fragment.addFragment(fragment: Fragment, id: Int) {
    parentFragmentManager.beginTransaction()
        .add(id, fragment).commitAllowingStateLoss()
}


fun FragmentActivity.addFragment(
    fragmentPath: String,
    id: Int,
    bundleBuilder: Bundle.() -> Unit = {}
) {
    val fragment: Fragment = Router.getFragment(fragmentPath)
    fragment.arguments = intent.extras ?: Bundle()
    bundleBuilder(fragment.requireArguments())

    supportFragmentManager.beginTransaction()
        .add(id, fragment).commitAllowingStateLoss()
}


fun FragmentActivity.addFragment(
    fragment: Fragment,
    id: Int,
    bundleBuilder: Bundle.() -> Unit = {}
) {
    fragment.arguments = intent.extras ?: Bundle()
    bundleBuilder(fragment.requireArguments())

    supportFragmentManager.beginTransaction()
        .add(id, fragment).commitAllowingStateLoss()
}


interface CommonService : IProvider {
    fun logout()
    fun attachCallIcon(activity: FragmentActivity)
}

fun <T : IProvider> Class<T>.get(): T? {
    return ARouter.getInstance().navigation(this)
}

inline fun <reified T : IProvider> getProvider() = T::class.java.get()