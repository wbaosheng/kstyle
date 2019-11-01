package reform.ko.ktask

/**
 * The Job task runs on different dispatcher.
 */
class Task(val runnable: Runnable,
           val result: Result?) {
    private constructor(builder: Builder): this(builder.runnable, builder.result)

    companion object {
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder {
        var runnable: Runnable = Runnable {  }
        var result: Result? = null

        fun build() = Task(this)
    }
}


class Result {
}