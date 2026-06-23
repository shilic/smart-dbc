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
// 项目组 ID。 组的名称必须命名为 io.github.你的github名称
group = "io.github.shilic"
// 项目版本
version = "1.0.1"

// 仓库们, 构建脚本会在里边定义的仓库中寻找依赖
repositories {
    mavenCentral()
    /* 如果你想使用自己在github中的发布库，则必须在这里设置maven地址，同样需要从环境变量获取个人访问令牌。
    这样，gradle就能从该仓库查询该软件包了，然后就会自动下载 dependencies 中相关的依赖 */
    maven {
        name = "GitHubPackages"
        // 替换 OWNER 为你的 GitHub 用户名或组织名（小写）
        url = uri("https://maven.pkg.github.com/shilic/*")
        credentials {
            username = globalProps.getProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR") ?: ""
            password = globalProps.getProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN") ?: ""
            println("username=$username,password=$password")
        }

    }
}

// 源码包 (可深入源码DEBUG)
tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

// Javadoc 包 (可查看注释)
tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().outputs)
}

// 定义发布内容 (在添加 `maven-publish` 之后，需要同步一下gradle更改才不会语法报错)
publishing {
    // 配置远程仓库们, 可以同时配置多个远程仓库
    repositories {
        maven {
            // 仓库名称 (固定参数 GitHubPackages, 不可变动 ; 该存储库指向 GitHub Packages)
            name = "GitHubPackages"
            // 仓库 github URL
            url = uri("https://maven.pkg.github.com/shilic/smart-dbc")
            // 设置仓库凭证
            credentials {
                /* 使用 GitHubPackages 发布，需要准备用户名和个人访问令牌(GITHUB_TOKEN) 。
                 * 和 maven 的.m2/settings.xml 文件类似；将 github 个人访问令牌放到环境变量中,再让gradle获取。
                 * 我准备了 4 种方式:
                 * 1. 使用 github 官方的 build.gradle.kts 的写法: 设置环境变量。
                 *     请参考 https://docs.github.com/zh/actions/tutorials/publish-packages/publish-java-packages-with-gradle
                 * 在我的电脑的环境变量中，设置 GITHUB_ACTOR 和 GITHUB_TOKEN 两个常量。设置了变量之后，需要重启IDE使其生效。
                 * 然后使用 System.getenv("GITHUB_ACTOR") 方式获取。
                 * 2. 使用C盘下的  C:\Users\Administrator\.gradle\gradle.properties 文件 读取github名称和令牌；
                 *     你需要在里边设置 gpr.user 和 gpr.key两个变量，名称可以随便起。
                 * 然后通过下边语句来获取，
                 //    val globalProps2 = Properties().apply {
                 //           File(System.getProperty("user.home"), ".gradle/gradle.properties")
                 //          .takeIf { it.exists() }?.inputStream()?.use { load(it) }
                 //       }
                 * 3. 有的教程会推荐使用 project.findProperty("gpr.key") 语句来设置github个人访问令牌。
                 *     请参考: https://blog.csdn.net/qq_41187124/article/details/156274030
                 * 实测该命令会从项目的根目录的 gradle.properties 文件读取，但是该文件需要上传到 github ，
                 * 这会暴露自己的令牌，显然这是不合适的做法。请不要使用该语句。
                 *
                 * 特别注意，不要在仓库中泄漏个人访问令牌, 不要上传到 github 中，请放到环境变量中。
                 * */
                // 4. 使用推荐的写法，从 GRADLE_USER_HOME 读取全局 gradle.properties (存放 git 凭证)
                username = globalProps.getProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR") ?: ""
                password = globalProps.getProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
        // 可以添加多个仓库
        /*
        maven {
            name = "MyNexus"
            url = uri("https://nexus.company.com/repository/maven-releases/")
            credentials {
                username = "your_username"
                password = "your_password"
            }
        }
        */
    }
    /* 定义一个标准的发布内容
     * 一个项目可以定义多个发布内容 (Multiple Publications)，例如发布不同的构件或为不同的用途提供不同的元数据。
     * 例如: 基本的jar(可调用代码)、 源码(可深入源码DEBUG)、 java-docs(可查看文档)  */
    publications {
        // 1. 定义名为 maven 的发布内容
        create<MavenPublication>("maven") {
            // kotlin("jvm") 插件内部会应用 java 插件，所以软件组件名统一叫 "java"，没有 "kotlin" 这个组件。
            // 这不是"不可变"，而是 JVM 类库的标准写法——Java 和 Kotlin 都是同一个 `components["java"]。
            from(components["java"])
            // 打包源码包
            artifact(tasks["sourcesJar"])
            // 打包注释包
            artifact(tasks["javadocJar"])
            // 可以在这里自定义 POM 内容
            pom {
                name = "smart-dbc"
                description = "更聪明的DBC，用于读写DBC，以及基于DBC做快速CAN报文编解码"
                url.set("https://github.com/shilic/smart-dbc")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("诚")
                        name.set("诚")
                        email.set("985478238@qq.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/shilic/smart-dbc.git")
                    developerConnection.set("scm:git:ssh://github.com/shilic/smart-dbc.git")
                    url.set("https://github.com/shilic/smart-dbc")
                }
            }
        }
    }
}

// 项目依赖
dependencies {
    // ========== 核心依赖 ==========
    implementation(kotlin("stdlib"))

    // ========== 测试依赖 ==========
    testImplementation(kotlin("test"))

    // ========== 引入自定义依赖 ==========
    // 使用该语句，调用自己在 GitHubPackages 上发布的软件包; smart-grid 用于从表格识别数据进来。
    implementation("io.github.shilic:smart-grid:1.0.1")
    // 使用该语句，调用自己在 GitHubPackages 上发布的软件包; numeric-converter 用于规范网络字节数据。
    implementation("io.github.shilic:numeric-converter:1.0.2")

    // ========== 引入excel依赖 ==========
    // 核心功能: 处理xlsx文件
    implementation("org.apache.poi:poi:5.3.0")
    // 处理xlsx文件（Office Open XML格式）
    implementation("org.apache.poi:poi-ooxml:5.4.0")

    implementation("com.github.albfernandez:juniversalchardet:2.4.0")
    implementation("com.google.code.gson:gson:2.10.1")

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