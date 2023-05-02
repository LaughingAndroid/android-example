package com.laughing.lib.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.alibaba.android.arouter.launcher.ARouter

/**
 *
 * @ClassName: Dialogs
 * @Description: dialog相关
 * @Author: Laughing
 * @CreateDate: 2019/5/24 14:30
 * @Version: 1.2.0
 */

/**
 * show dialog directly
 */
fun <T : HBaseDialogFragment<*, *>> FragmentActivity.showDialog(fragment: T, func: (T.() -> Unit)? = null) {
    showDialog(supportFragmentManager, fragment, func)
}

fun <T : HBaseDialogFragment<*, *>> Fragment.showDialog(fragment: T, func: (T.() -> Unit)? = null) {
    showDialog(childFragmentManager, fragment, func)
}

private fun <T : HBaseDialogFragment<*, *>> showDialog(fragmentManager: FragmentManager, fragment: T, func: (T.() -> Unit)? = null) {
    func?.let { fragment.it() }
    fragment.show(fragmentManager)
}

/**
 * show dialog by path
 */

private fun <T> showDialog(fragmentManager: FragmentManager, path: String, listenerBuilder: (HBaseDialogFragment.ListenerBuilder<T>.() -> Unit)? = null) {
    val f = ARouter.getInstance().build(path).navigation() as? HBaseDialogFragment<*, T>
    f?.apply {
        listenerBuilder?.run {
            registerListener(this)
        }
        show(fragmentManager)
    }
}


fun <T> Fragment.showDialog(path: String, listenerBuilder: (HBaseDialogFragment.ListenerBuilder<T>.() -> Unit)? = null) {
    activity?.apply {
        showDialog(childFragmentManager, path, listenerBuilder)
    }
}

fun <T> FragmentActivity.showDialog(path: String, listenerBuilder: (HBaseDialogFragment.ListenerBuilder<T>.() -> Unit)? = null) {
    showDialog(supportFragmentManager, path, listenerBuilder)
}