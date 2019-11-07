package reform.ko.task

import android.os.*
import reform.ko.internal.AVAILABLE_PROCESSORS
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * The four types of dispatcher:
 *
 * * [MAIN]: as name, run on UI thread.
 *
 * * [LOGIC]: process sequence logic on background handler thread. For example,
 * if runnable B dependent A, you can post A and B sequence.
 *
 * * [TASK]：process CPU busy job, don't confirm job's sequence.
 *
 * * [IO]: process IO blocking jobs, ex: network, disk file.
 */
public enum class Dispatch {
    MAIN, LOGIC, TASK, IO;
}

internal object JobDispatcher {
    private val Main: Dispatcher = Dispatcher(Dispatch.MAIN)

    private val Logic: Dispatcher = Dispatcher(Dispatch.LOGIC)

    private val Task: Dispatcher = Dispatcher(Dispatch.TASK)

    private val Io: Dispatcher = Dispatcher(Dispatch.IO)

    fun <T, R> dispatch(dispatch: Dispatch, element: Element<T, R>) {
        val dispatcher = when (dispatch) {
            Dispatch.MAIN -> Main
            Dispatch.LOGIC -> Logic
            Dispatch.TASK -> Task
            Dispatch.IO -> Io
        }
        dispatcher.dispatch(element)
    }
}

internal class Dispatcher(private val name: Dispatch) {
    private val main = DispatchHandler(Looper.getMainLooper())

    private val logic by lazy {
        val thread = HandlerThread("Lg-t")
        thread.start()
        DispatchHandler(thread.looper)
    }

    val transform: Handler by lazy {
        val thread = HandlerThread("tfm-t")
        thread.start()
        Handler(thread.looper)
    }

    private val executor: ExecutorService by lazy {
        when (name) {
            Dispatch.MAIN -> {
                AndroidExecutorService(main)
            }
            Dispatch.LOGIC -> {
                AndroidExecutorService(logic)
            }
            Dispatch.TASK -> {
                ThreadPoolExecutor(AVAILABLE_PROCESSORS, AVAILABLE_PROCESSORS,
                        1L, TimeUnit.SECONDS,
                        LinkedBlockingQueue<Runnable>(),
                        NameThreadFactory("Task-", Process.THREAD_PRIORITY_BACKGROUND)
                )
            }
            Dispatch.IO -> {
                ThreadPoolExecutor(AVAILABLE_PROCESSORS, AVAILABLE_PROCESSORS * 8,
                        1L, TimeUnit.SECONDS,
                        LinkedBlockingQueue<Runnable>(),
                        NameThreadFactory("Io-", Process.THREAD_PRIORITY_BACKGROUND)
                )
            }
        }
    }

    fun <T, R> dispatch(element: Element<T, R>) {
        executor.execute(element.future)
    }
}

internal class DispatchHandler(looper: Looper) : Handler(looper) {

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            EXECUTOR_COMMAND -> {
                val futureTask = msg.obj as FutureTask<*>
                futureTask.run()
            }
        }
    }

    companion object {
        const val EXECUTOR_COMMAND = 1
    }
}

internal class AndroidExecutorService(private val handler: DispatchHandler) : ExecutorService {

    override fun shutdown() {
    }

    override fun <T : Any?> submit(task: Callable<T>?): Future<T> {
        val future = FutureTask(task)
        handler.post(future)
        return future
    }

    override fun <T : Any?> submit(task: Runnable?, result: T): Future<T> {
        val future = FutureTask(task, result)
        handler.post(future)
        return future
    }

    override fun submit(task: Runnable?): Future<*> {
        val future = FutureTask(task, null)
        handler.post(future)
        return future
    }

    override fun shutdownNow(): MutableList<Runnable>? {
        return null
    }

    override fun isShutdown(): Boolean {
        return false
    }

    override fun awaitTermination(timeout: Long, unit: TimeUnit?): Boolean {
        return false
    }

    override fun <T : Any?> invokeAny(tasks: MutableCollection<out Callable<T>>?): T? {
        return null
    }

    override fun <T : Any?> invokeAny(tasks: MutableCollection<out Callable<T>>?, timeout: Long, unit: TimeUnit?): T? {
        return null
    }

    override fun isTerminated(): Boolean {
        return false
    }

    override fun <T : Any?> invokeAll(tasks: MutableCollection<out Callable<T>>?): MutableList<Future<T>>? {
        return null
    }

    override fun <T : Any?> invokeAll(tasks: MutableCollection<out Callable<T>>?, timeout: Long, unit: TimeUnit?): MutableList<Future<T>>? {
        return null
    }

    override fun execute(command: Runnable?) {
        handler.post(command)
        val msg = Message.obtain(handler)
        msg.what = DispatchHandler.EXECUTOR_COMMAND
        msg.obj = command
    }
}

internal class NameThreadFactory(private val prefix: String, private val priority: Int) : ThreadFactory {
    private val threadNumber = AtomicInteger(1)

    override fun newThread(r: Runnable?): Thread {
        val thread = Thread(r, prefix + threadNumber.getAndIncrement())
        Process.setThreadPriority(priority)
        return thread
    }
}