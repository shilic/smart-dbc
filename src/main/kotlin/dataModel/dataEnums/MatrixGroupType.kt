package dataModel.dataEnums

import dataModel.services.IDbcElement

/** 报文矩阵中的分组类型 */
sealed class MatrixGroupType : IDbcElement {
    override fun toString(): String = dbcValue
    /** 分组标志位， 返回 M */
    data object GroupFlag : MatrixGroupType() {
        override val dbcKey: String  = GroupFlag::class.java.simpleName
        override val dbcValue: String= "M"
    }
    /** 默认分组 , 返回空字符串 */
    data object DefaultGroup : MatrixGroupType() {
        override val dbcKey: String  = DefaultGroup::class.java.simpleName
        override val dbcValue: String= ""
    }
    /** 自定义组，返回类似于 m1 的值 */
    data class CustomGroup (
        /** 组号 */
        val number: Int
    ) : MatrixGroupType() {
        override val dbcKey: String  = CustomGroup::class.java.simpleName
        override val dbcValue: String= "m$number"
    }
}