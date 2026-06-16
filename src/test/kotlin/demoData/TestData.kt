package demoData

import kotlin.random.Random

/** 常量: Example */
const val dbcTag1 = "Example"

const val ExampleDbcPath1 = "src/test/resources/DBC/Example.dbc"
const val ExampleDbcPath2 = "src/test/resources/DBC/Example2.dbc"
const val ExampleDbcPath3 = "src/test/resources/DBC/Example(副本).dbc"
const val GBKDbcPath = "src/test/resources/DBC/大屏协议（测试版2）GBK编码.dbc"
const val UTF8DbcPath = "src/test/resources/DBC/大屏协议（测试版2）UTF-8编码.dbc"

/** 对应 BO_ 2561387265 message1: 8 Vector__XXX */
const val msg1_Id = 0x18ABAB01
const val msg2_Id = 0x18ABAB02
const val msg3_Id = 0x18ABAB03
/** 对应 BO_ 2560107544 CCSToAC1: 8 CCS */
const val CCSToAC1_Id = 0x1898_2418
/** 对应 BO_ 2560104484 ACToCCS1: 8 AC */
const val ACToCCS1_Id = 0x1898_1824
/** 对应 (CanMessage(message1).Values=[(msg1_sig1=30.00), (msg1_sig2=29.00), (msg1_sig3=28.00), (msg1_sig4=20.00), (msg1_sig5=22.20), (msg1_sig6=10.50), (msg1_sig7=-80.50), (msg1_sig8=110.00)]) */
var data8_1 = byteArrayOf(30, 29, 28, 20, 211.toByte(), 121, 200.toByte(), 100)
/** 对应 (CanMessage(0x18ABAB01).Values=['msg1_sig1=7.00', 'msg1_sig2=8.00', 'msg1_sig3=9.00', 'msg1_sig4=10.00', 'msg1_sig5=22.20', 'msg1_sig6=10.50', 'msg1_sig7=-80.50', 'msg1_sig8=110.00']) */
var data8_2 = byteArrayOf(7, 8, 9, 10, 211.toByte(), 121, 200.toByte(), 100)
/** 对应 (CanMessage(0x18ABAB01).Values=['msg1_sig1=2.00', 'msg1_sig2=22.00', 'msg1_sig3=23.00', 'msg1_sig4=24.00', 'msg1_sig5=0.40', 'msg1_sig6=2.00', 'msg1_sig7=-89.70', 'msg1_sig8=111.00']) */
var data8_3 = byteArrayOf(2, 22, 23, 24, 102.toByte(), 104, 108.toByte(), 110)
/** 对应: (CanMessage(0x18ABAB01).Values=['msg1_sig1=15.00', 'msg1_sig2=16.00', 'msg1_sig3=17.00', 'msg1_sig4=18.00', 'msg1_sig5=2.20', 'msg1_sig6=6.00', 'msg1_sig7=-89.20', 'msg1_sig8=111.40']) */
var data8_4 = byteArrayOf(15, 16, 17, 18, 111.toByte(), 112, 113.toByte(), 114)
var data8_5 = byteArrayOf(0x0B, 0x0C, 0x0D, 0x0E, 0xD8.toByte(), 0x79, 0xC2.toByte(), 0x6E)

// 将所有数组放入列表
val arrays = listOf(data8_1, data8_2, data8_3, data8_4)
/** 随机获取一个数组 */
fun getRandomArray() : ByteArray {
    return arrays[Random.nextInt(arrays.size)]
}