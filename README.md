# Silent Gear (1.12.2 Community Port)

将 SilentChaos512 的 **Silent Gear** 移植/维护到 Minecraft 1.12.2 的工程。
基于原作者 `1.12` 分支（0.6.x）为代码基线，使用现代的 **RetroFuturaGradle (RFG)** 构建体系，
并持续并入新版（master / 1.20.x+）的材质与内容。

> 本项目为非官方社区移植，与原作者无隶属关系。

---

## 一、构建环境

| 组件 | 版本 | 用途 |
|------|------|------|
| JDK（运行 Gradle） | Temurin **25**（`F:\jdk25`） | RFG 2.0.3 插件以 class v69 编译，需 JDK 25 加载 |
| JDK（编译目标） | Azul **8**（由 foojay 自动 provision） | 1.12.2 字节码目标 |
| Gradle | 9.7.0（Wrapper，腾讯云镜像） | 构建入口 |
| RetroFuturaGradle | 2.0.3 | 替代已停更的 ForgeGradle 2.3 |
| Forge | 1.12.2-14.23.5.2847 | 游戏加载器 |
| MCP | stable_39 | 反编译映射 |

### 构建命令

```powershell
# PowerShell（首次/日常）
$env:JAVA_HOME = "F:\jdk25\jdk-25.0.4.1+1"
.\gradlew.bat build          # 产出 build/libs/silentgear-<ver>.jar
.\gradlew.bat runClient     # 启动开发客户端
.\gradlew.bat runServer      # 启动开发服务端
```

> 首次构建会下载 Forge userdev、MCP 映射、原版 client/server jar 并反编译 MC，
> 耗时较长（已配置腾讯云/阿里云镜像与 GTNH nexus）。

---

## 二、许可证与归属（务必遵守）

本工程合并了两份不同协议的上游代码，均保留原始版权声明：

| 上游 | 协议 | 版权 |
|------|------|------|
| Silent Gear `1.12` 分支 | **LGPL v3** | Copyright (C) 2018 SilentChaos512 |
| Silent Lib `1.12` 分支（库代码已并入 `net.silentchaos512.lib`） | **GPL v3** | Copyright (C) SilentChaos512 |
| Silent Gear `master`（新版材质/内容参考） | **MIT** | Copyright (c) 2018 SilentChaos512 |

由于 Silent Lib 为 GPL v3，合并后的整体作品须按 **GPL v3** 分发：
保留版权与许可证声明、提供完整源码。新版 MIT 内容可并入，但合并作品整体仍受 GPL v3 约束。

- 原版源码出处：https://github.com/SilentChaos512/Silent-Gear （`1.12` 与 `master` 分支）
- 本移植不使用 Mixin / ASM，以最大化与其它 1.12.2 模组的兼容性。

---

## 三、工程结构

```
F:\SilentGear\
├── build.gradle            # RFG 构建脚本
├── settings.gradle         # 插件仓库（GTNH nexus + 国内镜像）
├── gradle.properties        # mod 元数据
├── gradle/wrapper/          # Gradle 9.7 wrapper（腾讯云分发）
└── src/main/
    ├── java/net/silentchaos512/
    │   ├── gear/            # Silent Gear 模组本体
    │   └── lib/            # Silent Lib 库代码（GPL v3，并入）
    └── resources/
        ├── mcmod.info
        └── assets/silentgear/  # 材质/语言/模型
```

---

## 四、移植路线

1. ✅ 搭建 RFG + JDK25 构建环境，老版源码可编译、可打包
2. 🔄 先移植**装备**：并入新版 item 材质、补充零件/材料
3. ⏳ 再移植**矿物**：新矿石、材料生成与对应材质
4. ⏳ 验证 `runClient` 实机加载、配方与装备合成

> 可选兼容：EvilCraft 血箱修复兼容暂时移除（原 Cyclops maven 已失效），
> 调用处保留 `Loader.isModLoaded` 守卫与 TODO，后续拿到 dev jar 再接回。
