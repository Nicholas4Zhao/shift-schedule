# 轮班（Shift Schedule）

Android 原生项目初始版本，使用 Kotlin + Jetpack Compose。

## 当前包含
- 三个底部页面：班表、闹钟、设置
- 月份切换、日期班别设置、批量多选与月内班别计数（当前为运行时内存状态）
- 闹钟页面的设置界面原型
- 设置页面开关原型

## 尚未完成
- 班表数据持久化（Room）
- Android AlarmManager 真正响铃、精确闹钟权限、重启恢复
- 本地音乐选择、渐强、稍后提醒
- 节假日/传统节日/节气数据
- 自定义班别、倒数日

## 构建
使用 Android Studio 打开本目录，等待 Gradle Sync 后运行 app。建议 Android Studio 使用 JDK 17。

包名：`com.example.shiftschedule`
