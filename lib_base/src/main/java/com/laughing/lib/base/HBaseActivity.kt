package com.laughing.lib.base

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.ktx.immersionBar
import com.laughing.lib.base.debug.DevTools
import com.laughing.lib.base.debug.IRegisterDebugMethod
import com.laughing.lib.utils.Router
import com.laughing.lib.utils.as2
import com.laughing.lib.utils.setPaddings
import com.laughing.lib.utils.statusBarHeight

@SuppressLint("Registered")
abstract class HBaseActivity<V : HBaseViewModel, VB : ViewBinding> : AppCompatActivity(),
    IRegisterDebugMethod, Loading, ViewModelFactory<V> {

    lateinit var viewModel: V

     val binding: VB by lazy(mode = LazyThreadSafetyMode.NONE) {
        BindingReflex.reflexViewBinding<VB>(javaClass, layoutInflater).apply {
            if (this is ViewDataBinding) {
                lifecycleOwner = this@HBaseActivity
            }
        }
    }
    protected abstract fun initCompleted()

    private fun registerChangeLiveDataCallBack() {
        viewModel.mShowLoading.observe(this, Observer {
            if (it) {
                showLoading()
            } else {
                hideLoading()
            }
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = createViewModel()
        setContentView(binding.root)
        setTopPadding()

        Router.inject(this)
        registerChangeLiveDataCallBack()
        setImmersionBar()
        initCompleted()
    }

    open fun setTopPadding() {
    }

    open fun setImmersionBar() {
        immersionBar {
            statusBarDarkFont(true)
            navigationBarDarkIcon(true)
        }
    }


    private var HDialogLoadingFragment: HDialogLoadingFragment? = null


    override fun showLoading() {
        if (HDialogLoadingFragment == null) {
            HDialogLoadingFragment = HDialogLoadingFragment()
        }
        if (!HDialogLoadingFragment?.isAdded!!) {
            HDialogLoadingFragment?.show(supportFragmentManager, "")
        }
    }

    override fun hideLoading() {
        HDialogLoadingFragment?.dismissAllowingStateLoss()
    }

    override fun getResources(): Resources {
        //字体适配，不随系统改变字体大小
        val res = super.getResources()
        if (res.configuration.fontScale != 1f) { //非默认值
            val newConfig = Configuration()
            newConfig.setToDefaults() //设置默认
            res.updateConfiguration(newConfig, res.displayMetrics)
        }
        return res
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.dispose()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        debug(ev)
        return super.dispatchTouchEvent(ev)
    }

    open fun debug(ev: MotionEvent?) {
        DevTools.debugOnOff?.onTouch(ev)
    }

    override fun registerDebugMethod(map: MutableMap<String, Any>) {

    }
}


interface Loading {
    fun showLoading()
    fun hideLoading()
}
