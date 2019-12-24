package per.goweii.statelayout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout

open class StateLayout : FrameLayout {

    enum class State {
        NONE, LOADING, EMPTY, ERROR, CONTENT
    }

    private var mCurrState: State = State.NONE

    private val mContentLayout: FrameLayout
    private var mLoadingLayout: View? = null
    private var mEmptyLayout: View? = null
    private var mErrorLayout: View? = null
    private var mLoadingLayoutId: Int = 0
    private var mEmptyLayoutId: Int = 0
    private var mErrorLayoutId: Int = 0

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        context.obtainStyledAttributes(attrs, R.styleable.StateLayout).apply {
            mLoadingLayoutId = getResourceId(R.styleable.StateLayout_sl_loadingLayout, mLoadingLayoutId)
            mEmptyLayoutId = getResourceId(R.styleable.StateLayout_sl_emptyLayout, mEmptyLayoutId)
            mErrorLayoutId = getResourceId(R.styleable.StateLayout_sl_errorLayout, mErrorLayoutId)
            recycle()
        }
        mContentLayout = FrameLayout(context).apply { visibility = View.GONE }
        switchState(if (isInEditMode) State.CONTENT else this.defaultState())
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        for (i in 0 until childCount) {
            val v = getChildAt(i)
            removeViewInLayout(v)
            mContentLayout.addView(v)
        }
        addViewInLayout(mContentLayout, 0, createDefLayoutParams())
    }

    protected open fun defaultState(): State {
        return State.NONE
    }

    protected open fun emptyLayout(): Int {
        return mEmptyLayoutId
    }

    protected open fun loadingLayout(): Int {
        return mLoadingLayoutId
    }

    protected open fun errorLayout(): Int {
        return mErrorLayoutId
    }

    fun currState(): State {
        return mCurrState
    }

    fun none() {
        switchState(State.NONE)
    }

    fun loading() {
        switchState(State.LOADING)
    }

    fun empty() {
        switchState(State.EMPTY)
    }

    fun error() {
        switchState(State.ERROR)
    }

    fun content() {
        switchState(State.CONTENT)
    }

    fun switchState(
        state: State,
        hideOtherState: Boolean = true,
        binder: (StateLayout.(view: View) -> Unit)? = null
    ) {
        when (state) {
            State.LOADING -> mLoadingLayout
            State.EMPTY -> mEmptyLayout
            State.ERROR -> mErrorLayout
            else -> null
        }?.let { binder?.invoke(this, it) }
        onStateChanged(mCurrState, state, hideOtherState)
        mCurrState = state
    }

    protected open fun onStateChanged(
        preState: State,
        currState: State,
        hideOtherState: Boolean = true
    ) {
        if (preState == currState) {
            return
        }
        when (currState) {
            State.NONE -> {
                if (hideOtherState) {
                    mContentLayout.visibility = View.GONE
                    removeLoadingView()
                    removeEmptyView()
                    removeErrorView()
                }
            }
            State.LOADING -> {
                addLoadingView()
                if (hideOtherState) {
                    mContentLayout.visibility = View.GONE
                    removeEmptyView()
                    removeErrorView()
                }
            }
            State.EMPTY -> {
                addEmptyView()
                if (hideOtherState) {
                    mContentLayout.visibility = View.GONE
                    removeLoadingView()
                    removeErrorView()
                }
            }
            State.ERROR -> {
                addErrorView()
                if (hideOtherState) {
                    mContentLayout.visibility = View.GONE
                    removeLoadingView()
                    removeEmptyView()
                }
            }
            State.CONTENT -> {
                mContentLayout.visibility = View.VISIBLE
                if (hideOtherState) {
                    removeLoadingView()
                    removeEmptyView()
                    removeErrorView()
                }
            }
        }
    }

    private fun addLoadingView() {
        mLoadingLayout = inflaterLayout(loadingLayout())?.also { view ->
            view.visibility = View.VISIBLE
            addView(view, createDefLayoutParams())
        }
    }

    private fun removeLoadingView() {
        mLoadingLayout?.let { view ->
            removeView(view)
        }
    }

    private fun addEmptyView() {
        mEmptyLayout = inflaterLayout(emptyLayout())?.apply {
            visibility = View.VISIBLE
            addView(this, createDefLayoutParams())
        }
    }

    private fun removeEmptyView() {
        mEmptyLayout?.let { removeView(it) }
    }

    private fun addErrorView() {
        mErrorLayout = inflaterLayout(errorLayout())?.apply {
            visibility = View.VISIBLE
            addView(this, createDefLayoutParams())
        }
    }

    private fun removeErrorView() {
        mErrorLayout?.let { removeView(it) }
    }

    private fun inflaterLayout(layoutId: Int): View? {
        return try {
            LayoutInflater.from(context).inflate(layoutId, this, false)
        } catch (e: Exception) {
            null
        }
    }

    private fun createDefLayoutParams(): LayoutParams {
        return LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }
}