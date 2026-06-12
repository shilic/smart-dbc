package io.github.shilic.smartDbc.dbc.dataModel

// -------------- 常用，标准常量 ------------
/** 常量: Vector__XXX
 *
 * 表示默认节点 */
const val Vector__XXX = "Vector__XXX"
const val VECTOR__INDEPENDENT_SIG_MSG = "VECTOR__INDEPENDENT_SIG_MSG"
const val VECTOR__INDEPENDENT_SIG_MSG_ID : Int = 0x40000000
/** 常量: VERSION
 *
 * 表示DBC文件中的版本号*/
const val VERSION = "VERSION"
/** 常量: BU_
 *
 * 表示节点*/
const val BU_ = "BU_"
/** 常量: BU_:
 *
 * 表示节点，但是后边有冒号*/
const val BU_colon = "${BU_}:"
/** 常量: BO_
 *
 * 表示报文*/
const val BO_ = "BO_"
/** 常量: SG_
 *
 * 表示信号*/
const val SG_ = "SG_"
/** 常量: BO_TX_BU_
 *
 * 表示报文的传输节点*/
const val BO_TX_BU_ = "BO_TX_BU_"
/** 常量: CM_
 *
 * 表示注释*/
const val CM_ = "CM_"
/** 常量: VAL_
 *
 * 表示值描述*/
const val VAL_ = "VAL_"
/** 常量(虚构的，DBC文件中不存在): Network
 *
 * 描述网络*/
const val Network = "Network"

// ------------- 自定义属性相关的常量 --------------
/** 常量: BA_DEF_
 *
 * 表示标准自定义属性的定义*/
const val BA_DEF_ = "BA_DEF_"
/** 常量: BA_DEF_DEF_
 *
 * 表示标准自定义属性的默认值*/
const val BA_DEF_DEF_ = "BA_DEF_DEF_"
/** 常量: BA_
 *
 * 表示自定义属性的值*/
const val BA_ = "BA_"

// ------------- 扩展属性相关的常量 --------------
/** 常量: EV_
 *
 * 表示标准环境变量 */
const val EV_ = "EV_"
/** 常量: BA_DEF_REL_
 *
 * 表示扩展的自定义属性的定义*/
const val BA_DEF_REL_ = "BA_DEF_REL_"
/** 常量: BA_DEF_DEF_REL_
 *
 * 表示扩展的自定义属性的默认值*/
const val BA_DEF_DEF_REL_ = "BA_DEF_DEF_REL_"
/** 常量: BU_EV_REL_
 *
 * 控制单元(ECU) - 环境变量,
 *
 * 官方名：Control Unit - Env. Variable */
const val BU_EV_REL_ = "BU_EV_REL_"
/** 常量: BU_BO_REL_
 *
 * 节点 - 传输(发送)报文,
 *
 * 官方名：Node - Tx Message */
const val BU_BO_REL_ = "BU_BO_REL_"
/** 常量: BU_SG_REL_
 *
 * 节点 - 接收信号,
 *
 * 官方名：Node - Mapped Rx Signal */
const val BU_SG_REL_ = "BU_SG_REL_"
