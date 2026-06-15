package canDemo

import demoData.ExampleDbcPath3
import demoData.dbcTag1
import io.github.shilic.smartDbc.can.contract.CanListener
import io.github.shilic.smartDbc.can.models.canFrame.contract.CanFrame
import io.github.shilic.smartDbc.dbc.dataModel.models.DataBaseCanImp
import io.github.shilic.smartDbc.dbc.io.reader.DbcFileReader
import io.github.shilic.smartDbc.valueConverter.decodeCanFrame
import org.junit.jupiter.api.Test

/**
 * CAN测试类, 不使用对象绑定，直接使用DBC对象，直接监听报文。
 */
class CanTest : CanListener {
    // 步骤1：创建 DBC
    val dbc: DataBaseCanImp = DbcFileReader(ExampleDbcPath3).create().apply {
        dbcTag = dbcTag1
        dbcComment = "DBC描述"
    }
    override val listenerName: String get() = CanTest::class.simpleName!!
    // 步骤2：实现监听器
    override fun onListening(canFrame: CanFrame) {
        println("CAN1 接收到报文: ${canFrame.display}")
        // 调用函数进行解码，将报文解码到DBC中。
        dbc.decodeCanFrame(canFrame)
    }
    /** 测试程序入口函数 */
    @Test
    fun onCreate() {
        // 步骤3：创建 CAN 通信适配器
        val mcu = McuAdapter
        // 步骤4：注册监听器
        mcu.nativeRegister(this)
        // 步骤5：启动监听
        mcu.startMonitoring()
        Thread.sleep(5_000)
    }
}