# 从零构建一个车载通信中间件：smart-dbc 设计与实现

> 这是一篇项目复盘。我会讲清楚：为什么做、怎么做的、难点在哪、以及我从中获得了什么。

---

## 一、背景：DBC 是什么，为什么需要中间件

在汽车电子行业，**DBC（CAN Database）**是 Vector 公司定义的一种文本格式，用于描述 CAN 总线上的消息和信号的布局规则。简单说，DBC 就是 CAN 通信的"字典"——没有它，你拿到一串 `0x1A2B3C4D...` 的十六进制报文，根本不知道它们代表发动机转速还是车门状态。

行业中处理 DBC 的痛点很明显：

1. CANoe / CANalyzer 可以图形化查看，但**无法集成到嵌入式终端**（如车载大屏、TBOX）
2. 开源库大多只支持**读取**，不能编辑并**写回** DBC 文件
3. 对 **Motorola 字节序**的支持要么缺失、要么有 bug
4. 做 CAN 报文编解码时，每个信号都要**手写移位运算**，几十个信号反复写、容易出错
5. 不同工具链之间 DBC 文件的**中文编码不一致**（CANoe 用 GBK，TSMaster 用 UTF-8），互相打开就乱码

smart-dbc 的定位是一个**车载通信中间件**。它的目标有三件事：完整读写 DBC、基于 DBC 自动完成 CAN 报文编解码、通过注解将信号绑定到业务字段。

---

## 二、从零开始：六个步骤

整个项目的构建过程，我把它分成六个阶段。下面按实际开发顺序逐一展开。

---

## 第 1 步：定义数据模型 —— DBC 的内部表示

第一步也是最基础的一步：得先有数据结构来表达一个 DBC 文件的内容。

### 1.1 分析 DBC 文件格式

一个典型的 DBC 文件长这样：

```
VERSION ""

NS_ :
    BA_
    BA_DEF_
    BA_DEF_DEF_
    ...

BS_:

BU_: CCS AC

BO_ 2560107544 CCSToAC1: 8 CCS
 SG_ test_Signal_14 : 24|8@1+ (0.1,-5.55) [-5|20.5] ""  Cabin,CCS
 SG_ test_Signal_15 : 32|8@1+ (0.1,-5.55) [-5|20.5] ""  Cabin,CCS

BO_ 2551255586 GroupTest: 8 Test
 SG_ sig_1 : 0|8@1+ (1,0) [0|255] ""  Test

BO_TX_BU_ 2560107544 : Cabin,Test;

CM_ BO_ 2560107544 "这是消息注释";
CM_ SG_ 2560107544 test_Signal_14 "这是信号注释";

BA_DEF_ BO_  "MsgAttribute" INT -100 200;
BA_DEF_DEF_ "MsgAttribute" 50;
BA_ "MsgAttribute" BO_ 2560107544 100;

VAL_ 2560107544 test_Signal_14 0 "关闭" 1 "开启" ;
```

从结构上看，DBC 是一种**分段文本格式**，每行由**关键字**开头。核心实体有三个：

- **DataBaseCan**（DBC 文件本身）：包含版本、节点列表、消息集合、属性集合
- **CanMessage**（消息 / BO_）：包含消息 ID、名称、长度、信号集合
- **CanSignal**（信号 / SG_）：包含信号名、起始位、位长、字节序、数据类型、精度/偏移量、物理值范围

三者构成一个自然的树形结构：`DataBaseCan → CanMessage → CanSignal`。

### 1.2 设计决策：只读接口 vs 可变接口

Kotlin 标准库区分 `List` / `MutableList`、`Map` / `MutableMap`，这是 Kotlin 防止副作用的核心设计理念。我决定在数据模型层完全遵循这个模式。

先定义只读接口：

```kotlin
interface DataBaseCan {
    val dbcTag: String
    val version: String
    val nodeSet: Set<String>              // 不可变 Set
    val msgMap: Map<String, CanMessage>    // 不可变 Map
    // ...
    operator fun get(msgId: Int): CanMessage?
    operator fun get(msgId: Int, signalName: String): CanSignal?
}
```

再定义可变接口，把所有 `val` 覆盖为 `var`，把 `Set` / `Map` 覆盖为 `MutableSet` / `MutableMap`：

```kotlin
interface MutableDataBaseCan<M, S, A> : DataBaseCan
    where M: MutableCanMessage, S: MutableCanSignal, A: MutableDbcAttributeDefinition {

    override var dbcTag: String
    override var version: String
    override var nodeSet: MutableSet<String>
    override var msgMap: MutableMap<String, M>

    fun set(canMsg: M)    // 新增可变操作
}
```

泛型约束 `where M: MutableCanMessage, S: MutableCanSignal, A: MutableDbcAttributeDefinition` 确保整个对象图的一致性：一个可变数据库里的消息一定是可变消息，可变消息里的信号一定是可变信号。

最后写唯一实现类：

```kotlin
class DataBaseCanImp : MutableDataBaseCan<CanMessageImp, CanSignalImp, DbcAttributeDefinitionImp> {
    override var dbcTag: String = "untitled"
    override var version: String = ""
    override var nodeSet: MutableSet<String> = mutableSetOf()
    override var msgMap: MutableMap<String, CanMessageImp> = mutableMapOf()
    // ...
}
```

**实际用法**体现了这个设计的价值：

```kotlin
// 构造阶段：内部用可变接口
val dbcImp = DataBaseCanImp()
dbcImp.version = "V1.0"
dbcImp.nodeSet.add("CCS")

// 对外暴露：只用只读接口
val dbc: DataBaseCan = dbcImp
// dbc.version = "V2.0"  ← 编译错误！只读接口没有 setter
```

编译器帮你拦截了对只读接口的任何修改尝试。这比 Java 里到处写 `Collections.unmodifiableList()` 干净得多。

### 1.3 枚举类型的设计

DBC 中有几个核心枚举，需要先定义：

**字节序（CanByteOrder）**：

```kotlin
enum class CanByteOrder(val dbcValue: String) {
    Intel("1"),           // Little Endian，信号位连续排列
    MotorolaMSB("0"),     // Big Endian，MSB 起始
    MotorolaLSB("0")      // CANdb++ 显示用，LSB 起始
}
```

**数据类型（CanDataType）**：

```kotlin
enum class CanDataType(val sign: String) {
    Unsigned("+"),
    Signed("-"),
    IEEE_Float(""),       // SIG_VALTYPE_ 1
    IEEE_Double("")       // SIG_VALTYPE_ 2
}
```

**CAN ID 类型（CanExternFlag）**：

```kotlin
enum class CanExternFlag(val mask: Int) {
    Standard(0x7FF),      // 11-bit CAN ID
    Extended(0x1FFFFFFF)  // 29-bit CAN ID
}
```

这些枚举都有对应的 `createBy(dbcValue)` 工厂方法，用于解析时从 DBC 文本值反向映射。

---

## 第 2 步：DBC 文件解析器 —— 把文本变成对象

有了数据模型之后，下一步是写出能解析 `.dbc` 文件的读取器。

### 2.1 编码问题首先解决

在汽车行业，CANoe 导出的 DBC 用 **GBK** 编码，TSMaster 用 **UTF-8**。两者互相打开，中文注释必然乱码。

我的方案是：在构造函数阶段自动检测编码，GBK 作为默认回退（因为 CANoe 的使用面更广）：

```kotlin
class DbcFileReader {
    val encoding: Charset

    constructor(file: File) {
        require(file.exists()) { "文件不存在" }
        require(file.name.lowercase().endsWith(".dbc")) { "后缀名必须是 .dbc" }
        this.inputStream = file.inputStream()
        // inputStream.encoding 是一个扩展属性，内部调用 juniversalchardet
        this.encoding = inputStream.encoding ?: Charset.forName("GBK")
    }
}
```

`inputStream.encoding` 是我在工具层封装的 `FileInputStream` 扩展属性，背后调用 Mozilla 的 `juniversalchardet`（通用字符编码检测库）。它读取文件的前若干个字节，通过统计分析判断编码类型。

### 2.2 逐行解析的调度框架

解析的整体思路是：**逐行读取 → 正则匹配关键字 → 分发到专用解析函数**。

```kotlin
// 匹配行首关键字的正则
val startRegex = Regex("""^(?<start>VERSION|BU_:|BO_|SG_|...)\s+""")

private fun parseLines(reader: BufferedReader): DataBaseCanImp {
    val dbc = DataBaseCanImp()
    var lineNumber = 0

    reader.forEachLine { rawLine ->
        lineNumber += 1
        val line = rawLine.trim()

        // 跳过空行和注释
        if (line.isEmpty() || line.startsWith("//") || line.startsWith("#"))
            return@forEachLine

        // 提取行首关键字
        val lineStart = startRegex.find(line)?.groups["start"]?.value
            ?: return@forEachLine

        try {
            when (lineStart) {
                "VERSION"  -> dbc.version = parseVersion(line)
                "BU_:"     -> dbc.nodeSet.addAll(parseBU(line))
                "BO_"      -> { val msg = parseBO(line); dbc.set(msg) }
                "SG_"      -> dbc.getMsgAt(dbc.msgMap.size - 1)?.let {
                                  it.set(parseSG(line).apply { longIdCode = it.longIdCode })
                              }
                "BO_TX_BU_"-> { /* 解析消息的接收节点 */ }
                "CM_"      -> parseCM(line, dbc)
                "BA_DEF_"  -> { /* 解析属性定义 */ }
                "BA_DEF_DEF_" -> parseBaDefault(line, dbc)
                "BA_"      -> parseBaValue(line, dbc)
                "VAL_"     -> parseValueTable(line, dbc)
            }
        } catch (exception: Exception) {
            error("解析报错：${exception.message} 。错误行号: $lineNumber, 行内容: $line")
        }
    }
    return dbc
}
```

这里值得注意的细节：

- **SG_ 依赖于当前 BO_**：`parseSG` 解析出的信号会被加入到 `msgMap` 中最后一条消息。这是利用了 DBC 文件总是把信号紧跟在所属消息后面的特性。
- **try-catch 包裹每一行**：捕获异常时打印行号和行内容，这对调试格式异常的 DBC 文件至关重要。
- **require() 前置校验**：每个 `parse*` 函数内部都用 `require()` 做前置校验，确保正则匹配失败时立刻抛出明确的错误信息。

### 2.3 信号行解析 —— 最复杂的一条正则

信号（SG_）行是整个 DBC 文件中最复杂的格式：

```
SG_ test_Signal_14 m2 : 24|8@1+ (0.1,-5.55) [-5|20.5] ""  Cabin,CCS
```

对应的正则表达式：

```kotlin
val sgRegex = Regex(
    """SG_\s+(?<sigName>\S+)\s+(?<group>[mM]\d*)?\s*:""" +
    """\s*(?<startBit>\S+)\s*[|]\s*(?<bitLength>\S+)\s*@\s*(?<byteOrder>[10])\s*(?<dataType>[+-])""" +
    """\s*\(\s*(?<factor>\S+)\s*,\s*(<offset>\S+)\s*\)\s*\[\s*(?<min>\S+)\s*[|]\s*(?<max>\S+)\s*]\s*""" +
    """"(?<unit>[^"]*)"\s*(?<nodeSet>.*)"""
)
```

使用了 Kotlin 正则的**命名捕获组**（`?<sigName>`、`?<startBit>` 等），解析时直接按名字取值：

```kotlin
fun parseSG(rawLine: String): CanSignalImp {
    val matchGroups = sgRegex.find(line)?.groups ?: error("正则识别异常")
    return CanSignalImp().apply {
        signalName = matchGroups["sigName"]!!.value.trim()
        startBit   = matchGroups["startBit"]!!.value.trim().toInt()
        bitLength  = matchGroups["bitLength"]!!.value.trim().toInt()
        byteOrder  = CanByteOrder.createBy(matchGroups["byteOrder"]!!.value.trim())
        dataType   = CanDataType.createBy(matchGroups["dataType"]!!.value.trim())
        factor     = matchGroups["factor"]!!.value.trim().toDouble()
        offset     = matchGroups["offset"]!!.value.trim().toDouble()
        // ...
    }
}
```

命名捕获组让代码自解释——看变量名就知道提取的是 DBC 行中的哪个字段，不需要记 "groups[3]" 是什么。

### 2.4 关于 SG_ 中 `group` 字段的处理

DBC 信号中有一个容易被忽略的字段：`m2`（矩阵分组标记）。在 DBC 标准中，它可以取值 `m0`, `m1`, `m2`, `M` 等，表示该信号属于信号矩阵中的哪个组。

我在解析时对其做了专门处理：

```kotlin
groupType = MatrixGroupType.createBy(matchGroups["group"]?.value?.trim() ?: "")
```

`MatrixGroupType` 枚举包含了 `GroupFlag("M")`、`DefaultGroup("")`、`CustomGroup("m+N")` 三种情况。

这个细节在实际项目中有意义——当 DBC 被导入 CANoe 后，分组标记会影响编辑器中的显示布局。如果库不能正确保留它，序列化出去的 DBC 就会丢失分组信息。

---

## 第 3 步：值转换层 —— 物理值与十六进制的互转

解析 DBC 只是第一步。真正核心的功能是：拿到一帧 CAN 报文（8 字节的 ByteArray），根据 DBC 定义把每个信号的值从十六进制字节中"抠"出来，换算成物理量。

### 3.1 转换流程

整个转换分为两步：

1. **Bit 层**：从 `ByteArray` 中提取信号的比特位，组合成一个 `Long`（称为**总线未处理值**）
2. **物理层**：将总线值按 `factor` 和 `offset` 换算为物理值：`physicalValue = rawValue × factor + offset`

举个例子：DBC 中定义发动机转速信号为 `16|16@1+ (0.125,0) [0|8031.875]`，含义是：
- 从 bit 16 开始，长度 16 bit
- 字节序 Intel（1），数据类型 Unsigned（+）
- `factor = 0.125`，`offset = 0`
- 物理范围 `[0, 8031.875]` rpm

如果报文该字段的原始值为 `0x4E20`（十进制 20000），则物理值 = `20000 × 0.125 = 2500 rpm`。

### 3.2 Intel 字节序的提取

Intel 格式最简单：信号所有比特位在数组中连续排列。从 `startBit` 开始，取 `bitLength` 个连续 bit 即可：

```kotlin
fun ByteArray.intelBitsToHex(startBit: Int, bitLength: Int): Long {
    return toBits()                          // ByteArray → ByteArray (每个元素是0或1)
        .copyOfRange(startBit, startBit + bitLength)  // 取连续范围
        .bitsToLong()                         // 比特数组 → Long
}
```

### 3.3 Motorola 字节序 —— 整个项目最难的部分

Intel 很简单，但 Motorola 完全是另一回事。

**问题在哪**

Motorola（Big Endian）格式下，信号的比特位不是连续排列的，而是遵循一种 **zigzag（之字形）布局**。把 8 字节的 CAN 报文想象成一个 8×8 比特矩阵：

```
       bit7  bit6  bit5  bit4  bit3  bit2  bit1  bit0
byte0   B7    B6    B5    B4    B3    B2    B1    B0
byte1  B15   B14   B13   B12   B11   B10    B9    B8
byte2  B23   B22   B21   B20   B19   B18   B17   B16
...
```

在 Intel 格式中，信号从 `startBit` 开始，直接向右（向高 bit 位）连续取就可以了。

在 Motorola MSB 格式中，信号从 MSB（最高位）开始，**从右往左**填充当前行，填满后**跳到下一行**（即上一个字节），继续从右往左。例如一个从 bit 19 开始、长度 12 的 Motorola 信号：

```
bit 19 → 18 → 17 → 16  (填满 byte2 的行)
跳行 →
bit 23 → 22 → 21 → 20  (byte3 的行)
跳行 →
bit 31 → 30 → 29 → 28  (byte4 的行)
```

这就是 zigzag——在矩阵中按"行内右到左、行间下到上"的顺序穿梭。

**核心算法：一维索引模拟二维遍历**

我的解决方案是：不构造真实的二维矩阵，而是用一个**一维索引公式**来模拟这个 zigzag 移动：

```kotlin
private fun parseMotorolaMsb(msbStartBit: Int, bitLength: Int, matrix: ByteArray): Long {
    val signalBits = ByteArray(bitLength) { 0 }
    var rowCount = 0
    var motorIndex: Int

    for (i in 0 until bitLength) {
        // 核心公式
        motorIndex = (msbStartBit - i) + (rowCount * 16)

        // 边界检查（跨行检测）：索引是 8 的倍数时，说明到了当前行末尾
        if (motorIndex % 8 == 0) { rowCount++ }

        // 提取的比特从高位（MSB）往低位（LSB）填充
        signalBits[bitLength - 1 - i] = matrix[motorIndex]
    }
    return signalBits.bitsToLong()
}
```

这个公式的理解：

- `(msbStartBit - i)`：从 MSB 位开始，每次递减，在行内从左往右移
- `(rowCount * 16)`：每次跨行后偏移 `+16`。为什么是 16？因为在字节矩阵中，从一行末尾（例如 bit 16）跳到上一行对应位置（bit 23），索引差是 `(byte2 的 bit0 → byte3 的 bit7)` = `16 - 7` ... 等一下，让我想清楚。

实际上，`+16` 的效果是这样：当你在一维数组中的 `motorIndex = 8`（即 byte1 bit0）处跨行，`rowCount` 变为 1，下一次计算时 `(rowCount * 16) = 16`，加上 `(msbStartBit - i)` 的效果，让你从 byte1 的行跳到了 byte2 的行。

关键在于：**一维数组中，两个相邻 byte 中相同 bit 位置的索引差恰好是 8**。当你在 byte1 的 bit0（索引=8）走到头，需要进入 byte2 的 bit7（索引=23），索引跳跃了 `23 - 8 = 15`。`+16` 加上 `-i` 的递减效果恰好模拟了这个跳跃。

**LSB → MSB 转换：同一个模型的正向运行**

CANdb++ 这个工具习惯以 LSB 标注起始位，但 DBC 文件标准使用 MSB。我需要在两种标注之间转换。

我没有推导封闭公式，而是用**同一个几何模型的正向遍历**来完成转换：

```kotlin
fun Int.lsbStartBitToMsb(bitLength: Int): Int {
    val lsbStartBit = this
    var rowCount = 0
    var motorIndex = 0

    for (i in 0 until bitLength) {
        // 正向遍历（+i 而不是 -i），从 LSB 走向 MSB
        motorIndex = (lsbStartBit + i) - (rowCount * 16)

        // 跨行条件相反：索引 % 8 == 7（行首）
        if (motorIndex % 8 == 7) { rowCount++ }
    }
    return motorIndex  // 最终到达的位置就是等价的 MSB 起始位
}
```

和 MSB 解析相比：
- `+i` 替代了 `-i`（方向相反）
- `-(rowCount * 16)` 替代了 `+(rowCount * 16)`（跨行方向相反）
- 跨行条件 `% 8 == 7`（行首）替代了 `% 8 == 0`（行尾）

两个算法共享同一套几何直觉，仅仅方向相反。不需要推导封闭公式，也不需要两套独立的转换逻辑。

**编码（写入）的对称性**

在报文发送时，需要把物理值编码回 `ByteArray`。编码是解码的逆过程，同样的公式，方向反过来：

```kotlin
fun combineMotorolaMsb(msbStartBit: Int, bitLength: Int, sigBits: ByteArray, matrix: ByteArray) {
    var rowCount = 0
    for (i in 0 until bitLength) {
        val motorIndex = (msbStartBit - i) + (rowCount * 16)
        if (motorIndex % 8 == 0) { rowCount++ }
        // 写入方向相反：从信号数组取，放到矩阵中
        matrix[motorIndex] = sigBits[bitLength - 1 - i]
    }
}
```

读取和写入共享完全相同的索引计算，唯一的区别是赋值方向（`matrix → signalBits` vs `signalBits → matrix`）。

---

## 第 4 步：DBC 写入 —— 把对象变回文本

能读还得能写。实际场景中经常需要编辑 DBC 后保存：修改信号范围、添加自定义属性、调整注释等。

### 4.1 设计选择：Kotlin Sequence

DBC 写入使用 `Sequence<String>` 而不是直接拼接字符串。原因是：

- **懒加载**：Sequence 是惰性求值的，不会一次性把所有行字符串构造出来
- **组合性**：每个 section 是独立的 Sequence，改一个不影响其他
- **内存友好**：大 DBC 文件不会因为构建字符串而消耗过多内存

### 4.2 八个输出 Section

```kotlin
val DataBaseCan.allSequence: Sequence<String>
    get() = sequence {
        yieldAll(dbcTitleSequence)           // ① VERSION + NS_/BS_ 模板 + BU_:
        yieldAll(msgSequence)                // ② 所有 BO_ 行 + 各自的 SG_ 行
        yieldAll(msgNodesSequence)           // ③ BO_TX_BU_ 行
        yieldAll(commentSequence)            // ④ CM_ 行（消息注释 + 信号注释）
        yieldAll(attributeDefinitionSequence)// ⑤ BA_DEF_ 行
        yieldAll(attributeDefaultSequence)   // ⑥ BA_DEF_DEF_ 行
        yieldAll(attributeValueSequence)     // ⑦ BA_ 行（按 Net→Message→Signal 排序）
        yieldAll(valueTableSequence)         // ⑧ VAL_ 行
    }
```

每一个 section 都是 `DataBaseCan` 上的一个扩展属性，返回 `Sequence<String>`。例如消息 section：

```kotlin
val DataBaseCan.msgSequence: Sequence<String>
    get() = sequence {
        for (msg in msgMap.values) {
            yield(msg.dbcValue)          // BO_ 2560107544 CCSToAC1: 8 CCS
            for (signal in msg.signalMap.values) {
                yield(signal.dbcValue)   //  SG_ test_Signal_14 : 24|8@1+ ...
            }
        }
    }
```

每个模型对象都实现了 `IDbcElement` 接口，包含 `dbcValue` 和 `dbcKey` 属性。`dbcValue` 负责将自己序列化为标准 DBC 行文本。

### 4.3 安全写入

覆盖已有文件是危险操作。`DbcFileWriter` 提供了 `safeWrite()`：

```kotlin
fun safeWrite(filePath: String) {
    val targetFile = File(filePath)
    val actualFile = targetFile.nextAvailableFile()  // 如果已存在，自动加后缀
    actualFile.bufferedWriter(charset).use { writer ->
        dbc.allSequence.forEach { line ->
            writer.write(line)
            writer.newLine()
        }
    }
}
```

`nextAvailableFile()` 在文件已存在时自动生成 `file(1).dbc`、`file(2).dbc` 等不重名的路径。

---

## 第 5 步：CAN 运行时框架 —— 注解驱动的自动绑定

前四步解决了"DBC 文件 ↔ 对象"的问题，但只完成了一半。最终目标是让使用者**不需要手写任何编解码代码**。

### 5.1 传统做法的问题

没有框架时，发一条 CAN 报文需要手动构造每一帧的每个比特：

```java
byte[] data = new byte[8];
int rpm = 2500;
int rawRpm = (int)(rpm / 0.125);  // 反算原始值
data[2] = (byte)((rawRpm >> 8) & 0xFF);
data[3] = (byte)(rawRpm & 0xFF);
// ... 对每个信号重复以上操作
```

几十个信号、几十个报文，手写这种代码既容易出错又难以维护。一旦 DBC 更新（信号位置变了），需要全局搜索修改。

### 5.2 设计目标

我想要的 API 是这样的：

```kotlin
// 1. 定义数据模型，用注解声明绑定关系
@DbcBinding(["EngineDBC"])
data class EngineModel(
    @CanBinding(0x18ABAB01, "EngineSpeed")
    var engineSpeed: Double = 0.0,

    @CanBinding(0x18ABAB01, "CoolantTemp")
    var coolantTemp: Int = 0,
) : CanCopyable<EngineModel> {
    override fun copyNew() = this.copy()
}

// 2. 一行注册
CanIo.bind(EngineModel())

// 3. 修改字段 → 自动编码 → 发送
val model = CanIo.getModel<EngineModel>()
model?.engineSpeed = 3000.0    // 修改即修改了信号值
CanIo.send(0x18ABAB01)         // 一行发送
```

一句话：**使用者只需要声明"哪个字段对应哪个信号"，框架完成剩下的一切**。

### 5.3 框架入口：CanIo 单例

```kotlin
object CanIo {
    var mMcu: IMcu? = null                          // MCU 适配器
    val dbcMap: MutableMap<String, DataBaseCan> = mutableMapOf()  // DBC 注册表
    val modelMap: MutableMap<KClass<*>, Any> = mutableMapOf()     // 已绑定的模型实例
}
```

三个核心组件：

- **`mMcu`**：底层 MCU 抽象（`IMcu` 接口，包含 `nativeSend()`、`nativeRegister()`、`nativeUnRegister()`），隔离硬件差异
- **`dbcMap`**：DBC 注册表，key 是 DBC 标签（用户自定义的别名），value 是 `DataBaseCan` 对象
- **`modelMap`**：已绑定模型的缓存，key 是模型的 `KClass`，value 是模型实例

### 5.4 注解定义

两个注解，极简：

```kotlin
// 类级别：声明本类绑定哪些 DBC
annotation class DbcBinding(val dbcTags: Array<String>)

// 字段级别：声明本字段绑定哪个消息的哪个信号
annotation class CanBinding(
    val msgId: Int = -1,           // 消息 ID，-1 表示不知道（会做慢速全搜索）
    val signalName: String = ""    // 信号名称
)
```

### 5.5 关键设计：让信号对象自己成为访问器

大多数框架在绑定时会创建独立的 `Binding` 对象来存储绑定元数据。我的做法不同——**让 `CanSignal` 直接实现 `KPropertyAccessor`**：

```kotlin
interface KPropertyAccessor {
    val originalOwnerType: KClass<*>?       // 模型类的 KClass
    val originalOwner: Any?                 // 模型实例
    val originalProperty: KProperty1<*, *>? // 绑定的字段

    fun setPropertyValue(value: Double, newOwner: Any? = null) {
        val aOwner = newOwner ?: originalOwner
        val property = originalProperty ?: return
        if (aOwner == null || aOwner::class != originalOwnerType) return

        property.isAccessible = true
        if (property is KMutableProperty1<*, *>) {
            val safeValue = value.toPropertyValue(property.returnType)
            property.setter.call(aOwner, safeValue)
        }
    }

    fun getPropertyValue(newOwner: Any? = null): Double? {
        // 对称的读操作，通过反射调用 getter
    }
}
```

`CanSignal` 继承 `KPropertyAccessor`，所以每个信号对象的 `originalOwner`、`originalProperty` 这些字段直接就是绑定的元数据。不需要额外的 `Map<Signal, Binding>`。

### 5.6 bind() 方法的执行流程

```kotlin
inline fun <reified T : Any> bind(model: T) {
    val kClass = T::class

    // ① 校验：类上必须有 @DbcBinding
    val dbcBind = kClass.findAnnotation<DbcBinding>()
        ?: error("'${kClass.simpleName}' 缺少 @DbcBinding 注解")

    // ② 校验：注解中声明的 DBC 标签必须已注册
    val missingTags = dbcBind.dbcTags.filter { it !in dbcMap }
    require(missingTags.isEmpty()) { "以下 DBC 标签未注册: $missingTags" }

    // ③ 遍历所有字段
    kClass.memberProperties.forEach { property ->
        // ④ 跳过没有 @CanBinding 的字段
        val canBind = property.findAnnotation<CanBinding>() ?: return@forEach

        // ⑤ 在注册的 DBC 中查找对应的信号
        val signal = findSignal(canBind)
            ?: error("未找到信号: ${canBind.signalName}")

        // ⑥ 把绑定元数据直接写到信号对象上
        signal.originalOwnerType = kClass
        signal.originalOwner = model
        signal.originalProperty = property
    }

    // ⑦ 缓存模型
    modelMap[kClass] = model
}
```

用了 `inline fun <reified T>` 在编译期捕获 `T::class`，调用时不需要传 `Class<T>` 参数。

### 5.7 级联读写

`CanAccessor` 把 `KPropertyAccessor`（反射绑定）和 `SignalAccessor`（DBC 对象存储）合并：

```kotlin
interface CanAccessor : SignalAccessor, KPropertyAccessor {
    fun writeCanValue(value: Double, newOwner: Any? = null) {
        currentPhyValue = value              // 写 DBC 信号对象
        setPropertyValue(value, newOwner)    // 写绑定字段（反射）
    }

    fun readCanValue(newOwner: Any? = null): Double {
        return getPropertyValue(newOwner)    // 优先读绑定字段
            ?: currentPhyValue              // 回退读 DBC 信号值
    }
}
```

解码报文时，`decodeCanFrame` 调用每个信号的 `writeCanValue`，自动完成"原始值 → 物理值 → 信号对象 + 绑定字段"的级联写入。用户拿到报文后，直接读自己模型的字段即可。

---

## 第 6 步：现实世界的打磨

以上五步已经是一个能跑的通路。但在实际项目中使用时，还会遇到一些现实问题。

### 6.1 Excel 导入 DBC

很多汽车行业的工程师习惯在 Excel 中管理协议定义（一个 Sheet 放消息列表，另一个 Sheet 放信号列表），而不是直接编辑 DBC 文本。

我利用自己写的一个库 `smart-grid`（用注解将 Excel 表格映射到 Kotlin 对象）实现了 `DbcGridReader`：从 Excel 的指定 Sheet 中读取协议数据，自动装配成 `DataBaseCanImp` 对象。

`smart-grid` 提供了 `@GridSheetBind` 和 `@GridColumnBind` 注解，恰好可以标注在 `DataBaseCanImp`、`CanMessageImp`、`CanSignalImp` 这些实现类上，声明每个字段对应 Excel 表格的哪一列。这种"注解桥接"的方式让 Excel 导入和 DBC 文本导入共用同一套数据模型，不需要维护两套。

### 6.2 自定义 DBC 属性的支持

标准的 DBC 属性（如信号注释、消息发送类型）只覆盖了一部分需求。实际项目中，工程师经常用 `BA_DEF_` 定义自定义属性。例如：

```
BA_DEF_ SG_ "GenSigSendType" ENUM "Cyclic","OnWrite","OnChange";
BA_DEF_DEF_ "GenSigSendType" "Cyclic";
BA_ "GenSigSendType" SG_ 2560107544 test_Signal_14 "OnChange";
```

我在属性模型中设计了完整的类型系统：

```kotlin
enum class DbcAttributeValueType {
    INT, FLOAT, STRING, ENUM, HEX
}

interface DbcAttributeDefinition {
    val name: String
    val scope: DbcAttributeScopeDefinition    // Net / Message / Signal / Node
    val valueType: DbcAttributeValueType       // 五种值类型
    val min: Double?
    val max: Double?
    val defaultValue: Any?
    val enumValueSet: Set<String>              // ENUM 类型的可选值列表
}
```

`DbcFileReader` 能解析自定义属性，`DbcSequencer` 能写回——确保经过"读 → 改 → 写"的 DBC 文件不丢失自定义属性。

### 6.3 CAN FD 帧支持

传统 CAN 每帧最多 8 字节数据。CAN FD（Flexible Data-rate）将上限提升到 64 字节。我在 `CanFrame` 接口中预留了扩展：

```kotlin
interface CanFrame {
    val msgId: Int
    val data: ByteArray
    val sendType: CanSendType
    val remoteFlag: CanRemoteFlag
    val externFlag: CanExternFlag
    val fdFlag: CanFdFlag          // 是否为 FD 帧
}
```

编解码层面，`ByteArray` 的长度天然支持任意字节数，不需要特殊处理。

---

## 三、整体架构回顾

经过六个步骤的构建，最终形成了四层架构：

```
┌─────────────────────────────────────┐
│    CAN 运行时框架 (can/)             │  ← 注解绑定 · 收发调度 · MCU 抽象
├─────────────────────────────────────┤
│    值转换层 (valueConverter/)        │  ← 物理值 ↔ 十六进制 · Intel/Motorola
├─────────────────────────────────────┤
│    DBC IO 层 (dbc/io/)              │  ← 文件解析 · 序列化写入 · Excel 导入
├─────────────────────────────────────┤
│    DBC 数据模型 (dbc/dataModel/)     │  ← DataBaseCan / Message / Signal
└─────────────────────────────────────┘
```

层与层之间的依赖是单向的：IO 层依赖数据模型，值转换层依赖数据模型，运行时框架依赖所有下层。

---

## 四、使用示例

### 轻量模式：直接操作 DBC

```kotlin
val dbc: DataBaseCan = DbcFileReader(File("example.dbc").inputStream()).read()

// 解码报文
dbc.decodeCanFrame(canFrame)

// 按消息 ID + 信号名精确定位
dbc[0x18ABAB01, "EngineSpeed"]?.also {
    println("转速 = ${it.currentPhyValue} rpm")
}

// 修改信号值并编码发送
dbc[0x18ABAB01, "EngineSpeed"]?.currentPhyValue = 3000.0
val frame = dbc.encodeCanFrame(0x18ABAB01)
mcu.nativeSend(frame)
```

### 框架模式：注解绑定

```kotlin
CanIo.apply {
    dbcMap["EngineDBC"] = DbcFileReader(...).read().apply { dbcTag = "EngineDBC" }
    bind(EngineModel())
    mMcu = MyMcuAdapter
    mcu.nativeRegister(myListener)
}

val model = CanIo.getModel<EngineModel>()
model?.engineSpeed = 3000.0
CanIo.send(0x18ABAB01)  // 自动编码 + 发送
```

---

## 五、复盘：如果重来一次

**做得好的**：

- **Motorola 算法的一维索引设计**：读写对称、无需构造二维数组、代码量少而精。至今未遇到字节序相关的 bug
- **接口分离**：只读/可变接口的层次设计让代码非常安全，重构时编译器会帮我定位所有副作用
- **Sequence 写入**：八个 section 各自独立、惰性输出，节约内存，改任何一个不影响其他

**可以改进的**：

- **性能基准测试**：目前只在几百条消息的 DBC 上验证过，没有在万级消息的大协议上做压测
- **错误信息国际化**：`DbcFileReader` 中的报错信息全是中文，如果要开源给海外用户，应该用错误码 + 多语言说明
- **Node 属性写入**：`DbcSequencer` 中 Node 级别的属性值写入标记了 TODO，尚未实现

---

## 六、写在最后

smart-dbc 代码量不大，但每个模块都是在实际车载项目中踩过坑之后反复打磨出来的。从最基础的数据模型设计，到 Motorola 的 zigzag 算法，到运行时的注解绑定框架，每一步都在平衡"做得足够通用"和"做得足够简单"。

这个过程让我深刻体会到：**一个好的中间件，往往不是设计出来的，而是用出来的**。很多设计决策（编码检测、安全写入、信号字段级联更新）都来自于现实项目的反馈和踩坑经验。

项目地址：[github.com/shilic/smart-dbc](https://github.com/shilic/smart-dbc)

---

*作者：诚（shilic） · 2026 年 6 月*
