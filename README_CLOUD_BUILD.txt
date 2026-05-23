v5.3 Android APK 修复版

重点：
1. 不再强制 Google TTS
2. 兼容联想“天禧智能体TTS”
3. App启动自动预热语音引擎
4. TTS未完成初始化时自动重试
5. 不再卡死在“语音引擎启动中”
6. 增加 Android TTS 自检接口

GitHub 更新步骤：
1. 解压本压缩包
2. GitHub仓库 → Add file → Upload files
3. 上传全部文件覆盖旧版本
4. Commit changes:
   update android tts v5.3
5. 等待 Actions 自动构建
6. 下载 Artifacts:
   children-english-vocab-v5-3-debug-apk
7. 安装新的 app-debug.apk

平板建议：
- 保持系统默认 TTS 为“天禧智能体TTS”
- App 电池优化设置为“不限制”
- 第一次打开等待 3~5 秒后再测试发音
