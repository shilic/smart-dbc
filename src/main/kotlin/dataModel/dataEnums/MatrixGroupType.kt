package dataModel.dataEnums

import dataModel.services.IDbcElement

/** 报文矩阵中的分组类型
 *
 * [GroupFlag] 分组标志位， 返回 M ;
 *
 * [DefaultGroup] 默认分组 , 返回空字符串 ;
 *
 * [CustomGroup] 自定义组 , 显示 m + 分组号, 例如 m2 ;
 *
 *  */
sealed class MatrixGroupType : IDbcElement {
    override fun toString(): String = dbcValue
    /**  [GroupFlag] 分组标志位， 返回 M ;*/
    data object GroupFlag : MatrixGroupType() {
        override val dbcKey: String  = GroupFlag::class.java.simpleName
        override val dbcValue: String= "M"
    }
    /** [DefaultGroup] 默认分组 , 返回空字符串 ; */
    data object DefaultGroup : MatrixGroupType() {
        override val dbcKey: String  = DefaultGroup::class.java.simpleName
        override val dbcValue: String= ""
    }
    /** [CustomGroup] 自定义组 , 显示 m + 分组号, 例如 m2 ; */
    data class CustomGroup (
        /** 分组号 */
        val number: Int
    ) : MatrixGroupType() {
        override val dbcKey: String  = CustomGroup::class.java.simpleName
        override val dbcValue: String= "m$number"
    }
}