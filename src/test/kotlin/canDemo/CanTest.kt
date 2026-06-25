package canDemo

import demoData.ExampleDbcPath3
import demoData.dbcTag1
import demoData.msg1_Id
import io.github.shilic.smartDbc.can.contract.CanListener
import io.github.shilic.smartDbc.can.models.canFrame.contract.CanFrame
import io.github.shilic.smartDbc.dbc.dataModel.contract.DataBaseCan
import io.github.shilic.smartDbc.dbc.io.reader.DbcFileReader
import io.github.shilic.smartDbc.valueConverter.*
import org.junit.jupiter.api.Test
import java.io.File

/**
 * 框架的用法1: 不使用对象绑定，直接使用DBC对象，监听报文后，解析报文到DBC对象。
 *
 * 适用于新项目，没有定义数据模型，或者你不想定义数据模型的时候使用。直接使用内置的数据模型来操作报文。
 */
class CanTest : CanListener {
    /* 步骤1：创建 DBC ;
     * 构造方法返回的是可变的DBC, 但是使用不可变的最上层接口来接收，避免副作用;
     * 这就是kotlin的设计哲学 */
    val dbc: DataBaseCan = DbcFileReader(File(ExampleDbcPath3)).read().apply {
        dbcTag = dbcTag1
        dbcComment = "DBC描述"
    }
    override val listenerName: String get() = CanTest::class.simpleName!!
    // 步骤2：实现监听器接口, 在监听器中, 使用DBC对象来解码报文
    override fun onListening(canFrame: CanFrame) {
        // 调用函数进行解码，将报文解码到DBC中。
        dbc.decodeCanFrame(canFrame)
        // 打印调试信息
        dbc[canFrame.msgId]?.also { println(it.valueInfo) }
        // 使用二维索引器语法，从DBC中快速查询到对应的信号。拿到值之后， 你可以在这里执行任意 的 UI渲染操作
        dbc[canFrame.msgId, "msg1_sig1"]?.also { println("使用 sig1 信号的值进行渲染 sig1=${it.currentTextValue}") }
    }
    /** 测试程序入口函数 */
    @Test
    fun onTest() {
        println("\n--------------- CanTest测试开始 -----------------\n")
        // 步骤3：创建 CAN 通信适配器
        with(McuAdapter) {
            // 步骤4：注册监听器
            register(this@CanTest)
            // 步骤5：启动监听 (这里会使用内置的随机数据进行测试)
            startMonitoring()
            // 延长JVM时间 (尽可能多的打印测试数据)
            Thread.sleep(3_000)
            stopMonitoring()
        }
        println("\n--------------- CanTest测试结束 -----------------\n")
        /*这里会循环打印接收到的报文,
        * 调用底层 MCU 注册监听器: CanTest
        MCU 监听循环启动，间隔: 100ms
        (CanMessage(0x18ABAB01).Values=['msg1_sig1=30.00', 'msg1_sig2=29.00', 'msg1_sig3=28.00', 'msg1_sig4=20.00', 'msg1_sig5=22.20', 'msg1_sig6=10.50', 'msg1_sig7=-80.50', 'msg1_sig8=110.00'])
        (CanMessage(0x18ABAB01).Values=['msg1_sig1=2.00', 'msg1_sig2=22.00', 'msg1_sig3=23.00', 'msg1_sig4=24.00', 'msg1_sig5=0.40', 'msg1_sig6=2.00', 'msg1_sig7=-89.70', 'msg1_sig8=111.00'])
        (CanMessage(0x18ABAB01).Values=['msg1_sig1=15.00', 'msg1_sig2=16.00', 'msg1_sig3=17.00', 'msg1_sig4=18.00', 'msg1_sig5=2.20', 'msg1_sig6=6.00', 'msg1_sig7=-89.20', 'msg1_sig8=111.40'])
        (CanMessage(0x18ABAB01).Values=['msg1_sig1=15.00', 'msg1_sig2=16.00', 'msg1_sig3=17.00', 'msg1_sig4=18.00', 'msg1_sig5=2.20', 'msg1_sig6=6.00', 'msg1_sig7=-89.20', 'msg1_sig8=111.40'])
        (CanMessage(0x18ABAB01).Values=['msg1_sig1=15.00', 'msg1_sig2=16.00', 'msg1_sig3=17.00', 'msg1_sig4=18.00', 'msg1_sig5=2.20', 'msg1_sig6=6.00', 'msg1_sig7=-89.20', 'msg1_sig8=111.40'])
        (CanMessage(0x18ABAB01).Values=['msg1_sig1=7.00', 'msg1_sig2=8.00', 'msg1_sig3=9.00', 'msg1_sig4=10.00', 'msg1_sig5=22.20', 'msg1_sig6=10.50', 'msg1_sig7=-80.50', 'msg1_sig8=110.00'])
        *
        * */
    }
    /** 模拟事件触发时, 发送CAN报文 */
    @Test
    fun onEvent() {
        println("\n--------------- CanTest测试开始 -----------------\n")
        // 模拟修改了指定信号的值
        dbc[msg1_Id, "msg1_sig1"]?.currentPhyValue = 10.0
        dbc[msg1_Id, "msg1_sig2"]?.currentPhyValue = 15.0
        dbc[msg1_Id, "msg1_sig3"]?.currentPhyValue = 16.0
        dbc[msg1_Id, "msg1_sig4"]?.currentPhyValue = 17.0

        // 快速编码报文
        val canFrame = dbc.encodeCanFrame(msg1_Id)
        // 发送报文
        McuAdapter.transmit(canFrame)
        println("\n--------------- CanTest测试结束 -----------------\n")
    }
}