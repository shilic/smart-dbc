package io.github.shilic.smartDbc.common.typeExtension

/** 检查集合是否没有重复元素 */
fun <T> Collection<T>.hasNoDuplicates(): Boolean {
    return this.size == this.toSet().size
}