云端生成 APK 操作说明

目标：
不用本地 Android Studio，不受 Astrill / Java / Gradle 本机环境影响。
通过 GitHub Actions 在线生成 APK。

步骤：
1. 注册或登录 GitHub。
2. 新建一个仓库，例如 children-english-vocab-android。
3. 上传本项目文件夹里的全部内容。
   你应该能看到：
   - settings.gradle
   - build.gradle
   - app 文件夹
   - .github 文件夹
4. 打开仓库页面的 Actions。
5. 选择 Build Android APK。
6. 点击 Run workflow。
7. 等 3~8 分钟。
8. 构建完成后，打开本次 workflow 结果页。
9. 在 Artifacts 区域下载 children-english-vocab-v5-1-debug-apk。
10. 解压后得到 app-debug.apk。
11. 发送到安卓平板安装。

说明：
- 这是 debug APK，适合自用、测试、班级小范围分发。
- 授权码仍使用原 v4.3 同一套。
- 发音使用 Android 原生 TextToSpeech。
