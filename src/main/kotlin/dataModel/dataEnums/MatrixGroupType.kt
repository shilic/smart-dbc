package dataModel.dataEnums

/** 报文矩阵中的分组类型 */
sealed class MatrixGroupType {
    /** 在 DBC 中的值 */
    abstract val value: String
    /** 分组标志位 */
    data object GroupFlag : MatrixGroupType() {
        override val value = "M"
    }
    /** 默认分组 */
    data object DefaultGroup : MatrixGroupType() {
        override val value = ""
    }
    /** 自定义组 */
    data class CustomGroup(
        /** 组号 */
        val number: UInt
    ) : MatrixGroupType() {
        override val value = "m$number"
    }
}