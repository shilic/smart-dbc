import java.util.Properties

// 从 GRADLE_USER_HOME 读取凭证（复用同一份）
val globalProps = Properties().apply {
    gradle.gradleUserHomeDir.resolve("gradle.properties").takeIf { it.exists() }?.reader()?.use { load(it) }
}

plugins {
    kotlin("jvm") version "2.1.0"
    /* 应用 maven-publish 插件;
   * 将项目发布到 本地maven仓库、远程maven仓库、GitHub Packages仓库 都需要使用该插件 */
    `maven-publish`
}
group = "io.github.shilic"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    /* 如果你想使用自己在github中的发布库，则必须在这里设置maven地址，同样需要从环境变量获取个人访问令牌。
    这样，gradle就能从该仓库查询该软件包了，然后就会自动下载 dependencies 中相关的依赖 */
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/shilic/smartGrid")
        credentials {
            username = globalProps.getProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR") ?: ""
            password = globalProps.getProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN") ?: ""
        }
    }
}

dependencies {
    // ========== 核心依赖 ==========
    implementation(kotlin("stdlib"))

    // ========== 测试依赖 ==========
    testImplementation(kotlin("test"))

    // ========== 引入自定义依赖 ==========
    // 使用该语句，调用自己在 GitHubPackages 上发布的软件包; smart-grid 用于从表格识别数据进来。
    implementation("io.github.shilic:smart-grid:1.0.1-SNAPSHOT")
    // 使用该语句，调用自己在 GitHubPackages 上发布的软件包; numeric-converter 用于规范网络字节数据。
    implementation("io.github.shilic:numeric-converter:1.0.2")

    // ========== 引入excel依赖 ==========
    // 核心功能: 处理xlsx文件
    implementation("org.apache.poi:poi:5.3.0")
    // 处理xlsx文件（Office Open XML格式）
    implementation("org.apache.poi:poi-ooxml:5.4.0")

    implementation("com.github.albfernandez:juniversalchardet:2.4.0")

    // ========== 反射 引入 kotlin-reflect ==========
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0")

    // =============== 引入 kotlin 协程 ==============
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}