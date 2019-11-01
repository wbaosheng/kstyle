package com.beta.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.AttributeSet

class LoadMoreRecyclerView : RecyclerView {

    var loading: Boolean = false
    var loadMoreListener: ILoadMoreListener? = null

    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attr: AttributeSet?) : super(context, attr) {
        init(context)
    }

    constructor(context: Context?, attr: AttributeSet?, defStyleAttr: Int) : super(context, attr, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context?) {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                if (SCROLL_STATE_IDLE == newState && !loading) {
                    val layoutManager: LayoutManager? = recyclerView?.layoutManager ?: return
                    var lastVisiblePosition = -1
                    if (layoutManager is LinearLayoutManager) {
                        lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
                    } else if (layoutManager is StaggeredGridLayoutManager) {
                        val visiblePositions = layoutManager.findLastVisibleItemPositions(IntArray(layoutManager.spanCount))
                        var maxPosition = Int.MIN_VALUE;
                        for (p in visiblePositions) {
                            if (p > maxPosition) {
                                maxPosition = p
                            }
                        }
                        lastVisiblePosition = maxPosition
                    }

                    if (lastVisiblePosition == (layoutManager!!.itemCount - 1)) {
                        loading = true
                        loadMoreListener?.onLoadMore()
                    }
                }
            }
        })
    }

    override fun setLayoutManager(layout: LayoutManager?) {
        super.setLayoutManager(layout)
        adapter?.onAttachedToRecyclerView(this)
    }
}