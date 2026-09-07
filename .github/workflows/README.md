# CI/CD 工作流说明

> 本文档说明 `.github/workflows/` 目录下的自动化流程，对应博客《SDK的设计哲学》第八章 ——「CI/CD 是 SDK 对使用者的信任担保机制」。

---

## 一、原理

`smart-dbc` 是一个发布到 Maven Central 的 SDK。SDK 的 bug 会传播到所有使用者，因此每次发布都必须经过验证、每个版本都可追溯。整套流程由两部分构成：

- **CI（持续集成）**：每次 push / PR 跑质量门禁，拦截不合规的提交。
- **CD（持续交付）**：根据 commit message 自动决定版本号、生成 CHANGELOG、打 tag，并发布到包仓库。

版本号、CHANGELOG、tag、发布动作**全部自动完成**，不允许手动修改版本号。

---

## 二、文件清单

### `.github/workflows/` 目录

| 文件 | 作用 | 状态 |
|---|---|---|
| `ci.yml` | CI 质量门禁：编译 + 单元测试 | ✅ 即时生效 |
| `release.yml` | CD：release-please 生成版本号 + CHANGELOG + 打 tag + 发布 | ✅ 即时生效（需配 secrets） |
| `qodana_code_quality.yml` | JetBrains Qodana 静态代码扫描 | ✅ 即时生效（需配 `QODANA_TOKEN`） |
| `.keep` | 占位文件，无实际作用 | — |
| `README.md` | 本说明文档 | — |

### 仓库根目录相关文件

| 文件 | 作用 |
|---|---|
| `.release-please-config.json` | release-please 配置：`simple` 类型 + generic 更新 `build.gradle.kts`，中文 CHANGELOG 分节 |
| `.release-please-manifest.json` | 记录当前版本号（发布时由 release-please 自动更新） |
| `qodana.yaml` | Qodana 扫描配置（`qodana.starter` 规则集，JDK 17） |
| `build.gradle.kts` | 第 19 行 `version = "..."`，版本号由 release-please 自动改写 |

---

## 三、版本号自动管理机制

版本号**不是**写死在代码里的逻辑，而是委托给 GitHub 上的 [release-please](https://github.com/googleapis/release-please-action) 这个第三方 Action。仓库里只有三处「标记」：

1. **`build.gradle.kts:19`** —— 版本号行末尾的定位注释：

   ```kotlin
   version = "1.0.11" // x-release-please-version
   ```

2. **`.release-please-config.json`** —— 告诉 release-please「去改 build.gradle.kts」：

   ```json
   "packages": {
     ".": {
       "extra-files": [
         { "type": "generic", "path": "build.gradle.kts" }
       ]
     }
   }
   ```

3. **`.github/workflows/release.yml`** —— 触发这个 Action：

   ```yaml
   - uses: googleapis/release-please-action@v4
   ```

release-please 运行时读取提交历史，按 Conventional Commits 决定版本号，然后自动改写 `build.gradle.kts` 的版本号、更新 manifest、生成 `CHANGELOG.md`、打 tag。

---

## 四、完整发布流程

```
写代码 → 提交(feat/fix) → push 到 master
        ↓
CI + Qodana 跑门禁（编译、测试、静态扫描）
        ↓
release-please 自动开 release PR（内含新版本号 + CHANGELOG）
        ↓
你合并这个 release PR
        ↓
自动打 vX.Y.Z tag + 创建 GitHub Release
        ↓
触发 publish job，发布到 Maven Central + GitHub Packages
```

**你唯一的手动动作是：合并 release PR。**

---

## 五、Commit 提交规范（Conventional Commits）

格式：`<type>(<scope>): <subject>`

### 决定版本号的 3 种 type

| type | 版本变化 | 说明 |
|---|---|---|
| `fix:` | 补丁 +1（1.0.11 → 1.0.12） | 修 bug |
| `feat:` | 次版本 +1（1.0.11 → 1.1.0） | 新增功能 |
| `feat!:` / `fix!:` 或 footer 写 `BREAKING CHANGE:` | 主版本 +1（1.0.11 → 2.0.0） | 破坏性变更 |

**主版本 +1 的两种写法：**

```bash
# 写法1：类型后加感叹号
feat!: 删除旧版 API，重构报文接收接口

# 写法2：footer 声明
feat: 重构报文接收接口

BREAKING CHANGE: 删除了 tsfifo_receive_can_message_list，调用方需迁移
```

### 不升版本号的 type（只可能进 CHANGELOG）

`docs`:（文档）、`chore`:（杂项）、`style`:（格式）、`refactor`:（重构）、`perf`:（性能）、`test`:（测试）、`build`:（构建）、`ci`:（CI 改动）

> 这些提交**不会触发 release**。一次发布里至少要有一个 `feat:` 或 `fix:`，否则 release-please 不会开 release PR。

### subject 规则

- 用祈使句（动词开头）：`修复` 而不是 `修复了`
- 简短（≤ 50 字符），结尾不加句号
- 中文 subject 可以，但 `type:` 前缀必须是标准英文

### 示例

```bash
git commit -m "feat: 新增 DBC 属性读写接口"
git commit -m "fix: 修复 Motorola 格式信号值解析错误"
git commit -m "fix(parser): 修复空字节数组返回 null 的问题"
git commit -m "feat!: 删除旧版 getModel(Class) 重载，统一用 KClass"
```

> 历史提交（`修复了一些问题`、`兼容了java` 等）没有 type 前缀，release-please 识别不了。不用改历史，从下一笔提交开始遵守即可。

---

## 六、首次配置（只需做一次）

### 1. 配置 Secrets

进入对应的 `github` 仓库: `Settings → Secrets and variables → Actions → New repository secret`，添加：

| Secret                                                     | 说明                                                                                                 |
|------------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| `MAVEN_CENTRAL_USERNAME`<br>(名称和`release.yml`文件中的环境变量保持一致) | Sonatype Central Portal 的 token 用户名<br>取 `~/.gradle/gradle.properties`文件中的`mavenCentralUsername`变量 |
| `MAVEN_CENTRAL_PASSWORD`                                   | Sonatype Central Portal 的 token 密码                                                                 |
| `GPG_PRIVATE_KEY`                                          | ASCII-armored GPG 私钥（含 `-----BEGIN PGP PRIVATE KEY BLOCK-----`）                                    |
| `GPG_PASSPHRASE`                                           | 私钥密码                                                                                               |
| `QODANA_TOKEN`                                             | Qodana 云服务 token（在 [qodana.cloud](https://qodana.cloud) 生成）                                        |

> `GITHUB_TOKEN` 无需手动配置，GitHub 自动注入。

### 2. 首次推送

把新增的这些文件提交并 push 到 GitHub：

```bash
git add .github/ .release-please-config.json .release-please-manifest.json build.gradle.kts .qodana.yaml
git commit -m "ci: 接入 CI/CD 自动化发布流程"
git push
```

---

## 七、日常使用

1. 正常写代码，commit 遵守第五节规范。
2. push 后 CI + Qodana 自动跑，全绿后再考虑发版。
3. 有 `feat:`/`fix:` 提交后，release-please 会自动开 release PR。
4. 检查 release PR 里的版本号和 CHANGELOG，无误就合并。
5. 合并后自动打 tag + 发布到 Maven Central / GitHub Packages。
6. 本地 `git pull` 同步新版本号。

---

## 八、注意事项

1. **严禁手改版本号**：`build.gradle.kts` 的 `version` 行交给 release-please 全权管理。手改会导致它与 `.release-please-manifest.json` 不一致，下次发版会出错。

2. **CI 门禁部分未完全启用**：`ci.yml` 里「覆盖率 ≥ 80%」「detekt 静态扫描」「依赖漏洞扫描」「japicmp 二进制兼容性检查」四个门禁目前是**注释占位**。需在 `build.gradle.kts` 接入对应插件后取消注释：
   - 覆盖率 → `jacoco`（或 `kover`）
   - 静态扫描 → `io.gitlab.arturbosch.detekt`
   - 漏洞扫描 → `org.owasp.dependency-check`
   - 兼容性 → `com.github.siom79.japicmp`

   目前只有「编译 + 单元测试」这一道门禁即时生效。

3. **发布任务名**：`release.yml` 里 GitHub Packages 用的是聚合任务 `publishAllPublicationsToGitHubPackagesRepository`。若与你实际发布配置不一致，用 `./gradlew tasks` 确认后修改。

4. **README 版本号不同步**：README 里的 `implementation("io.github.shilic:smart-dbc:1.0.11")` 和「版本更新」章节不会随 release-please 自动更新。如需同步，可把 `README.md` 也加入 `.release-please-config.json` 的 `extra-files`。

5. **Qodana 需要 token**：未配置 `QODANA_TOKEN` 时 Qodana 工作流会失败。若暂不使用，可删除 `qodana_code_quality.yml` 或先配上 token。

6. **发布到 Maven Central 的凭据**：`MAVEN_CENTRAL_USERNAME` / `MAVEN_CENTRAL_PASSWORD` 是 Sonatype Central Portal 的 **token**（不是登录账号密码）。在 Sonatype 账户里生成。

---

## 九、故障排查

| 现象 | 原因 / 解决 |
|---|---|
| 合并 release PR 后没发布 | 检查 secrets 是否齐全，尤其 GPG 签名相关 |
| 一直不开 release PR | 提交里没有 `feat:` / `fix:`，或 commit 格式不规范 |
| publish 失败：签名错误 | `GPG_PRIVATE_KEY` 不是 armored 格式，或 `GPG_PASSPHRASE` 不对 |
| publish 失败：401 | Maven Central token 无效或权限不足 |
| Qodana 工作流红叉 | 缺少 `QODANA_TOKEN` |

---

## 参考

- [release-please-action](https://github.com/googleapis/release-please-action)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [vanniktech/gradle-maven-publish-plugin](https://github.com/vanniktech/gradle-maven-publish-plugin)
- [JetBrains Qodana](https://www.jetbrains.com/qodana/)
