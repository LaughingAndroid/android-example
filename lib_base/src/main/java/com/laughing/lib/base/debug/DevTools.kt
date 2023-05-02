package com.laughing.lib.base.debug

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.hardware.SensorManager
import android.view.MotionEvent
import androidx.fragment.app.FragmentActivity
import com.laughing.lib.isBaseLibDebug
import com.laughing.lib.utils.*
import kotlinx.coroutines.Job


/**
 * DevTools
 *
 * @author xl
 * @version V1.0
 * @since 23/12/2016
 */
class DevTools private constructor(context: Context?) : ShakeDetector.ShakeListener {
    private var mShakeDetector: ShakeDetector? = null
    private val mContext: Context
    private var mDialog: AlertDialog? = null
    private var job: Job? = null
    fun unregisterShakeDetector() {
        if (mShakeDetector != null) {
            mShakeDetector!!.stop()
        }
    }

    protected fun registerShakeDetector() {
        if (mShakeDetector == null) {
            mShakeDetector = ShakeDetector(this)
        }
        mShakeDetector!!.start(mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager)
    }

    private val mShowing: MutableMap<Any, Boolean> = HashMap()
    private var closeDebug = false
    override fun onShake() {
        if (closeDebug) {
            return
        }
        val activity = topActivity
        val map: MutableMap<String, Any> = LinkedHashMap()
        map["关闭工具，杀死app会恢复"] = Runnable { closeDebug = true }
        if (activity == null || mShowing[activity.hashCode()] != null && mShowing[activity.hashCode()]!!) {
            return
        }
        if (activity is IRegisterDebugMethod) {
            (activity as IRegisterDebugMethod).registerDebugMethod(map)
        }
        if (activity is FragmentActivity) {
            val fragments = activity.supportFragmentManager.fragments
            for (fragment in fragments) {
                if (fragment == null) {
                    continue
                }
                if (fragment is IRegisterDebugMethod) {
                    (fragment as IRegisterDebugMethod).registerDebugMethod(map)
                }
                val childFragmentManager = fragment.childFragmentManager
                val childFragments = childFragmentManager.fragments
                for (childFragment in childFragments) {
                    if (childFragment is IRegisterDebugMethod) {
                        (childFragment as IRegisterDebugMethod).registerDebugMethod(map)
                    }
                }
            }
        }

        if (map["dev_tool"] == false) return
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("开发者菜单")
        val array = map.keys.toTypedArray()
        builder.setItems(array) { dialog, which ->
            val obj = map[array[which]]
            if (obj is Class<*>) {
                try {
                    // 检查是否是Activity的子类
                    if (Activity::class.java.isAssignableFrom(obj as Class<*>?)) {
                        val intent = Intent(mContext, obj as Class<*>?)
                        activity.startActivity(intent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (obj is DialogInterface.OnClickListener) {
                obj.onClick(dialog, which)
            }
            if (obj is Runnable) {
                obj.run()
            }
            if (obj is Function0<*>) {
                obj.invoke()
            }
        }
        if (mDialog != null) {
            try {
                mDialog!!.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mDialog = builder.create()
        mDialog?.setOnDismissListener(DialogInterface.OnDismissListener { dialog: DialogInterface? ->
            mShowing[activity.hashCode()] = false
            mDialog = null
        })
        mDialog?.show()
        mShowing[activity.hashCode()] = true
    }

    private fun showServerDialog(activity: Activity?) {
    }

    fun onTouch(ev: MotionEvent?) {
        ev ?: return
        if (isBaseLibDebug) {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                if (ev.rawX / screenWidth < 0.8) {
                    return
                }
                job = delay(1000) {
                    Logs.d("onShake")
                    onShake()
                }
            } else if (ev.action == MotionEvent.ACTION_UP) {
                if (job != null) {
                    job!!.cancel(null)
                    job = null
                }
            }
        }
    }



    companion object {
        @JvmStatic
        var debugOnOff: DevTools? = null
            private set

        fun register(isDebug: Boolean, context: Context?) {
            if (isDebug) {
                debugOnOff = DevTools(context)
            }
        }
    }


    init {
        if (context == null) {
            throw NullPointerException("Context can not be null")
        }
        mContext = context.applicationContext
    }
}