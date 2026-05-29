package io.github.shilic.smartDbc.dbc.dataModel.dataEnums

import io.github.shilic.smartDbc.common.tool.IntEnum
import io.github.shilic.smartDbc.dbc.dataModel.contract.*
import io.github.shilic.smartGrid.core.GridColumnBind

/** 信号发送类型
 *
 *  [GenSigSendType.Cyclic] 周期型, 值 = 0
 *
 *  [GenSigSendType.OnWrite] 写入型， 值 = 1
 *
 *  [GenSigSendType.OnWriteWithRepetition] 写入型(重复型) ， 值 = 2
 *
 *  [GenSigSendType.OnChange] 事件型， 值 = 3
 *
 *  [GenSigSendType.OnChangeWithRepetition] 变化型(重复型) ，值 = 4
 *
 *  [GenSigSendType.IfActive] 激活型 ，值 = 5
 *
 *  [GenSigSendType.IfActiveWithRepetition] 激活型(重复型) ，值 = 6
 *
 *  [GenSigSendType.NoSigSendType] 未定义,  值 = 7
 * */
enum class GenSigSendType (
    override val intValue: Int ,
    override val dbcKey : String,
    override val dbcValue : String
) : IDbcElement, IntEnum<GenSigSendType> {
    /** [GenSigSendType.Cyclic] 周期型, 值 = 0 */
    @GridColumnBind(headerText = "Cyclic", pattern = "Cyclic|CYCLIC|cyclic|Cycle|cycle|CYCLE")
    Cyclic (0, "Cyclic", "0" ),
    /** [GenSigSendType.OnWrite] 写入型， 值 = 1 */
    @GridColumnBind(headerText = "OnWrite", pattern = "(OnWrite|onWrite)((?!((With|with|WITH)\\s*(Repetition|REPETITION|repetition)))")
    OnWrite (1, "OnWrite", "1"),
    /**  [GenSigSendType.OnWriteWithRepetition] 写入型(重复型) ， 值 = 2 */
    @GridColumnBind(headerText = "OnWrite", pattern = "(OnWrite|onWrite)\\s*(With|with|WITH)\\s*(Repetition|REPETITION|repetition)")
    OnWriteWithRepetition (2, "OnWriteWithRepetition", "2"),
    /** [GenSigSendType.OnChange] 变化型 ，值 = 3 */
    @GridColumnBind(headerText = "OnChange", pattern = "(OnChange|onChange)((?!((With|with|WITH)\\s*(Repetition|REPETITION|repetition)))")
    OnChange (3, "OnChange", "3"),
    /** [GenSigSendType.OnChangeWithRepetition] 变化型(重复型) ，值 = 4 */
    @GridColumnBind(headerText = "OnChangeWithRepetition", pattern = "(OnChange|onChange)\\s*(With|with|WITH)\\s*(Repetition|REPETITION|repetition)")
    OnChangeWithRepetition (4, "OnChangeWithRepetition", "4"),
    /** [GenSigSendType.IfActive] 激活型 ，值 = 5 */
    @GridColumnBind(headerText = "IfActive", pattern = "(IfActive|ifActive)((?!((With|with|WITH)\\s*(Repetition|REPETITION|repetition)))")
    IfActive (5, "IfActive", "5"),
    /** [GenSigSendType.IfActiveWithRepetition] 激活型(重复型) ，值 = 6 */
    @GridColumnBind(headerText = "IfActiveWithRepetition", pattern = "(IfActive|ifActive)\\s*(With|with|WITH)\\s*(Repetition|REPETITION|repetition)")
    IfActiveWithRepetition (6, "IfActiveWithRepetition", "6"),
    /** [GenSigSendType.NoSigSendType] 未定义,  值 = 7 */
    NoSigSendType (7, "NoSigSendType", "7");
}