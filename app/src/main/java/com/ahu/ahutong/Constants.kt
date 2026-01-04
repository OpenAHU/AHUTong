package com.ahu.ahutong

object Constants {
    const val UPDATE_LOG = """
        1. 修复了上个版本遗留的一些bug，如：充值问题，考场查询问题，课表问题等，详见commits
        2. 更新了免责声明
        3. 完成了热更新，将爬虫类接口使用rust进行了重写，并实现了动态下发.so文件以实现热更新
    """
}
