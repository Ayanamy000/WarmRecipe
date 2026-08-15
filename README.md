# 温馨食谱（WarmRecipe）

[English](README.en.md) | 简体中文

一款面向**安卓系统**的温馨简洁风格食谱本 App，纯原生 Android（零第三方依赖，APK 仅约 58KB）。

## 功能

- **添加食谱**：先填「食材与调料」的**重量/数量**（如 `鸡蛋 · 2个`、`面粉 · 200克`），再填**分步骤**（每一步做什么 + 大约时长），可随时增删行。
- **食谱详情**：先展示食材/调料清单，再按序号展示每一步与时长，自动估算总时长；支持备注、分类、图标。
- **食谱分类**：炒菜 / 炖菜 / 蒸菜 / 油炸 / 烤肉 / 凉菜 / 汤 / 主食 / 烘焙 / 甜品 / 小吃 / 饮品 / 其他。
- **搜索与收藏**：按名称或食材搜索；★ 收藏。
- **编辑与删除**：已有食谱可修改、删除。
- **自定义主题配色**：6 套温馨配色（奶油杏 / 蜜桃粉 / 抹茶绿 / 暖橙 / 雾蓝 / 薰衣草），随时切换。
- **导入 / 导出**：单个食谱可一键分享为文本；全部食谱可导出为 JSON 备份，也能从文件导入读取。
- **预定食材**：勾选一份或多份食谱生成购物清单（同名食材自动合并），可划掉家里已有的食材并随时恢复；购买完成后进入「选定菜谱」烹饪页，点任意菜谱看做法，全部做完一键清空。
- **纯本机存储**：数据保存在手机内部存储 `recipes.json`，不上传、不联网。

## 安装到安卓手机

1. 把 `温馨食谱-v1.2.apk`（或 `WarmRecipe-v1.2.apk`）传到手机（微信/QQ 文件传输、数据线、网盘均可）。
2. 手机上点击该 APK 安装；若提示「未知来源应用」，在设置里允许「安装未知应用」即可（首次安装时系统会引导）。
3. 桌面出现「温馨食谱」图标，点开即用。

> 系统要求：Android 8.0（API 26）及以上，覆盖绝大多数安卓机型。

## 项目结构

```
食谱/
├─ app/                        # 应用源码
│  ├─ AndroidManifest.xml
│  ├─ res/                     # 布局 / 样式(6套配色) / drawable / 图标
│  └─ src/com/warmrecipes/app/ # Java 源码
│     ├─ MainActivity.java     # 首页列表 + 搜索 + 分类筛选
│     ├─ DetailActivity.java   # 详情（食材 → 步骤）
│     ├─ EditActivity.java     # 新建 / 编辑
│     ├─ ThemeActivity.java    # 主题配色选择
│     ├─ PlanSelectActivity.java   # 预定：多选食谱
│     ├─ PlanShoppingActivity.java # 购物清单页
│     ├─ PlanCookingActivity.java  # 烹饪页
│     ├─ Recipe.java           # 数据模型 + 时长估算
│     ├─ RecipeStore.java      # JSON 本机存储
│     ├─ PlanStore.java        # 预定清单（合并）
│     ├─ Palette.java          # 配色定义
│     └─ ThemeManager.java     # 主题读写/应用
├─ sdk/                        # 本地 Android SDK（build-tools 35.0.0 + android-35）
├─ tools/                      # Fetch(下载器) / MakeIcon(图标生成)
├─ build.ps1                   # 一键构建脚本（无需 Gradle）
├─ release.keystore            # 签名密钥（密码 recipe123，请妥善保管/自行更换）
└─ 温馨食谱-v1.2.apk           # 构建产物（可直接安装）
```

## 重新构建

本机需有 JDK 24；`sdk/` 内已含 build-tools 与 platform。在项目根目录执行：

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force
.\build.ps1
```

构建流程：`aapt2 编译资源 → link 生成 R.java → javac → d8 转 dex → 打包 → zipalign → apksigner 签名`。
产物输出为 `WarmRecipe-v1.2.apk`（会自动拷回项目根目录）。

> 说明：aapt2 等原生工具在 Windows 上无法打开含中文的路径，因此脚本会在系统临时目录（ASCII 路径）完成构建后把 APK 拷回。

## 已知说明

- 每步时长支持 `10分钟`、`1小时30分钟` 等写法；详情页总时长为各步时长的自动求和。
- 分类切换会自动带入对应默认图标（可再手动改选）。
- 预定清单会自动保存，重开 App 仍在；点「制作完成」后清空。
