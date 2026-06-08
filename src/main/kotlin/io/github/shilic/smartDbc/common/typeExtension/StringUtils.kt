package io.github.shilic.smartDbc.common.typeExtension

/** 判断一个字符串是否是 word 类型;  word 类型必须是 a-z 字母大小写, 数值和下划线, 并且只能是以字母和下划线开头。  */
val String.isWord : Boolean get() = matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$"))
/** 验证一个字符串是否是 word 类型;  word 类型必须是 a-z 字母大小写, 数值和下划线, 并且只能是以字母和下划线开头。不是则报错  */
fun String.requireWord() = require(isWord) { "word 类型必须是 a-z 字母大小写, 数值和下划线, 并且只能是以字母和下划线开头。\"$this\" 不是 word类型。" }
/** 验证一个字符串是否是十进制整数;  十进制整数可能以 - 开头, 包含0-9 数字, 可能包含下划线。  */
val String.isDecimal: Boolean get() = matches(Regex("^-?[0-9]+(_*[0-9]+)*$"))
/** 验证一个字符串是否是十进制整数;  十进制整数可能以 - 开头, 包含0-9 数字, 可能包含下划线。不是则报错    */
fun String.requireDecimal() = require(isDecimal) { "\"$this\" 不是一个十进制整数。"}
/** 验证一个字符串是否是十六进制数;  十六进制数必须以0x开头, 包含0-9a-fA-F, 可能包含下划线。  */
val String.isHex: Boolean get() = matches(Regex("^0[xX][0-9a-fA-F]+(_*[0-9a-fA-F]+)*$"))
/** 验证一个字符串是否是十六进制数;  十六进制数必须以0x开头, 包含0-9a-fA-F, 可能包含下划线。不是则报错   */
fun String.requireHex() = require(isHex) { "十六进制数必须以0x开头, 并且只能是0-9a-fA-F。\"$this\" 不是一个十六进制数。"}
/** 验证一个字符串是否是整数;
 *
 * 0x开头视为16进制数, 包含0-9a-fA-F;
 *
 * 不以0x开头, 视为10进制数, 包含0-9;
 *
 * 都可能包含下划线。  */
val String.isInteger: Boolean get() = matches(Regex("(^-?[0-9]+(_*[0-9]+)*$)|(^0[xX][0-9a-fA-F]+(_*[0-9a-fA-F]+)*$)"))
/** 验证一个字符串是否是整数;
 *
 * 0x开头视为16进制数, 包含0-9a-fA-F;
 *
 * 不以0x开头, 视为10进制数, 包含0-9;
 *
 * 都可能包含下划线。不是则报错   */
fun String.requireInteger() = require(isInteger) { "\"$this\" 不是一个有效的整数, 16进制数必须以0x开头, 10进制数必须不包含0x。"}
/** 验证一个字符串是否是浮点数;
 *
 * 可能以 - 开头, 0-9, 可能包含下划线, 可能包含小数点。  */
val String.isDouble: Boolean get() = matches(Regex("^-?[0-9]+(_*[0-9]+)*([.][0-9]+(_*[0-9]+)*)?$"))
/** 验证一个字符串是否是浮点数;
 *
 * 可能以 - 开头, 0-9, 可能包含下划线, 可能包含小数点。不是则报错   */
fun String.requireDouble() = require(isDouble) { "\"$this\" 不是一个有效的十进制数(或十进制浮点数)。"}
/**
 * 校验字符串是否以指定字符开头和结尾
 */
fun String.startsAndEndsWith(prefix: String, suffix: String? = null, ignoreCase: Boolean = false): Boolean {
    val mSuffix = suffix ?: prefix
    return startsWith(prefix, ignoreCase) && endsWith(mSuffix, ignoreCase)
}
/**
 * 校验字符串是否以指定字符开头和结尾, 不是则抛出异常
 */
fun String.requireStartsAndEnds(prefix: String, suffix: String? = null, ignoreCase: Boolean = false) {
    val mSuffix = suffix ?: prefix
    require(startsAndEndsWith(prefix, mSuffix, ignoreCase)) { "元素 '$this' 不是以 $prefix 和 $mSuffix 包裹。"}
}
