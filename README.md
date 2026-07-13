# 安大通

安大通是一个立足于安徽大学，由安徽大学学生自发开发的集校园一卡通、电子课表、成绩查询、考试查询等实用功能于一体的App

## 技术栈

安大通正在建设 AIO（All in One）仓库，用于统一管理不同平台的客户端。

Android 客户端采用 MVVM 架构，UI 使用 Jetpack Compose 和 Material 3，以 Kotlin 为主并包含少量 Java 代码；核心校园数据能力由 Rust SDK 提供，经 JNI 接入 Android，并使用 Tokio、Axum 和 Reqwest 实现本地服务及网络访问。

HarmonyOS 客户端预计使用 ArkTS 和 ArkUI，iOS 客户端预计使用 Swift 和 SwiftUI。

## 软件展示
软件部分界面效果如下：
<div>
    <img src="pic/login_page.png" alt="登录页" width="32%">
    <img src="pic/schedule_page.png" alt="课程表" width="32%">
    <img src="pic/home_page.png" alt="主页" width="32%">
</div>
<div>
    <img src="pic/tools_page.png" alt="工具页" width="32%">
    <img src="pic/grade_page.png" alt="成绩查询" width="32%">
    <img src="pic/exam_page.png" alt="考试查询" width="32%">
</div>

更新了部分内容（2025.9）：

<div>
    <img src="pic/home_page_2025.png" alt="新主页" width="32%">
    <img src="pic/bathroom_charge.png" alt="浴室缴费" width="32%">
    <img src="pic/electricity_charge.png" alt="电控缴费" width="32%">
</div>

## A Historic Moment
An era has ended — **2025-11-01**.
![772f0c1d-bc2a-4d27-a317-9b0ee1ae2a13](https://github.com/user-attachments/assets/07512859-7523-490a-a985-c93554c67a5f)
