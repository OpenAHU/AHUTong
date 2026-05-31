package com.ahu.ahutong.data.mock

object MockScenarioRegistry {
    val defaultScenario: MockScenario by lazy { standardStudent }

    val scenarios: List<MockScenario> by lazy {
        listOf(
            standardStudent,
            examWeek,
            newStudent,
            lowBalance,
            emptyCampus,
            networkError,
            highPerformer,
            retakeStudent,
            busyLostFound,
            longheOnly,
            paymentGatewayIssue,
            expiredLogin
        )
    }

    fun find(id: String): MockScenario =
        scenarios.firstOrNull { it.id == id } ?: defaultScenario

    private val standardStudent: MockScenario by lazy {
        MockScenario(
            id = "standard_student",
            title = "标准在校生",
            subtitle = "覆盖课表、成绩、考试、一卡通、浴室、空教室和失物招领的正常数据。",
            badge = "正常",
            behavior = MockBehavior(),
            academic = MockFixtureFactory.academic(AcademicVariant.Standard),
            campus = MockFixtureFactory.campus(CampusVariant.Standard),
            payment = MockFixtureFactory.payment(PaymentVariant.Standard),
            discovery = MockFixtureFactory.discovery(DiscoveryVariant.Standard)
        )
    }

    private val examWeek: MockScenario by lazy {
        MockScenario(
            id = "exam_week",
            title = "考试周",
            subtitle = "课表收缩为答疑安排，考试列表密集，用于验证倒计时和考试状态。",
            badge = "考试",
            behavior = MockBehavior(),
            academic = MockFixtureFactory.academic(AcademicVariant.ExamWeek),
            campus = MockFixtureFactory.campus(CampusVariant.Standard),
            payment = MockFixtureFactory.payment(PaymentVariant.Standard),
            discovery = MockFixtureFactory.discovery(DiscoveryVariant.Standard)
        )
    }

    private val newStudent: MockScenario by lazy {
        MockScenario(
            id = "new_student",
            title = "新生刚入学",
            subtitle = "少量课程、暂无成绩排名、龙河校区为主，适合验证空状态和首次使用。",
            badge = "新生",
            behavior = MockBehavior(loginState = MockLoginState.Valid),
            academic = MockFixtureFactory.academic(AcademicVariant.NewStudent),
            campus = MockFixtureFactory.campus(CampusVariant.LongheFocused),
            payment = MockFixtureFactory.payment(PaymentVariant.LowBalance),
            discovery = MockFixtureFactory.discovery(DiscoveryVariant.Standard)
        )
    }

    private val lowBalance: MockScenario by lazy {
        MockScenario(
            id = "low_balance",
            title = "余额不足",
            subtitle = "一卡通和浴室账户余额偏低，支付流程返回余额不足。",
            badge = "低余额",
            behavior = MockBehavior(paymentMode = MockPaymentMode.InsufficientBalance),
            academic = MockFixtureFactory.academic(AcademicVariant.Standard),
            campus = MockFixtureFactory.campus(CampusVariant.Standard),
            payment = MockFixtureFactory.payment(PaymentVariant.LowBalance),
            discovery = MockFixtureFactory.discovery(DiscoveryVariant.Standard)
        )
    }

    private val emptyCampus: MockScenario by lazy {
        MockScenario(
            id = "empty_campus",
            title = "空数据",
            subtitle = "课表、成绩、考试、空教室、失物招领均返回空列表，用于验证空状态。",
            badge = "空",
            behavior = MockBehavior(networkMode = MockNetworkMode.Empty),
            academic = MockFixtureFactory.academic(AcademicVariant.Empty),
            campus = MockFixtureFactory.campus(CampusVariant.EmptyClassrooms),
            payment = MockFixtureFactory.payment(PaymentVariant.Standard),
            discovery = MockFixtureFactory.discovery(DiscoveryVariant.Empty)
        )
    }

    private val networkError: MockScenario by lazy {
        MockScenario(
            id = "network_error",
            title = "接口异常",
            subtitle = "所有 mock 接口模拟失败，用于验证 Toast、错误页和刷新重试。",
            badge = "错误",
            behavior = MockBehavior(
                networkMode = MockNetworkMode.Error,
                errorMessage = "Mock 场景：接口返回 500"
            ),
            academic = MockFixtureFactory.academic(AcademicVariant.Standard),
            campus = MockFixtureFactory.campus(CampusVariant.Standard),
            payment = MockFixtureFactory.payment(PaymentVariant.Standard),
            discovery = MockFixtureFactory.discovery(DiscoveryVariant.Standard)
        )
    }

    private val highPerformer: MockScenario by lazy {
        MockScenario(
            id = "high_performer",
            title = "高绩点样例",
            subtitle = "绩点和排名靠前，成绩页面展示优秀/高分混合。",
            badge = "高分",
            behavior = MockBehavior(),
            academic = MockFixtureFactory.academic(AcademicVariant.HighPerformer),
            campus = MockFixtureFactory.campus(CampusVariant.Standard),
            payment = MockFixtureFactory.payment(PaymentVariant.RichBalance),
            discovery = MockFixtureFactory.discovery(DiscoveryVariant.Standard)
        )
    }

    private val retakeStudent: MockScenario by lazy {
        MockScenario(
            id = "retake_student",
            title = "重修/补考样例",
            subtitle = "包含不及格、重修和低排名数据，用于验证成绩边界展示。",
            badge = "重修",
            behavior = MockBehavior(),
            academic = MockFixtureFactory.academic(AcademicVariant.Retake),
            campus = MockFixtureFactory.campus(CampusVariant.Standard),
            payment = MockFixtureFactory.payment(PaymentVariant.Standard),
            discovery = MockFixtureFactory.discovery(DiscoveryVariant.Standard)
        )
    }

    private val busyLostFound: MockScenario by lazy {
        MockScenario(
            id = "busy_lost_found",
            title = "失物招领活跃",
            subtitle = "分页数据较多，适合验证加载更多、刷新和删除。",
            badge = "分页",
            behavior = MockBehavior(),
            academic = MockFixtureFactory.academic(AcademicVariant.Standard),
            campus = MockFixtureFactory.campus(CampusVariant.Standard),
            payment = MockFixtureFactory.payment(PaymentVariant.Standard),
            discovery = MockFixtureFactory.discovery(DiscoveryVariant.ActiveLostFound)
        )
    }

    private val longheOnly: MockScenario by lazy {
        MockScenario(
            id = "longhe_only",
            title = "龙河校区优先",
            subtitle = "空教室和课程集中在龙河，适合验证校区切换。",
            badge = "龙河",
            behavior = MockBehavior(),
            academic = MockFixtureFactory.academic(AcademicVariant.NewStudent),
            campus = MockFixtureFactory.campus(CampusVariant.LongheFocused),
            payment = MockFixtureFactory.payment(PaymentVariant.Standard),
            discovery = MockFixtureFactory.discovery(DiscoveryVariant.OwnerPosts)
        )
    }

    private val paymentGatewayIssue: MockScenario by lazy {
        MockScenario(
            id = "payment_gateway_issue",
            title = "支付网关异常",
            subtitle = "创建订单成功但最终支付失败，用于验证支付错误状态。",
            badge = "支付",
            behavior = MockBehavior(paymentMode = MockPaymentMode.GatewayError),
            academic = MockFixtureFactory.academic(AcademicVariant.Standard),
            campus = MockFixtureFactory.campus(CampusVariant.Standard),
            payment = MockFixtureFactory.payment(PaymentVariant.GatewayIssue),
            discovery = MockFixtureFactory.discovery(DiscoveryVariant.Standard)
        )
    }

    private val expiredLogin: MockScenario by lazy {
        MockScenario(
            id = "expired_login",
            title = "登录过期",
            subtitle = "模拟登录态失效时接口错误，用于验证重新登录流程。",
            badge = "登录",
            behavior = MockBehavior(
                networkMode = MockNetworkMode.Error,
                errorMessage = "Mock 场景：登录状态已过期",
                loginState = MockLoginState.Expired
            ),
            academic = MockFixtureFactory.academic(AcademicVariant.Standard),
            campus = MockFixtureFactory.campus(CampusVariant.Standard),
            payment = MockFixtureFactory.payment(PaymentVariant.Standard),
            discovery = MockFixtureFactory.discovery(DiscoveryVariant.Standard)
        )
    }
}
