package reform.ko.task

import reform.ko.log.Logk
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.atomic.AtomicReference

/**
 * The [JobTask] class represents anything that caller want to do.
 *
 * JobTask is consist of many elements and do each element one by one. If JobTask is cancelled, all element
 * will be cancelled.
 */
public class JobTask private constructor() {
    /**
     * all element to processing
     */
    private var element: Element<*, *>? = null

    /**
     * current processing element
     */
    private var curElement: AtomicReference<Element<*, *>> = AtomicReference()

    /**
     * the [JobTask] status
     */
    private var status: AtomicReference<JTStatus> = AtomicReference(JTStatus.INIT)

    init {
        Logk.i(msg = "JobTask init")
    }

    companion object {
        val Main = Worker.MAIN
        val Logic = Worker.LOGIC
        val Task = Worker.TASK
        val Io = Worker.IO
        inline fun build(block: (Builder.() -> Unit)) = Builder().apply(block).build()
    }

    class Builder {
        fun build() = JobTask()
    }

    /**
     * [next] function constructs job [Element] that running on different thread. Only constructs
     * element don't execute block code until [JobTask.start] function.
     */
    fun <T, R> next(worker: Worker, block: ((T?) -> R)) = apply {
        element = if (element == null) {
            Element(0, this, worker, block)
        } else {
            element!! + Element(element!!.id + 1, this, worker, block)
        }
    }

    /**
     * Start execute element's block code one by one.
     */
    fun start(param: Any? = null) = apply {
        status.set(JTStatus.RUNNING)
        element?.let {
            curElement.set(it)
            curElement.get().start(param)
        }
    }

    /**
     * Cancel current running [JobTask]. If the [JobTask] isn't running, do nothing.
     */
    fun cancel() {
        if (status.compareAndSet(JTStatus.RUNNING, JTStatus.CANCEL)) {
            curElement.get().cancel()
        } else {
            status.set(JTStatus.CANCEL)
        }
    }

    internal fun processNext(element: Element<*, *>) {
        curElement.set(element)
    }

    internal fun finishJobTask(e: Exception? = null) {
        Logk.i(msg = "finishJobTask ${curElement.get().id}")
        status.set(JTStatus.COMPLETE)
        e?.apply {
            Logk.e(JobTask::class.java.simpleName,
                    "finishJobTask with e: ${e.localizedMessage}")
        }
    }
}

/**
 * The lambda expression that execute on the dispatcher.
 *
 * One job may consist of multi element linked by [JobTask.next] function, the dispatcher runs each element
 * one by one. Each element's input parameter is previous element's output result, the first
 * element's input parameter is transferred by [JobTask.start] function.
 */
internal class Element<T, R>(
        internal val id: Int,
        private val jobTask: JobTask,
        private val worker: Worker,
        private val block: (T?) -> R
) {

    private var next: Element<*, *>? = null

    var future: FutureElement<T, R>? = null

    operator fun plus(element: Element<*, *>): Element<T, R> {
        next = if (next == null) {
            element
        } else {
            next!! + element
        }
        return this
    }

    fun start(param: Any?) {
        @Suppress("UNCHECKED_CAST")
        val p = param as T?
        future = FutureElement(this, ElementCallable(p, block))
        JobDispatcher.dispatch(worker, this)
    }

    fun cancel() {
        Logk.i(msg = "cancel element: $id")
        JobDispatcher.cancel(worker, this)
    }

    fun processNext() {
        Logk.i(msg = "processNext current: $id")
        if (future!!.isCancelled) {
            return
        }
        try {
            // 正常执行完成
            if (next == null) {
                jobTask.finishJobTask()
            } else {
                Logk.i(msg = "processNext next: ${next!!.id}")
                // 执行下一个element
                jobTask.processNext(next!!)
                next!!.start(future!!.get())
            }
        } catch (e: InterruptedException) {
            jobTask.finishJobTask(e)
            return
        } catch (e: ExecutionException) {
            jobTask.finishJobTask(e)
            return
        }
    }

    internal class ElementCallable<T, R>(
            private var p: T?,
            private val block: (T?) -> R
    ) : Callable<R> {
        override fun call(): R {
            return block(p)
        }
    }

    internal class FutureElement<T, R>(
            private val element: Element<T, R>,
            callable: Callable<R>
    ) : FutureTask<R>(callable) {

        override fun done() {
            element.processNext()
        }
    }
}

/**
 * Represent the [JobTask] status
 */
internal enum class JTStatus(val status: Int) {
    INIT(0),
    CANCEL(1),
    RUNNING(2),
    COMPLETE(3)
}