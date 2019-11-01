package com.beta.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.beta.view.ILoadMoreListener
import com.beta.view.LoadMoreView
import com.beta.R
import kotlinx.android.synthetic.main.recycler.*

class RecyclerActivity : AppCompatActivity() {

    companion object {
        const val CAPACITY: Int = 30
        const val FOOTER_VIEW_COUNT = 1
    }

    var data: MutableList<FeedData> = MutableList(CAPACITY) {
        FeedData(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recycler)
        initRadioGroup()
        initList()
    }

    private fun initRadioGroup() {
        radio_group.check(R.id.radio_linear)
        radio_group.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_linear -> {
                    list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                }
                R.id.radio_stage -> {
                    list.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                }
            }
        }
    }

    private fun initList() {
        list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        var count = data.size
        list.loadMoreListener = object : ILoadMoreListener {
            override fun onLoadMore() {
                list.postDelayed({
                    data.addAll(MutableList(CAPACITY) {
                        FeedData(count + it)
                    })
                    list.adapter.notifyDataSetChanged()
                    list.loading = false
                }, 1000)
            }

        }
        list.adapter = FeedAdapter(this, data)
        list.adapter.notifyDataSetChanged()
    }
}

class FeedAdapter constructor(private val context: Context, private val data: MutableList<FeedData>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemType(val type: Int) {
        LOAD_MORE(-1),
        NORMAL(0),
        BIG(1),
        SMALL(2)
    }

    override fun getItemCount(): Int {
        return data.size + RecyclerActivity.FOOTER_VIEW_COUNT
    }

    override fun getItemViewType(position: Int): Int {
        if (position >= data.size) {
            return ItemType.LOAD_MORE.type
        }
        val value: FeedData = data[position]
        when (value.type) {
            0 -> return ItemType.NORMAL.type
            1 -> return ItemType.BIG.type
            2 -> return ItemType.SMALL.type
        }
        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            -1 -> LoadMoreViewHolder(LoadMoreView(context))
            0 -> NormalViewHolder(LayoutInflater.from(context).inflate(R.layout.listitem_normal, parent, false))
            else -> NormalViewHolder(LayoutInflater.from(context).inflate(R.layout.listitem_normal, parent, false))
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NormalViewHolder) {
            holder.idle.text = "ListView Item $position"
        }

        if (holder is LoadMoreViewHolder) {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        } else {
            holder.itemView.setBackgroundColor(getColorRandom())
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        var lp = holder.itemView.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) {
            if (getItemViewType(holder.layoutPosition) == ItemType.LOAD_MORE.type) {
                lp.isFullSpan = true
            }
        }
    }

    private fun getColorRandom(): Int {
        return Color.argb((Math.random() * 255).toInt(),
                (Math.random() * 255).toInt(),
                (Math.random() * 255).toInt(),
                (Math.random() * 255).toInt())
    }

    class LoadMoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
        }
    }

    class NormalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var idle: TextView = itemView.findViewById(R.id.text_idle)
    }
}

data class FeedData(var type: Int) {
    var title: String = "title"
    var description: String = "description"
}