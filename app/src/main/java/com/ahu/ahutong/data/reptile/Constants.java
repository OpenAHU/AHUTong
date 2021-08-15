package com.ahu.ahutong.data.reptile;


public class Constants {
    public final static String URL_LOGIN_BASE = "http://101.76.160.28:8001/cas/login?service=%s";
    public final static String URL_LOGIN_HOME = "http%3A%2F%2Fone.ahu.edu.cn%2Ftp_up%2F";
    public final static String URL_LOGIN_1 = "http://101.76.160.28:8001/cas/login;jsessionid=%s?service=%s";
    public final static String URL_CARD_MONEY = "http://one.ahu.edu.cn/tp_up/up/subgroup/getCardMoney";
    public final static String URL_TEACH_SYSTEM = "https://jwxt0.ahu.edu.cn";
    public final static String URL_LOGIN_TEACH = URL_TEACH_SYSTEM + "/login_cas.aspx";
    public final static String URL_TEACH_SCHEDULE = URL_TEACH_SYSTEM + "/xsxkqk.aspx?xh=%s&gnmkdm=N121615";
    public final static String URL_TEACH_EXAM = URL_TEACH_SYSTEM + "/xskscx.aspx?xh=%s&gnmkdm=N121604";
    public final static String URL_TEACH_MAIN = URL_TEACH_SYSTEM + "/xs_main.aspx?xh=%s&type=1";
    public final static String URL_TEACH_GRADE = URL_TEACH_SYSTEM + "/xscj_gc2.aspx?xh=%s&gnmkdm=N121605";
    public final static String URL_TEACH_ROOM = URL_TEACH_SYSTEM + "/xxjsjy.aspx?xh=%s&gnmkdm=N121611";

    public final static String COOKIE_NAME_AHU = "tp_up";
    public final static String COOKIE_LOGIN_TICKET = "CASTGC";
    public final static String COOKIE_AHU_JESESSIONID = "JSESSIONID";
    public final static String COOKIE_TEACH_SESSION = "ASP.NET_SessionId";
}
