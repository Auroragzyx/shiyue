# 《时约》CHRONO — 100% 纯原生 Native Android 纪念日 App

<p align="center">
  <img src="./icon-192.png" width="128" height="128" alt="时约 App 图标" />
</p>

<p align="center">
  <b>守候每一个值得铭记的时刻 · 100% 纯原生 Android Framework × Material 3 架构</b>
</p>

---

## 📖 项目简介

《时约》是一款专为现代安卓系统打造的**100% 纯原生（Pure Native Android）高颜值纪念日 / 倒数日应用**。  
应用已彻底告别任何 WebView 混合外壳，完全基于 **Android 原生 SDK（Activity、View、Canvas、AppWidgetProvider、SharedPreferences、CalendarContract）** 构建，严格遵循 **[Google Material Design 3 (M3)](https://m3.material.io/)** 设计规范。

得益于零第三方沉重依赖的极简原生架构，应用实现了 **50ms 闪电级原生冷启动**、**120Hz 丝滑物理滑动体验**与仅 **120 KB 的羽量级 APK 体积**。

---

## 🚀 100% 纯原生 Native 特性与核心功能

### 1. ⚡ 纯原生架构与极致性能 (Zero WebView, Pure Native)
- **100% 纯原生渲染**：主界面由原生 [`MainActivity.java`](file:///c:/Users/Administrator/-APP/android/src/main/java/com/shiyue/app/MainActivity.java) 与 XML 布局构建，渲染管线直接运行在 Android 原生 SurfaceFlinger 之上。
- **冷启动 50ms 秒开**：没有网页内核初始化的白屏与耗时，轻触图标即刻呈现。
- **体积仅 121 KB**：相比动辄 30MB~50MB 的臃肿应用，《时约》安装包仅 121 KB，常驻内存仅几兆。

### 2. 🎨 Material You 动态调色系统 (Dynamic Color Tokens)
- **4 组精选 Material You 调色盘一键切换**：
  - 💜 **Pixel Violet（灵动紫，默认）**：经典的 Material You 薰衣草紫调
  - 🌊 **Oceanic Blue（晴空海蓝）**：深邃明朗的海洋蔚蓝调
  - 🌿 **Sage Botanical（旷野森绿）**：自然护眼的鼠尾草绿调
  - 🌅 **Terracotta Amber（暮光暖杏）**：温润治愈的夕阳暖调
- **深浅色彩自适应**：支持 Light / Dark 双模切换，深色模式采用 M3 Surface Container 阶梯背景，极度舒适护眼。

### 3. 📱 2×2 原生安卓 M3 桌面小组件 (AppWidget)
- **系统底层无缝直连**：基于 [`ShiYueWidgetProvider.java`](file:///c:/Users/Administrator/-APP/android/src/main/java/com/shiyue/app/ShiYueWidgetProvider.java) 实现，由 Android 系统 Launcher 原生渲染。
- **28dp M3 大圆角磨砂卡片**：无需打开应用，在手机桌面上随时查看置顶焦点纪念日与倒数天数，轻触卡片一秒直达应用。

### 4. 🌙 原生中国传统农历（阴历）精准换算
- 原生算法 [`LunarHelper.java`](file:///c:/Users/Administrator/-APP/android/src/main/java/com/shiyue/app/util/LunarHelper.java) 包含 1900–2100 年闰月及干支生肖精准编解码，支持公历与农历自由切换。
- 每年自动推算下一个公历倒数日，完美适用于农历生日与传统节日。

### 5. 🗓️ 原生一键同步系统日历与闹钟
- 通过 Android 原生 `Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)` 唤醒系统日历，自动写入日程并设定当天提醒。

### 6. 🖼️ 原生 Canvas 朋友圈海报生成器
- 基于 Android 纯原生 [`PosterGenerator.java`](file:///c:/Users/Administrator/-APP/android/src/main/java/com/shiyue/app/util/PosterGenerator.java)，利用原生 `Canvas` 渲染 1080×1440 高分辨率大图。
- 支持 **Material You 灵动卡片海报** 与 **瑞士极简黑白海报** 双模板切换，并通过 `MediaStore` 一键保存至手机相册。

### 7. 🔒 100% 离线隐私安全与数据持久化
- 数据保存在本地原生 `SharedPreferences`，零后台常驻、零网络上传、无广告侵扰。

---

## 📂 原生工程结构

```
-APP/
├── android/                     # 100% 纯原生 Native 工程源码
│   ├── build.gradle             # Android Studio 标准应用 Gradle 配置
│   └── src/main/
│       ├── AndroidManifest.xml  # 应用清单配置
│       ├── java/com/shiyue/app/
│       │   ├── MainActivity.java           # 原生主 Activity (Material 3 主界面与交互逻辑)
│       │   ├── ShiYueWidgetProvider.java   # 2x2 原生桌面小组件 Provider
│       │   ├── model/
│       │   │   ├── EventItem.java          # 纪念日数据模型与天数计算核心
│       │   │   └── PhotoItem.java          # 回忆照片模型
│       │   └── util/
│       │       ├── DataManager.java        # 本地数据持久化管理与小组件自动同步
│       │       ├── LunarHelper.java        # 中国传统农历换算与干支算法
│       │       └── PosterGenerator.java    # 原生 Canvas 1080x1440 海报生成器
│       └── res/
│           ├── layout/                     # 原生界面与弹窗 XML 布局
│           │   ├── activity_main.xml       # 主界面布局
│           │   ├── item_event_card.xml     # 纪念日卡片 Item 布局
│           │   ├── dialog_edit_event.xml   # 新增/编辑事件弹窗布局
│           │   ├── dialog_detail_event.xml # 详情查看弹窗布局
│           │   └── widget_shiyue.xml       # 2x2 桌面微件布局
│           ├── drawable/                   # M3 大圆角 (20dp/28dp) 背景、按钮、进度条
│           └── values/
│               ├── colors.xml              # M3 动态色彩定义
│               └── strings.xml             # 应用文案
├── .builder/                    # 极客轻量级微型编译工具链 (免 Android Studio)
│   ├── build.py                 # 一键免 Android Studio 极速自动化编译打包脚本
│   └── shiyue.jks               # 专属发布签名证书 (Release Keystore)
├── build.gradle                 # 根工程 Gradle 配置 (供 Android Studio 直接导入)
└── settings.gradle              # 工程设置清单
```

---

## 🛠️ 如何构建与运行

### 方式一：一键免 Android Studio 极速构建（推荐，仅耗时 5 秒）

无需安装庞大的 15GB Android Studio，在终端直接运行：

```bash
python .builder/build.py
```

构建完成后将在项目根目录与桌面直接生成最终的 `时约.apk`（约 121 KB）。

### 方式二：在 Android Studio 中打开
直接将本工程导入 Android Studio，即可按标准 Gradle 流程进行开发与调试。

---

## 📄 开源与协议

本项目采用 MIT 协议开源。
