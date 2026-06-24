package canDemo

import demoData.*
import demoData.dbcTag1
import io.github.shilic.numberUtils.toHexStr
import io.github.shilic.smartDbc.can.contract.CanListener
import io.github.shilic.smartDbc.can.core.CanIo
import io.github.shilic.smartDbc.can.models.canFrame.contract.CanFrame
import io.github.shilic.smartDbc.dbc.dataModel.contract.DataBaseCan
import io.github.shilic.smartDbc.dbc.io.reader.DbcFileReader
import java.io.File
import kotlin.test.Test

class MainTest: CanListener {
    override val listenerName: String get() = MainTest::class.simpleName!!
    // 步骤6：实现监听器接口, 在监听器中, 使用DBC对象来解码报文
    override fun onListening(canFrame: CanFrame) {
        // 调用函数进行解码，将报文解码到DBC中。
        CanIo.decodeCanFrame(canFrame)
        // 以下语句可以打印报文, 但是我们需要验证的是绑定对象的打印
        //CanIo.findMessage(canFrame.msgId)?.also { println(it.valueInfo) }
        CanIo.getModel<Message1>()?.also { println(it) }
    }
    init {
        with(CanIo) {
            /* 步骤1: 使用 @DbcBinding 注解和 @CanBinding 注解来绑定数据模型, 将数据模型的字段绑定到DBC对象的信号中
            * 详细见 Message1 , ACToCCS1 类 等 */
            val msg1 = Message1()
            /* 步骤2：创建 DBC ;
            * 方法返回的是可变的DBC, 但是使用不可变的最上层接口来接收，避免副作用;
            * 这就是kotlin的设计哲学 */
            val dbc: DataBaseCan = DbcFileReader(File(ExampleDbcPath3)).read().apply {
                // 设置DBC标签, 这里需要和数据模型上用DbcBinding绑定的DBC标签一致。
                dbcTag = dbcTag1
                dbcComment = "DBC描述"
            }
            // 步骤3：将 DBC 添加到 DBC 管理器中
            dbcMap.put(dbc.dbcTag, dbc)
            // 步骤4：绑定数据模型，绑定成功后，框架会自动将数据模型中的字段与DBC中的信号进行绑定，并保存到模型映射中。
            bind(msg1)
            //println("modelMap.size = ${modelMap.size}")
            // 步骤5：注册 MCU
            mcuAdapter = McuAdapter
            // 步骤6：实现监听器接口, 在监听器中, 使用DBC对象来解码报文

            // 步骤7：注册监听器
            nativeRegister(this@MainTest)
        }
    }
    /** 测试程序入口函数 */
    @Test
    fun onTest() {
        println("\n--------------- CanTest测试开始 -----------------\n")
        with(McuAdapter) {
            // 步骤8：启动监听 (这里会使用内置的随机数据进行测试)
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
        // 模拟修改了指定信号的值, 相比于 CanTest 中的测试, 我们可以直接使用 数据模型进行数据的修改
        val msg1 = CanIo.getModel<Message1>()

        msg1?.apply {
            msg1sig1 = 7
            msg1sig2 = 8
            msg1sig3 = 9
            msg1sig4 = 10
            msg1sig5 = 22.20
            msg1sig6 = 10.50
            msg1sig7 = -80.50
            msg1sig8 = 110.00
        }
        // 快速发送报文, 这里我们的第二个参数为空，会使用默认的数据模型中的数据进行发送
        CanIo.send(msg1_Id)
        // 对应报文  byteArrayOf(7, 8, 9, 10, 211.toByte(), 121, 200.toByte(), 100)
        println("期望的报文为: ${data8_2.toHexStr()}")

        val newMsg = msg1?.copy (
            msg1sig1 = 15,
            msg1sig2 = 16,
            msg1sig3 = 17,
            msg1sig4 = 18,
            msg1sig5 = 2.20,
            msg1sig6 = 6.00,
            msg1sig7 = -89.20,
            msg1sig8 = 111.40,
        )
        // 快速发送报文, 这里的参数为新的数据模型对象, 会使用新数据进行发送
        CanIo.send(msg1_Id, newMsg)
        // 对应报文  byteArrayOf(15, 16, 17, 18, 111.toByte(), 112, 113.toByte(), 114)
        // 浮点型在转换时, 存在一定误差，所以发送的值会不一样
        println("期望的报文为: ${data8_4.toHexStr()}")
        println("\n--------------- CanTest测试结束 -----------------\n")
    }
}