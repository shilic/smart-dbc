package canDemo

import demoData.getRandomArray
import demoData.msg1_Id
import io.github.shilic.smartDbc.can.contract.*
import io.github.shilic.smartDbc.can.models.canFrame.contract.*
import io.github.shilic.smartDbc.can.models.canFrame.models.*
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.*


/**
 * 增强的 MCU 适配器，支持协程化的后台监听循环。
 *
 * 设计原则：
 * 1. 使用 AtomicReference 保证监听器的线程安全访问
 * 2. 遵循结构化并发，协程可被取消
 * 3. 监听器调用在 IO 调度器上执行，避免阻塞主线程
 * 4. 提供明确的启动/停止控制
 */
object McuAdapter : IMcu, CoroutineScope {

    /** 真实的业务环境下, 应该是使用适配器模式, 外部适配器持有一个真实的MCU组件
     *
     * 再向外边，实现框架需求的接口，从而实现解耦。
     *
     * 这里，我模拟一个真实的MCU组件
     * */
    object ActualMcu  {
        fun nativeSend(canFrame: CanFrame) {
            println("底层 MCU 发送报文: ${canFrame.display}")
        }
        fun nativeRegister(canListener: CanListener) {
            println("底层 MCU 注册监听器: ${canListener.listenerName}")
        }
        fun nativeUnRegister(canListener: CanListener) {
            println("底层 MCU 注销监听器: ${canListener.listenerName}")
        }
    }
    // 使用 AtomicReference 确保线程安全的监听器访问
    private val listenerRef = AtomicReference<CanListener?>(null)
    // 使用原子引用存储 Job，确保线程安全的启动/停止
    private val monitoringJobRef = AtomicReference<Job?>(null)
    // 自定义协程作用域，使用 IO 调度器（适合设备通信）
    // 注意：object 是全局单例，这个协程会一直运行直到应用进程结束
    // 如果你需要更精细的生命周期控制，建议从外部传入 CoroutineScope
    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO
    // 监听循环是否运行的标志
    val isMonitoring: Boolean get() = monitoringJobRef.get()?.isActive == true

    /**
     * 启动后台监听循环
     * @param intervalMs 监听调用间隔（毫秒），默认 100ms
     * @param onListenerError 监听器抛出异常时的回调
     */
    fun startMonitoring(
        intervalMs: Long = 100L,
        onListenerError: ((Throwable) -> Unit)? = null
    ) {
        // 如果已经运行，先停止之前的
        stopMonitoring()

        val job = launch {
            println("MCU 监听循环启动，间隔: ${intervalMs}ms")

            // 使用 isActive 而不是 while(true)，确保可取消
            while (isActive) {
                try {
                    // 获取当前监听器（线程安全）
                    val currentListener = listenerRef.get()

                    // 如果监听器存在，调用它, 传入随机数据
                    currentListener?.onListening(CanFrameData(msg1_Id, getRandomArray()))

                    // 延迟指定间隔，但保持可取消性
                    delay(intervalMs)

                } catch (e: CancellationException) {
                    // 协程被取消，正常退出循环
                    println("MCU 监听循环被取消")
                    throw e
                } catch (e: Exception) {
                    // 监听器调用出错，可以选择继续运行
                    println("监听器调用异常: ${e.message}")
                    onListenerError?.invoke(e)
                    throw e
                    // 避免异常导致无限快速重试，延迟后继续
                    //delay(1000L)
                }
            }
        }

        // 保存 job 引用
        monitoringJobRef.set(job)
    }

    /**
     * 停止后台监听循环
     */
    fun stopMonitoring() {
        monitoringJobRef.get()?.let { job ->
            if (job.isActive) {
                job.cancel("用户请求停止监听")
                println("MCU 监听循环停止请求已发送")
            }
        }
        monitoringJobRef.set(null)
    }

    override fun nativeSend(canFrame: CanFrame) {
        ActualMcu.nativeSend(canFrame)
    }

    override fun nativeRegister(canListener: CanListener) {
        // 原子操作设置监听器
        listenerRef.set(canListener)
        ActualMcu.nativeRegister(canListener)
    }

    override fun nativeUnRegister(canListener: CanListener) {
        // 比较并交换，确保只移除匹配的监听器
        listenerRef.getAndUpdate { current ->
            if (current?.listenerName == canListener.listenerName) {
                ActualMcu.nativeUnRegister(canListener)
                null
            } else {
                current
            }
        }
    }

    /** 清理所有资源 */
    fun destroy() {
        stopMonitoring()
        listenerRef.set(null)
        //(coroutineContext[Job] as? Job)?.cancel("McuAdapter 销毁")
        coroutineContext[Job]?.cancel("McuAdapter 销毁")
    }
}