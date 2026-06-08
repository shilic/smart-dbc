package io.github.shilic.smartDbc.common.typeExtension

/**
 * 在Map中根据值查找所有对应的键
 *
 * 注意：Map的值可能有重复，所以返回一个集合而不是单个值
 * 时间复杂度：O(n)，需要遍历整个Map
 *
 * @param value 要查找的值
 * @return 包含所有对应键的Set，如果没找到则返回空Set
 */
fun <K, V> Map<K, V>.findKeysByValue(value: V): Set<K> =
    // 使用 == 进行值比较（会调用equals方法）
    entries.filter { it.value == value }.map { it.key }.toSet()

/**
 * 查找第一个匹配的键（如果存在）
 * 性能更好，找到第一个匹配项就返回
 */
fun <K, V> Map<K, V>.findFirstKeyByValue(value: V): K? = entries.firstOrNull { it.value == value }?.key