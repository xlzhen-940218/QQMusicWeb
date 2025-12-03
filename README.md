# QQMusicWeb for Android

## 项目概述

这是一个基于WebView的QQ音乐网页版Android应用。通过WebView嵌套QQ音乐网页，移除了网页中的弹窗广告，并添加了本地化的播放控制功能，包括底部播放控制栏和通知栏播放控制。支持音乐和MV资源的下载功能。

## 核心业务代码架构

### 1. 主Activity (`MainActivity.java`)
- **功能**：应用入口，WebView容器，集成所有核心功能
- **关键特性**：
  - WebView加载QQ音乐网页（https://i.y.qq.com/n2/m/index.html）
  - JavaScript注入，通过`JsConfig`类动态修改网页DOM
  - 音乐播放状态监听和进度获取
  - 下载功能集成（支持MP3、M4A、MP4、M3U8格式）
  - 全屏视频播放支持
  - 播放控制（上一曲、下一曲、播放/暂停）

### 2. JavaScript配置 (`JsConfig.java`)
- **功能**：动态注入JavaScript代码，操作网页DOM
- **关键特性**：
  - 隐藏广告元素和不需要的UI组件
  - 获取歌曲信息（歌名、歌手、封面、歌词、播放进度）
  - 支持不同页面（播放页、歌单页）的DOM操作
  - 提供上一曲/下一曲的JavaScript模拟点击事件

### 3. 音乐通知管理器 (`MusicNotificationManager.java`)
- **功能**：管理通知栏播放控制和底部播放控制栏
- **关键特性**：
  - 绑定`MusicNotificationService`服务
  - 显示/更新通知栏播放控制
  - 管理底部播放控制栏UI
  - 处理播放控制事件（播放/暂停、上一曲、下一曲）
  - Android 13+通知权限处理

### 4. 文件下载 (`DownloadFile.java`)
- **功能**：音乐和视频文件下载工具
- **关键特性**：
  - 支持多种媒体格式（MP3、M4A、MP4等）
  - 使用Android MediaStore API保存文件
  - 下载进度回调
  - 自动文件类型识别和MIME类型处理
  - 图片文件自动扫描到相册

### 5. 音乐信息实体 (`MusicInfo.java`)
- **功能**：音乐信息数据模型
- **属性**：
  - 歌曲名称、歌手、封面URL
  - 当前歌词、播放进度、总时长
  - 播放状态、当前音乐URL

### 6. 广播接收器
- **功能**：处理通知栏播放控制事件
- **包含类**：
  - `MusicPlayOrPauseReceiver`：播放/暂停控制
  - `MusicNextReceiver`：下一曲控制
  - `MusicPreReceiver`：上一曲控制

### 7. M3U8下载集成
- **功能**：MV资源下载（基于第三方库`jaygoo/library/m3u8downloader`）
- **特性**：
  - M3U8流媒体视频下载
  - 下载进度监控
  - 下载完成后自动分享

## 项目结构

```
app/src/main/java/com/tencent/qqmusic/web/
├── MainActivity.java                    # 主Activity
├── broadcastreceiver/                   # 广播接收器
│   ├── MusicNextReceiver.java
│   ├── MusicPlayOrPauseReceiver.java
│   └── MusicPreReceiver.java
├── config/
│   └── JsConfig.java                    # JavaScript配置
├── download/                            # 下载功能
│   ├── DownloadFile.java
│   ├── DownloadStatus.java
│   └── OnFileDownListener.java
├── entity/
│   └── MusicInfo.java                   # 音乐信息实体
├── notification/
│   └── MusicNotificationManager.java    # 通知管理器
└── service/
    └── MusicNotificationService.java    # 通知服务
```

## 核心功能实现

### 1. WebView与JavaScript通信
- 通过`WebChromeClient.onConsoleMessage()`捕获JavaScript控制台输出
- 解析特定格式的消息获取音乐信息
- 使用`evaluateJavascript()`执行JavaScript代码控制播放

### 2. 广告移除机制
- 通过JavaScript动态隐藏广告相关DOM元素
- 修改页面布局，移除顶部操作栏、底部栏等
- 根据不同页面URL应用不同的DOM操作策略

### 3. 播放控制集成
- **底部控制栏**：固定在应用底部的播放控制界面
- **通知栏控制**：系统通知栏中的播放控制按钮
- **网页控制**：通过JavaScript控制网页播放器

### 4. 下载功能
- **音频下载**：MP3、M4A格式音乐文件
- **视频下载**：MP4格式视频文件
- **M3U8下载**：流媒体视频下载和转换
- **下载管理**：进度显示、完成提示、文件分享

### 5. 权限处理
- 存储权限（文件下载）
- 通知权限（Android 13+）
- 网络权限

## 技术特点

1. **混合开发**：WebView + 原生Android功能
2. **动态DOM操作**：通过JavaScript实时修改网页内容
3. **服务绑定**：通知服务与Activity绑定通信
4. **MediaStore集成**：使用Android现代存储API
5. **M3U8流媒体支持**：第三方库集成

## 使用说明

1. 首次运行需要授予存储权限（用于下载文件）
2. Android 13+需要授予通知权限
3. 底部播放控制栏在播放音乐时自动显示
4. 下载功能通过底部下载按钮触发
5. 通知栏控制需要保持通知服务运行

## 注意事项

- 项目依赖于QQ音乐网页版，网页结构变化可能导致功能失效
- 下载功能仅用于个人学习使用，请遵守相关版权规定
- M3U8下载需要网络连接和足够的存储空间

## 开发建议

1. 定期更新`JsConfig`中的DOM选择器以适应网页变化
2. 考虑添加本地播放历史记录功能
3. 可以扩展下载管理界面
4. 添加设置选项控制功能开关

## 许可证

本项目仅供学习交流使用，请遵守相关法律法规。

---

*最后更新：2025年12月3日*
*基于QQ音乐网页版开发*
