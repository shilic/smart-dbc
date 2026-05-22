package dataModel.dataEnums

import dataModel.services.IDbcElement
import io.github.shilic.smartGrid.core.GridColumnBind

/** 信号发送类型
 *
 *  [Cyclic] 周期型, 值 = 0
 *
 *  [OnWrite] 写入型， 值 = 1
 *
 *  [OnWriteWithRepetition] 写入型(重复型) ， 值 = 2
 *
 *  [OnChange] 事件型， 值 = 3
 *
 *  [OnChangeWithRepetition] 变化型(重复型) ，值 = 4
 *
 *  [IfActive] 激活型 ，值 = 5
 *
 *  [IfActiveWithRepetition] 激活型(重复型) ，值 = 6
 *
 *  [NoSigSendType] 未定义,  值 = 7
 * */
enum class GenSigSendType (
    /** 该数据在DBC文件中的关键字 */
    override val dbcKey : String,
    /** 该数据在DBC文件中的编码 */
    override val dbcValue : String
) : IDbcElement {
    /** [Cyclic] 周期型, 值 = 0 */
    @GridColumnBind(headerText = "Cyclic", pattern = "Cyclic|CYCLIC|cyclic")
    Cyclic ("Cyclic", "0" ),
    /** [OnWrite] 写入型， 值 = 1 */
    @GridColumnBind(headerText = "OnWrite", pattern = "(OnWrite|onWrite)((?!((With|with|WITH)\\s*(Repetition|REPETITION|repetition)))")
    OnWrite ("OnWrite", "1"),
    /**  [OnWriteWithRepetition] 写入型(重复型) ， 值 = 2 */
    @GridColumnBind(headerText = "OnWrite", pattern = "(OnWrite|onWrite)\\s*(With|with|WITH)\\s*(Repetition|REPETITION|repetition)")
    OnWriteWithRepetition ("OnWriteWithRepetition", "2"),
    /** [OnChange] 变化型 ，值 = 3 */
    @GridColumnBind(headerText = "OnChange", pattern = "(OnChange|onChange)((?!((With|with|WITH)\\s*(Repetition|REPETITION|repetition)))")
    OnChange ("OnChange", "3"),
    /** [OnChangeWithRepetition] 变化型(重复型) ，值 = 4 */
    @GridColumnBind(headerText = "OnChangeWithRepetition", pattern = "(OnChange|onChange)\\s*(With|with|WITH)\\s*(Repetition|REPETITION|repetition)")
    OnChangeWithRepetition ("OnChangeWithRepetition", "4"),
    /** [IfActive] 激活型 ，值 = 5 */
    @GridColumnBind(headerText = "IfActive", pattern = "(IfActive|ifActive)((?!((With|with|WITH)\\s*(Repetition|REPETITION|repetition)))")
    IfActive ("IfActive", "5"),
    /** [IfActiveWithRepetition] 激活型(重复型) ，值 = 6 */
    @GridColumnBind(headerText = "IfActiveWithRepetition", pattern = "(IfActive|ifActive)\\s*(With|with|WITH)\\s*(Repetition|REPETITION|repetition)")
    IfActiveWithRepetition ("IfActiveWithRepetition", "6"),
    /** [NoSigSendType] 未定义,  值 = 7 */
    NoSigSendType ("NoSigSendType", "7");
}