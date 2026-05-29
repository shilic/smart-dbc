package io.github.shilic.smartDbc.dbc.dataModel.models

/** DBC基本信息 */
data class DbcBaseInfo (
    /** DBC 标签 */
    val dbcTag: String = "",
    /** DBC 版本 */
    val version: String = "",
    /** DBC 描述 */
    val dbcComment: String = "",
    /** 节点列表 */
    val nodeSet: MutableSet<String> = hashSetOf(),
    /** 波特率 (单位：kbps) */
    val baudRate: Int = 500,
)
