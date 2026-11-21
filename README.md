# 洗衣机

基于 [Night-stars-1/wash](https://github.com/Night-stars-1/wash) 的个人 fork，提供胖乖生活第三方客户端功能。当前个人版本为 1.2.2（versionCode 5），应用包名为 `com.srap.wash`。

使用用户提供的 Pixel 风格启动图标，包含自适应、圆形和单色图层。界面采用 Material3 大标题和低阴影卡片，跟随系统明暗主题，Android 12 及以上使用系统动态色彩。

1. 支持短信登录
2. 纯净无广告
3. 支持扫码查看设备
4. 支持查看历史订单

## 构建

需要 JDK 17、Android SDK Platform 35 和对应 Android Build Tools。配置 `JAVA_HOME`，通过 `ANDROID_HOME` 或本地 `local.properties` 的 `sdk.dir` 指定 Android SDK，使用仓库内的 Gradle wrapper 构建。

```bash
bash ./gradlew :app:assembleDebug
bash ./gradlew :app:assembleRelease :app:lintRelease :app:testReleaseUnitTest
```

Debug APK 输出到 `app/build/outputs/apk/debug/`。Release APK 输出到 `app/build/outputs/apk/release/`，启用 R8 优化。缺少个人签名配置时会生成未签名的 Release APK。

上游作者宿舍专用的电费动态模块 `elecbill` 未公开源码。构建脚本仅在 `elecbill/build.gradle.kts` 存在时包含该模块，公开源码可独立构建洗衣、登录、扫码和订单功能。

### 个人发布签名

本仓库发布的 APK 使用个人密钥。后续更新需保留同一份密钥及配置，并将它们保存在仓库外的安全备份中。

在项目根目录创建本地签名目录与密钥，密码由 `keytool` 交互输入。

```bash
mkdir -p .signing
keytool -genkeypair -keystore .signing/personal.jks -alias personal -keyalg RSA -keysize 3072 -validity 10000
```

创建 `.signing/personal.properties`，填写对应值后再执行 Release 构建。

```properties
storePassword=YOUR_STORE_PASSWORD
keyAlias=personal
keyPassword=YOUR_KEY_PASSWORD
```

`.signing/` 和密钥文件已被 Git 忽略。上游已安装版本与个人版本的签名可能不同，覆盖更新兼容性需要安装时确认。

## 上游界面截图

|<img width="884" height="1920" alt="7ff4bd4af21700138042717ef368f0c7" src="https://github.com/user-attachments/assets/5b1b8162-2cbb-4f12-bfc0-0832bfc0d6e4" />|<img width="884" height="1920" alt="855bf268cab323e01dc7fb07134d0b47" src="https://github.com/user-attachments/assets/05db3015-46e8-4815-b09c-ea7e940463a3" />|
|-|-|

## 特别鸣谢

- [LINUX DO](https://linux.do/)
