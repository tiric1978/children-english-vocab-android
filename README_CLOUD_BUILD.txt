v5.2 Web Parity + TTS Engine Switch Android APK

本版原则：
- 题型、练习逻辑、界面内容以网页版 v5.2 为准。
- Android 版只在设置中额外增加“英文发音引擎”切换。
- 不再使用旧 APK 题型逻辑。

包含网页版 v5.2 的需求：
1. 练习中增加“看英文选中文”，位于“看中文选英文”后。
2. “听音写拼写”不考短语；短语仅在“听音选拼写”中考。
3. “单元一轮”确保当前题池全部词汇考过才主动结束。

Android 额外新增：
- 自动：优先浏览器TTS，失败再用Android TTS
- 浏览器TTS / WebView speechSynthesis
- Android 原生 TTS
- 关闭英文发音
- 测试全部引擎

GitHub 更新步骤：
1. 解压本包。
2. 进入 GitHub 仓库 → Add file → Upload files。
3. 上传解压后的全部文件覆盖旧版本。
4. Commit message:
   rebuild android from web v5.2 with tts switch
5. Actions 完成后下载 artifact:
   children-english-vocab-v5-2-web-parity-tts-switch-debug-apk
