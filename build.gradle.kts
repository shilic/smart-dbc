import java.util.Properties

plugins {
    kotlin("jvm") version "2.2.0"
    /* 对应 publishing 节点; 使用传统方式发布软件包 */
    `maven-publish`
    // 添加原生签名插件，用于GPG签名
    signing
    /* 使用社区插件 com.vanniktech.maven.publish 发布软件包:
    * JVM : 必须 17 以上 (在项目结构中修改SDK级别, 不是修改语言级别)，
    * Kotlin : 2.2.0以上；
    * gradle : com.vanniktech.maven.publish 插件 0.36.0 调用了 ProjectLayout.getSettingsDirectory() 方法，该方法在 Gradle 8.12 才引入;
    * 我TM服了。*/
    id("com.vanniktech.maven.publish") version "0.36.0"
}
/* ======================= 填写个人信息 ============================= */
val githubUser = "shilic"
/* 版本号  !!! 严禁 -SNAPSHOT */
version = "1.0.11"
val mDescription = "smart-dbc 是一个CAN协议车载通信中间件（Kotlin/JVM 库），提供完整的 DBC 文件转换、解析、生成、编辑 能力，" +
        "并在此基础上封装了一套 CAN 通信框架；" +
        "支持通过注解将数据模型字段与 DBC 信号自动绑定，实现 CAN 报文的快速编解码 (从总线值到物理值, 以及从物理值到总线值)。" +
        "适用于汽车电子、车载网络等需要处理 CAN 总线协议的业务场景。使用Kotlin编写，同时兼容 java和kotlin。"

/* 组织机构的名称必须是 io.github.<你的github名称>，除非你有你自己的域名; maven中心会校验你是否拥有这个域名，否则一律挂到 github 下 */
group = "io.github.$githubUser"
/** 从 settings.gradle.kts 文件取值过来 */
val artifactId: String = rootProject.name
/** 提取个人的链接，方便统一修改 */
val myGit: String = "github.com/shilic/$artifactId"
/** 复用我的POM */
val myPom: MavenPom.() -> Unit = {
    name = artifactId
    description = mDescription
    url = "https://$myGit"
    licenses {
        license {
            name = "The Apache License, Version 2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
        }
    }
    developers {
        developer {
            id = "诚"
            name = "诚"
            email = "985478238@qq.com"
        }
    }
    scm {
        url = "https://$myGit"
        connection = "scm:git:git://$myGit.git"
        developerConnection = "scm:git:ssh://$myGit.git"
    }
}
/* 仓库们, 构建脚本会在里边定义的仓库中寻找依赖 */
repositories {
    mavenCentral()
}
/* 使用 mavenPublishing 发布到 Maven Central，签名、源码包、文档包均由插件自动处理 */
mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(group.toString(), artifactId, version.toString())
    pom(myPom)
}
/* 追加 GitHubPackages 发布目标; com.vanniktech.maven.publish 插件 已经打包了发布内容，所以这里只需要追加远程仓库。 */
afterEvaluate {
    /* 从 GRADLE_USER_HOME 读取全局 gradle.properties (存放 git 凭证) !!! 不要把密钥放到仓库里上传到 github */
    val globalProps: Properties = Properties().apply {
        gradle.gradleUserHomeDir.resolve("gradle.properties")
            .takeIf(File::exists)?.reader()?.use(::load)
    }
    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/$githubUser/${artifactId}")
                credentials {
                    username = globalProps.getProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR") ?: ""
                    password = globalProps.getProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN") ?: ""
                }
            }
            /*  // 使用 Gitea 自建的远程仓库
             maven {
                 // 使用 Gitea 自建的远程仓库，名称强制指定为 Gitea
                 name = "Gitea"
                 url = uri("http://你的内网网址:你的端口号/api/packages/你的gitea名/maven")
                 // http 链接需要强制使用 isAllowInsecureProtocol = true
                 isAllowInsecureProtocol = true
                 // 设置仓库凭证
                 credentials(HttpHeaderCredentials::class) {
                     // Gitea 规定，名称强制为 Authorization
                     name = "Authorization"
                     // Gitea 的个人访问令牌和 github 类似，到网站上自己去生成一个。
                     value = "token ${globalProps.getProperty("gitea.token")}"
                 }
                 // 以下代码为固定的
                 authentication {create("header", HttpHeaderAuthentication::class)}
             }
             */
        }
    }
}
/* 项目依赖 */
dependencies {
    // ========== 核心依赖 ==========
    implementation(kotlin("stdlib"))
    // ========== 测试依赖 ==========
    testImplementation(kotlin("test"))
    // ========== 引入自定义依赖 ==========
    // smart-grid 用于从表格识别数据进来。
    implementation("io.github.shilic:smart-grid:1.0.3")
    // smart-network-byte 用于规范网络字节数据。
    implementation("io.github.shilic:smart-network-byte:1.0.0")

    // ========== 引入excel依赖 ==========
    // 核心功能: 处理xlsx文件
    implementation("org.apache.poi:poi:5.5.1")
    // 处理xlsx文件（Office Open XML格式）
    implementation("org.apache.poi:poi-ooxml:5.5.1")
    // 识别文件编码
    implementation("com.github.albfernandez:juniversalchardet:2.4.0")
    // 序列化框架
    implementation("com.google.code.gson:gson:2.10.1")
    // ========== 反射 引入 kotlin-reflect ==========
    implementation(kotlin("reflect"))
    // =============== 测试项目引入 kotlin 协程 ==============
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}
tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(8)
}