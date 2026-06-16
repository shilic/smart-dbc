package demoData

import io.github.shilic.smartDbc.can.binds.*
import io.github.shilic.smartDbc.can.contract.CanCopyable

/** 使用 [DbcBinding] 注解, 将一个数据模型绑定到我们的DBC对象上
 *
 * 并且对象需要实现 [CanCopyable] 接口
 * */
@DbcBinding([dbcTag1])
data class Message1 (
    // 使用 @CanBinding 注解，将数据模型绑定到指定的 CAN 消息和信号上;
    // 这里对应 BO_ 2561387265 message1: 8 Vector__XXX
    // SG_ msg1_sig1 : 0|8@1+ (1,0) [0|255] "" Vector__XXX
    @CanBinding(msg1_Id, "msg1_sig1")
    var msg1sig1: Int = 0,
    @CanBinding(msg1_Id, "msg1_sig2")
    var msg1sig2: Int = 0,
    @CanBinding(msg1_Id, "msg1_sig3")
    var msg1sig3: Int = 0,
    @CanBinding(msg1_Id, "msg1_sig4")
    var msg1sig4: Int = 0,
    @CanBinding(msg1_Id, "msg1_sig5")
    var msg1sig5: Double = 0.0,
    @CanBinding(msg1_Id, "msg1_sig6")
    var msg1sig6: Double = 0.0,
    @CanBinding(msg1_Id, "msg1_sig7")
    var msg1sig7: Double = 0.0,
    @CanBinding(msg1_Id, "msg1_sig8")
    var msg1sig8: Double = 0.0,
): CanCopyable<Message1> {
    // 因为数据类 data class  自带克隆方法, 所以如果你的数据模型没有采用数据类时, 请实现一个克隆方法, 用于快速生成新的数据进行报文的发送
    override fun copyNew(): Message1 {
        return this.copy()
    }
}
