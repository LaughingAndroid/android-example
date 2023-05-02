package com.laughing.lib.base

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.laughing.lib.base.debug.IRegisterDebugMethod
import com.laughing.lib.utils.as2

abstract class HBaseDialogFragment<VM : HBaseViewModel, T> : DialogFragment(), IRegisterDebugMethod,
    ViewModelFactory<VM> {

    protected abstract fun getLayoutId(): Int

    abstract fun generateViewModel(): VM

    abstract fun onViewCreatedFinish(view: View, savedInstanceState: Bundle?)

    lateinit var viewModel: VM


    var enableLoading = true

    private lateinit var listener: ListenerBuilder<T>

    private var confirmClicked = false

    open fun getAnimationId(): Int {
        return 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(0xb3000000.toInt()))
        return inflater.inflate(getLayoutId(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = generateViewModel()

        if (enableLoading) {
            viewModel.mShowLoading.observe(this, Observer {
                showLoading(it)
            })
        }

        if (canCancel()) {
            dialog?.run {
                setCancelable(true)
                setCanceledOnTouchOutside(true)
            }
        } else {
            dialog?.run {
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setOnKeyListener { _, _, _ -> true }
            }
        }

        onViewCreatedFinish(view, savedInstanceState)
    }

    open fun canCancel(): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    open fun enableLoading(): Boolean {
        return true
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.run {
            if (getAnimationId() != 0) {
                this.attributes?.windowAnimations = getAnimationId()
            }
            this.attributes?.dimAmount = 0.7f
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onDestroyView() {
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        super.onDestroyView()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (::listener.isInitialized) {
            listener.dismissAction?.invoke(this, confirmClicked)
        }
    }

    open fun registerListener(listenerBuilder: ListenerBuilder<T>.() -> Unit): HBaseDialogFragment<VM, T> {
        listener = ListenerBuilder<T>().also(listenerBuilder)
        return this
    }

    fun showLoading(show: Boolean) {
        when (show) {
            true -> (activity.as2<Loading>())?.showLoading()
            false -> (activity.as2<Loading>())?.hideLoading()
        }
    }


    protected open fun invokeConfirmed(selected: T) {
        this.confirmClicked = true
        if (::listener.isInitialized) {
            listener.confirmedAction?.invoke(this, selected)
        }
    }

    protected fun invokeCanceled() {
        if (::listener.isInitialized) {
            listener.cancelAction?.invoke(this)
        }
    }

    protected fun invokeNeutral() {
        if (::listener.isInitialized) {
            listener.neutralAction?.invoke(this)
        }
    }

    protected fun invokeDismissed() {
        if (::listener.isInitialized) {
            listener.dismissAction?.invoke(this, confirmClicked)
        }
        confirmClicked = false
    }

    protected fun invokeClosed() {
        if (::listener.isInitialized) {
            listener.closeAction?.invoke(this)
        }
    }

    fun show(manager: FragmentManager) {
        show(manager, javaClass.canonicalName)
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            DialogFragment::class.java.getDeclaredField("mDismissed").apply {
                isAccessible = true
                setBoolean(this@HBaseDialogFragment, false)
            }
            DialogFragment::class.java.getDeclaredField("mShownByMe").apply {
                isAccessible = true
                setBoolean(this@HBaseDialogFragment, true)
            }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        try {
            if (!this.isAdded) {
                manager.executePendingTransactions()
                manager.beginTransaction()
                    .add(this, tag)
                    .commitNowAllowingStateLoss()
            }
        } catch (ignore: Exception) {
            ignore.printStackTrace()
            // do nothing
        }
    }

    override fun showNow(manager: FragmentManager, tag: String?) {
        show(manager, tag)
    }

    open class ListenerBuilder<T> {
        var confirmedAction: ((HBaseDialogFragment<*, *>, T) -> Unit)? = null
        var cancelAction: ((HBaseDialogFragment<*, *>) -> Unit)? = null
        var dismissAction: ((HBaseDialogFragment<*, *>, Boolean) -> Unit)? = null
        var neutralAction: ((HBaseDialogFragment<*, *>) -> Unit)? = null
        var closeAction: ((HBaseDialogFragment<*, *>) -> Unit)? = null

        fun confirmed(action: (HBaseDialogFragment<*, *>, T) -> Unit) {
            confirmedAction = action
        }

        fun canceled(action: (HBaseDialogFragment<*, *>) -> Unit) {
            cancelAction = action
        }

        fun dismissed(action: (HBaseDialogFragment<*, *>, Boolean) -> Unit) {
            dismissAction = action
        }

        fun neutralClick(action: (HBaseDialogFragment<*, *>) -> Unit) {
            neutralAction = action
        }

        fun closeClick(action: (HBaseDialogFragment<*, *>) -> Unit) {
            closeAction = action
        }
    }

    override fun registerDebugMethod(map: MutableMap<String, Any>) {

    }
}


