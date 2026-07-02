# smart-dbc

smart-dbc 是一个CAN协议的**车载通信中间件**（Kotlin/JVM 库），提供完整的 **DBC 文件解析、生成、编辑** 能力，并在此基础上封装了一套 **CAN 运行时框架**，支持通过注解将数据模型字段与 DBC 信号自动绑定，**实现 CAN 报文的快速编解码**。适用于汽车电子、车载网络等需要处理 CAN 总线协议的业务场景。

---

## 环境依赖

| 依赖项 | 版本要求 |
|--------|---------|
| JDK | 8+ |
| Kotlin | 2.1.0+ |
| Gradle | 8.10+（构建用） |

**核心传递依赖**（由 Gradle 自动拉取）：

- `io.github.shilic:smart-grid:1.0.1` — Excel 表格数据读取
- `io.github.shilic:numeric-converter:1.0.2` — 网络字节数据转换
- `org.apache.poi:poi:5.3.0` / `poi-ooxml:5.4.0` — Excel 文件处理
- `com.github.albfernandez:juniversalchardet:2.4.0` — 文件编码自动检测
- `org.jetbrains.kotlin:kotlin-reflect:1.9.0` — 反射支持（注解绑定）
- `com.google.code.gson:gson:2.10.1` — JSON 序列化

---

## 安装与部署

### 添加仓库

smart-dbc 发布在 **GitHub Packages**，需先在 `build.gradle.kts` 中添加仓库：

```kotlin
repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/shilic/*")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

> GitHub Packages 要求提供个人访问令牌（classic token，勾选 `read:packages`）。请将令牌配置到环境变量 `GITHUB_TOKEN` 或 `~/.gradle/gradle.properties`文件 中，**切勿提交到仓库**。

### 添加依赖

```kotlin
dependencies {
    implementation("io.github.shilic:smart-dbc:1.0.7")
    implementation("io.github.shilic:smart-grid:1.0.1")
    implementation("io.github.shilic:numeric-converter:1.0.2")
}
```

### 克隆源码

```bash
git clone https://github.com/shilic/smart-dbc.git
cd smart-dbc
./gradlew build
```

---

## 目录结构

```
smart-dbc/
├── README.md                           // 项目说明
├── build.gradle.kts                    // Gradle 构建配置（依赖、发布）
├── settings.gradle.kts                 // Gradle 项目设置
├── gradle.properties                   // Kotlin 编码风格配置
├── gradlew / gradlew.bat               // Gradle Wrapper
├── gradle/wrapper/                     // Gradle Wrapper 文件（8.10）
│
├── src/main/kotlin/io/github/shilic/smartDbc/
│   ├── can/                            // CAN 运行时框架
│   │   ├── accessors/                  // 信号值与 Kotlin 属性的桥接器
│   │   ├── binds/                      // @CanBinding / @DbcBinding 注解定义
│   │   ├── contract/                   // 框架接口（IMcu、CanListener、CanCopyable）
│   │   ├── core/                       // CanIo 框架入口（单例）
│   │   └── models/canFrame/            // CAN 帧模型（数据帧/远程帧/FD帧）
│   │
│   ├── common/                         // 通用工具
│   │   ├── customComponents/           // 自定义组件（IntEnum 等）
│   │   ├── tool/                       // CAN 工具 / 通用工具函数
│   │   └── typeExtension/              // Kotlin 标准类型扩展函数
│   │
│   ├── dbc/                            // DBC 数据模型与 IO
│   │   ├── attributes/                 // DBC 自定义属性（BA_DEF_ / BA_）
│   │   ├── dataModel/                  // 核心数据模型
│   │   │   ├── contract/              // 只读接口（DataBaseCan / CanMessage / CanSignal）
│   │   │   ├── dataEnums/             // 枚举（字节序、数据类型、帧类型等）
│   │   │   └── models/                // 可变实现类
│   │   └── io/
│   │       ├── reader/                // DBC 文件读取器 / Excel 表格读取器
│   │       └── writer/                // DBC 文件写入器 / 序列化排序器
│   │
│   └── valueConverter/                 // 信号值转换（物理值 ↔ 十六进制）
│
├── src/test/kotlin/
│   ├── canDemo/                        // CAN 框架使用示例（含模拟 MCU）
│   ├── dbcDemo/                        // DBC 文件读写示例
│   ├── demoData/                       // 测试用数据模型
│   └── toolTest/                       // 工具函数测试
│
└── src/test/resources/DBC/             // 测试用 .dbc 文件示例
```

### 架构分层

```
┌──────────────────────────────────────────┐
│          CAN 运行时框架 (can/)            │  ← 注解绑定、报文收发调度
├──────────────────────────────────────────┤
│         值转换层 (valueConverter/)        │  ← 物理值 ↔ 十六进制、Intel/Motorola 位序
├──────────────────────────────────────────┤
│         DBC IO 层 (dbc/io/)              │  ← 文件解析、序列化、Excel 读取
├──────────────────────────────────────────┤
│         DBC 数据模型 (dbc/dataModel/)     │  ← DataBaseCan / CanMessage / CanSignal
└──────────────────────────────────────────┘
```

---

## 使用说明

smart-dbc 提供了三种使用模式： **直接模式**（操作 DBC 对象）和 **绑定模式**（注解驱动的数据模型绑定）。以及DBC文件编辑。

### 模式一：直接操作 DBC 对象

适用于快速上手、无需预先定义数据模型的场景。

```kotlin
// 更详细的使用步骤请参考 src/test/kotlin/canDemo/CanTest.kt 文件

// 1. 读取 DBC 文件；自动根据里边的内容生成对应的报文和信号的对象。
val dbc: DataBaseCan = DbcFileReader({ File("example.dbc").inputStream() }).read()

// 2. 解码 CAN 报文; 自动将报文解析到对应的信号中。
dbc.decodeCanFrame(canFrame)

// 3. 按消息 ID 查看解析结果
dbc[0x18ABAB01]?.also { println(it.valueInfo) }

// 4. 按 (消息ID, 信号名) 精确定位某个信号
dbc[0x18ABAB01, "msg1_sig1"]?.also {
    println("物理值 = ${it.currentPhyValue}")
    println("文本值 = ${it.currentTextValue}")
}

// 5. 修改信号值并编码发送
dbc[0x18ABAB01, "msg1_sig1"]?.currentPhyValue = 10.0
// 快速编码报文
val frame = dbc.encodeCanFrame(0x18ABAB01)
// 调用第三方API模拟发送
mcu.nativeSend(frame)
```

### 模式二：注解绑定（推荐）

适用于已有数据模型的工程，通过注解将字段与 DBC 信号自动关联。适用于更进阶的玩法，例如可以和`Viewmodel`联动。

**第 1 步：定义数据模型**

```kotlin
// 定义一个数据类，用于映射DBC中的报文；在类上使用DbcBinding注解绑定这个数据类属于哪一个DBC
@DbcBinding(["myDbcTag"])
data class Message1(
    // 使用CanBinding注解，绑定这个字段(属性)，映射到DBC文件中的哪一个信号。
    @CanBinding(0x18ABAB01, "msg1_sig1")
    var msg1sig1: Int = 0,

    @CanBinding(0x18ABAB01, "msg1_sig2")
    var msg1sig2: Double = 0.0,

    // ...更多信号绑定，建议一个数据模型严格绑定DBC中一个ID的报文，实现精准映射。
) : CanCopyable<Message1> {
    // 可选，实现一个克隆接口，kotlin数据类自带克隆，这里只是演示。可以与 Viewmodel 等其他组件联动
    override fun copyNew() = this.copy()
}
```

**第 2 步：初始化框架**

```kotlin
// 更详细的用法请参考 src/test/kotlin/canDemo/MainTest.kt
import io.github.shilic.smartDbc.can.core.CanIo

// 在框架组件CanIo的作用域上调用
CanIo.apply {
    // 注册 DBC
    val dbc = DbcFileReader({ File("example.dbc").inputStream() }).read().apply {
        // 设置DBC标签, 这里需要和数据模型上用DbcBinding绑定的DBC标签一致。
        dbcTag = "myDbcTag"
    }
    // 将 DBC 添加到 DBC 管理器中(绑定DBC, 使其拥有CAN解析的能力)
    dbcMap[dbc.dbcTag] = dbc

    // 绑定数据模型 (需要在绑定DBC之后)（自动反射绑定所有 @CanBinding 字段）
    bind(Message1())
    // 有多个数据模型则反复绑定
    bind(Message2())

    // 注册 MCU 适配器 (使其拥有CAN收发的能力) (需要你自己根据具体的CAN收发组件去实现，通常会是你的嵌入式设备厂家提供这样一个组件给你，你自己适配进来)
    mcuAdapter = MyMcuAdapter
}
```

**第3步：监听报文**

```kotlin
// 更详细的用法请参考 src/test/kotlin/canDemo/MainTest.kt

// 注册 CAN 监听器
CanIo.register(MyListener)
object MyListener : CanListener {
    // 调用函数进行解码，将报文解码到DBC中。
    CanIo.decodeCanFrame(canFrame)
    val msg = CanIo.getModel<Message1>()?.also { println(it) }
    // 如果你使用了 viewmodel ，可以在这里使用刚才获取的数据类对 viewmodel 进行联动。
    // 或者：直接让 viewmodel 实现 CanListener 接口，并将 viewmodel 注册进来。
}
```



**第 4 步：发送报文**

```kotlin
// 直接修改绑定模型的字段值
val msg = CanIo.getModel<Message1>()
msg?.apply {
    msg1sig1 = 15
    msg1sig2 = 22.0
}

// 一条命令完成编码 + 发送 (使用绑定的默认数据类进行发送)
CanIo.send(0x18ABAB01)

// 或者指定新的数据类进行报文的发送, 这里就可以和 viewmodel 联动了
val newMsg = msg?.copy (msg1sig1 = 15,msg1sig2 = 16)
CanIo.send(0x18ABAB01, newMsg)
```

### DBC 文件编辑

```kotlin
// 读取 DBC 文件, 自动处理 GBK 编码和 UTF-8 编码, 避免文件乱码
val dbc: DataBaseCanImp = DbcFileReader({ File(ExampleDbcPath3).inputStream() }).read()

// 你可以在这里对DBC对象做一些编辑
// 例如添加信号，添加报文，添加自定义属性等等。你可以在此基础之上编写界面，来完成DBC文件的编辑。

// 将DBC对象再次序列化回 .dbc 文件中; 安全写入（自动避免覆盖已有文件）
DbcFileWriter(dbc).safeWrite(ExampleDbcPath3)
```

---

## 功能特性

- ✅ 完整的 DBC 关键字解析：`VERSION`、`BU_`、`BO_`、`SG_`、`CM_`、`VAL_`、`BA_DEF_`、`BA_DEF_DEF_`、`BA_`、`BO_TX_BU_`
- ✅ 自动检测文件编码（GBK / UTF-8），避免打开文件时乱码
- ✅ 支持 Intel、Motorola MSB、Motorola LSB 三种字节序
- ✅ 支持 Standard (11-bit) 和 Extended (29-bit) CAN ID
- ✅ 支持 CAN FD 帧
- ✅ 自定义 DBC 属性读写（五种值类型：INT / FLOAT / STRING / ENUM / HEX）
- ✅ Excel 表格导入 DBC 协议定义（通过 `smart-grid`）
- ✅ 注解驱动的数据模型绑定（`@CanBinding` / `@DbcBinding`）
- ✅ Kotlin 只读/可变接口分离，遵循 Kotlin 设计哲学
- ✅ 安全的文件写入（自动生成不重名文件，避免覆盖）

---

## 版本更新

### v1.0.7 (2026-6-25)

- 已经在安卓的 `Compose UI`上完成了技术验证
- 修复了诸多BUG

### v1.0.0（2026-06-16）

- 新增：完整的 DBC 文件解析与生成
- 新增：CAN 报文编解码（Intel / Motorola 字节序）
- 新增：注解驱动的数据模型绑定框架
- 新增：Excel 表格导入 DBC 协议
- 新增：自定义 DBC 属性支持
- 新增：CAN FD 帧支持
- 首次正式发布

---

## 常见问题（FAQ）

**Q: 为什么拉取依赖时报 401/403？**

A: GitHub Packages 需要认证。请确保已在环境变量或 `~/.gradle/gradle.properties` 中配置了有效的 GitHub 个人访问令牌（classic token，需勾选 `read:packages` 权限）。

**Q: DBC 文件中文注释乱码？**

A: 读取器内置了自动编码检测（通过 `juniversalchardet`），会自适应 GBK 或 UTF-8 编码。如果仍有问题，请确认 DBC 源文件的实际编码。

**Q: 支持哪些 DBC 版本？**

A: 支持标准 DBC 格式，涵盖 `VERSION ""`、`BU_:`、`BO_`、`SG_`、`CM_`、`VAL_`、`BA_DEF_`、`BA_DEF_DEF_`、`BA_`、`BO_TX_BU_` 等全部常用关键字。

**Q: Motorola 格式的信号值不对？**

A: 请确认 DBC 中信号的字节序定义是否正确（Motorola 为 `0`，Intel 为 `1`）。框架内部已实现 Motorola 的 zigzag 位布局解析。

---

## 版权与许可

本项目基于 [Apache License 2.0](LICENSE) 开源，详见 [LICENSE](LICENSE) 文件。

---

## 作者

- **诚（shilic）** — [GitHub](https://github.com/shilic)
- 邮箱：985478238@qq.com

---

## 贡献指南

欢迎提交 Issue 和 Pull Request。

- 报告 Bug：请在 Issue 中附上 DBC 文件片段和复现步骤
- 功能建议：欢迎在 Issue 中描述使用场景
- 代码贡献：请先开 Issue 讨论方案，再提交 PR
