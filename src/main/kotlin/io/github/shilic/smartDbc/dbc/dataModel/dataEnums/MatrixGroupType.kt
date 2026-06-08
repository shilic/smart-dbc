package io.github.shilic.smartDbc.dbc.dataModel.dataEnums

import io.github.shilic.smartDbc.dbc.dataModel.contract.*

/** 报文矩阵中的分组类型
 *
 * [MatrixGroupType.GroupFlag] 分组标志位， 返回 M ;
 *
 * [MatrixGroupType.DefaultGroup] 默认分组 , 返回空字符串 ;
 *
 * [MatrixGroupType.CustomGroup] 自定义组 , 显示 m + 分组号, 例如 m2 ;
 *
 *  */
sealed class MatrixGroupType : IDbcElement {
    /**  [MatrixGroupType.GroupFlag] 分组标志位， 返回 M ;*/
    data object GroupFlag : MatrixGroupType() {
        override val dbcKey: String  = GroupFlag::class.java.simpleName
        override val dbcValue: String= "M"
    }
    /** [MatrixGroupType.DefaultGroup] 默认分组 , 返回空字符串 ; */
    data object DefaultGroup : MatrixGroupType() {
        override val dbcKey: String  = DefaultGroup::class.java.simpleName
        override val dbcValue: String= ""
    }
    /** [MatrixGroupType.CustomGroup] 自定义组 , 显示 m + 分组号, 例如 m2 ; */
    data class CustomGroup (
        /** 分组号 */
        val number: Int
    ) : MatrixGroupType() {
        override val dbcKey: String  = CustomGroup::class.java.simpleName
        override val dbcValue: String= "m$number"
    }
    companion object {
        val groupRegex : Regex = Regex("""m(?<number>\d+)""")
        /** 统一通过 DBC 值创建分组类型 */
        fun createBy(dbcValue: String): MatrixGroupType {
            val dbcValueTrim = dbcValue.trim()
            return when (dbcValueTrim) {
                "M" -> GroupFlag
                "" -> DefaultGroup
                else ->  groupRegex.find(dbcValueTrim)?.groups["number"]?.value?.let { CustomGroup(it.toInt()) }
            } ?: error("${MatrixGroupType::class.simpleName}识别异常, 值必须为 'M' 或 空字符串 \"\" 或 'm + 分组号', 例如 m2 ; 错误值: $dbcValueTrim ;")
        }
    }
}