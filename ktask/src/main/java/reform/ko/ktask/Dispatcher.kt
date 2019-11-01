package reform.ko.ktask

import android.os.Handler
import android.os.Looper
import android.os.Process
import reform.ko.internal.AVAILABLE_PROCESSORS
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

public object Dispatchers {
    val MAIN: Dispatcher = MainDispatcher()

    val LOGIC: Dispatcher = LogicDispatcher()

    val TASK: Dispatcher = TaskDispatcher()

    val IO: Dispatcher = IODispatcher()
}

abstract class Dispatcher {
    abstract val executor: Executor
    abstract fun dispatch(task: Task)
}

internal class MainDispatcher : Dispatcher() {
    private val handler = Handler(Looper.getMainLooper())

    override val executor: Executor = Executor {

    }

    override fun dispatch(task: Task) {
        handler.post(task.runnable)
    }
}

internal class LogicDispatcher : Dispatcher() {
    override val executor = ThreadPoolExecutor(
            1,
            1,
            60L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(),
            NameThreadFactory("LOGIC-", Process.THREAD_PRIORITY_DEFAULT))

    override fun dispatch(task: Task) {
    }
}

internal class TaskDispatcher : Dispatcher() {

    override val executor = ThreadPoolExecutor(
            AVAILABLE_PROCESSORS,
            AVAILABLE_PROCESSORS,
            60L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(),
            NameThreadFactory("TASK-", Process.THREAD_PRIORITY_BACKGROUND)
    )

    override fun dispatch(task: Task) {
    }
}

internal class IODispatcher : Dispatcher() {
    override val executor = ThreadPoolExecutor(
            2,
            AVAILABLE_PROCESSORS * 128,
            5L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>(),
            NameThreadFactory("IO-", Process.THREAD_PRIORITY_BACKGROUND)
    )

    override fun dispatch(task: Task) {

    }
}

internal class NameThreadFactory(private val prefix: String, private val priority: Int) : ThreadFactory {
    private val threadNumber = AtomicInteger(1)

    override fun newThread(r: Runnable?): Thread {
        val thread = Thread(prefix + threadNumber.getAndIncrement())
        Process.setThreadPriority(priority)
        return thread
    }
}