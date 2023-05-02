package com.laughing.lib.utils

import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


fun View.setWidthHeight(w: Int? = null, h: Int? = null) {
    val lp = layoutParams ?: ViewGroup.LayoutParams(-1, -1)
    lp.width = w ?: lp.width
    lp.height = h ?: lp.height
    layoutParams = lp
}

fun View.setMargins(l: Int? = null, t: Int? = null, r: Int? = null, b: Int? = null) {
    this.layoutParams.as2<ViewGroup.MarginLayoutParams>()?.apply {
        setMargins(l ?: leftMargin, t ?: topMargin, r ?: rightMargin, b ?: bottomMargin)
        layoutParams = this
    }
}

fun View.setPaddings(l: Int? = null, t: Int? = null, r: Int? = null, b: Int? = null) {
    setPadding(l ?: paddingLeft, t ?: paddingTop, r ?: paddingRight, b ?: paddingBottom)
}

fun View.setPaddingOnce(p: Int) {
    setPadding(p, p, p, p)
}

fun View?.attach(parent: ViewGroup?) {
    this ?: return
    parent ?: return
    if (parent == this.parent) return "View?.attach(parent: View?) ==== parent is equals".log()
    detach()
    parent.addView(this)
}

fun View?.detach() {
    this ?: return
    parent?.as2<ViewGroup>()?.removeView(this)
}

fun View.inViewArea(x: Float, y: Float): Boolean {
    "top $top left $left right $right bottom $bottom".log()
    val r = Rect(left, top, right, bottom)
    return x > r.left && x < r.right && y > r.top && y < r.bottom
}

fun View?.letGone() {
    if (this?.visibility != View.GONE) {
        this?.visibility = View.GONE
    }
}

fun View?.letVisible() {
    if (this?.visibility != View.VISIBLE) {
        this?.visibility = View.VISIBLE
    }
}

fun View?.setVisibility(visible: Boolean) {
    if (visible) {
        letVisible()
    } else {
        letGone()
    }
}


fun View?.letInvisible() {
    this?.visibility = View.INVISIBLE
}

fun TextView?.setIconDrawable(@DrawableRes l: Int = 0, @DrawableRes t: Int = 0, @DrawableRes r: Int = 0, @DrawableRes b: Int = 0) {
    this ?: return
    setCompoundDrawablesWithIntrinsicBounds(l, t, r, b)
}

typealias ClickListener<T> = (T) -> Unit

/***
 * 设置延迟时间的View扩展
 * @param delay Long 延迟时间，默认600毫秒
 * @return T
 */
@JvmOverloads
fun <T : View> T.withTrigger(delay: Long = 600): T {
    triggerDelay = delay
    return this
}

/***
 * 点击事件的View扩展
 * @param block: (T) -> Unit 函数
 * @return Unit
 */
@Suppress("UNCHECKED_CAST")
fun <T : View> T.click(block: (T) -> Unit) = setOnClickListener {

    if (clickEnable()) {
        block(it as T)
    }
}

@Suppress("UNCHECKED_CAST")
        /***
         * 带延迟过滤的点击事件View扩展
         * @param time Long 延迟时间，默认600毫秒
         * @param block: (T) -> Unit 函数
         * @return Unit
         */
fun <T : View> T.clickWithTrigger(time: Long = 600, block: (T) -> Unit) {
    triggerDelay = time
    setOnClickListener {
        if (clickEnable()) {
            block(it as T)
        }
    }
}

private var <T : View> T.triggerLastTime: Long
    get() = if (getTag(1123460103) != null) getTag(1123460103) as Long else 0
    set(value) {
        setTag(1123460103, value)
    }

private var <T : View> T.triggerDelay: Long
    get() = if (getTag(1123461123) != null) getTag(1123461123) as Long else -1
    set(value) {
        setTag(1123461123, value)
    }

private fun <T : View> T.clickEnable(): Boolean {
    var flag = false
    val currentClickTime = System.currentTimeMillis()
    if (currentClickTime - triggerLastTime >= triggerDelay) {
        flag = true
        triggerLastTime = currentClickTime
    }
    return flag
}


/**
 * 保存当前滑动的位置，以便[resumeLocation]恢复
 */
fun RecyclerView.saveLocation(tagKey: Any? = null) {
    val lm = layoutManager.as2<LinearLayoutManager>()
        ?: return Logs.e("saveLocation: layoutManager is null")
    val topView = lm.getChildAt(0) ?: return Logs.e("saveLocation: topView is null")
    val topOffset = if (lm.orientation == LinearLayoutManager.VERTICAL) topView.top else topView.left
    val lastPosition = lm.getPosition(topView)

    val locationAndOffsetMap = getTag(R.id.recycler_location).as2<MutableMap<Any?, Pair<Int, Int>>>()
        ?: mutableMapOf()
    locationAndOffsetMap[tagKey] = Pair(topOffset, lastPosition)
    setTag(R.id.recycler_location, locationAndOffsetMap)
}

/**
 * 恢复上次列表所在的位置，需要配合[saveLocation]使用
 */
fun RecyclerView.resumeLocation(tagKey: Any? = null) {
    val locationAndOffsetMap = getTag(R.id.recycler_location).as2<MutableMap<Any?, Pair<Int, Int>>>()
        ?: return Logs.d("resumeLocation: no position")
    locationAndOffsetMap[tagKey]?.apply {
        val topOffset = first
        val lastPosition = second
        layoutManager.as2<LinearLayoutManager>()?.scrollToPositionWithOffset(lastPosition, topOffset)
    }
}

/**
 * Computes the coordinates of this view on the screen.
 */
inline val View.locationOnScreen: Rect
    get() = IntArray(2).let {
        getLocationOnScreen(it)
        Rect(it[0], it[1], it[0] + width, it[1] + height)
    }

inline fun View.withStyledAttributes(
    set: AttributeSet?,
    @StyleableRes attrs: IntArray,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0,
    block: TypedArray.() -> Unit
) =
    context.withStyledAttributes(set, attrs, defStyleAttr, defStyleRes, block)

val View.rootWindowInsetsCompat: WindowInsetsCompat? by viewTags(R.id.tag_root_window_insets) {
    ViewCompat.getRootWindowInsets(this)
}

val View.windowInsetsControllerCompat: WindowInsetsControllerCompat? by viewTags(R.id.tag_window_insets_controller) {
    ViewCompat.getWindowInsetsController(this)
}

fun View.doOnApplyWindowInsets(action: (View, WindowInsetsCompat) -> WindowInsetsCompat) =
    ViewCompat.setOnApplyWindowInsetsListener(this, action)

fun <T> viewTags(key: Int) = object : ReadWriteProperty<View, T?> {
    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: View, property: KProperty<*>) =
        thisRef.getTag(key) as? T

    override fun setValue(thisRef: View, property: KProperty<*>, value: T?) =
        thisRef.setTag(key, value)
}

@Suppress("UNCHECKED_CAST")
fun <T> viewTags(key: Int, block: View.() -> T) = ReadOnlyProperty<View, T> { thisRef, _ ->
    if (thisRef.getTag(key) == null) {
        thisRef.setTag(key, block(thisRef))
    }
    thisRef.getTag(key) as T
}

class MultiTouchDelegate(bound: Rect, delegateView: View) : TouchDelegate(bound, delegateView) {
    private val map = mutableMapOf<View, Pair<Rect, TouchDelegate>>()
    private var targetDelegate: TouchDelegate? = null

    init {
        put(bound, delegateView)
    }

    fun put(bound: Rect, delegateView: View) {
        map[delegateView] = bound to TouchDelegate(bound, delegateView)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                targetDelegate = map.entries.find { it.value.first.contains(x, y) }?.value?.second
            }
            MotionEvent.ACTION_CANCEL -> {
                targetDelegate = null
            }
        }
        return targetDelegate?.onTouchEvent(event) ?: false
    }
}

fun ImageView?.load(
    url: Any?,
    context: Context? = null,
    placeholder: Int = 0,
    error: Int = 0,
    requestListener: RequestListener<Drawable>? = null,
    radius: Int = 0
) {
    val imageView = this ?: return

    if (url != null) {
        val realContext = context ?: imageView.context
        if (realContext is Activity) {
            if (realContext.isDestroyed || realContext.isFinishing) {
                return
            }
        }
        val realPlaceholder = if (placeholder == 0) imageView.drawable else realContext.resources.getDrawable(placeholder)
        val realError = if (error == 0) imageView.drawable else realContext.resources.getDrawable(error)
        val requestOptions = RequestOptions()
            .placeholder(realPlaceholder)
            .error(realError)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
        if (radius > 0) {
            requestOptions.transforms(CenterCrop(), RoundedCorners(radius))
        }
        Glide.with(realContext)
            .load(url)
            .apply(requestOptions)
            .listener(requestListener)
            .into(imageView)
    } else if (0 != placeholder) {
        imageView.setImageResource(placeholder)
    }
}