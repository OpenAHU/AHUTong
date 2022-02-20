package com.ahu.ahutong.data.reptile;


public class Constants {

    public final static String URL_TEACH_BASE = "https://jwxt0.ahu.edu.cn";
    public final static String URL_ONE_BASE = "http://one.ahu.edu.cn";
    public final static String URL_LOGIN_BASE = "https://wvpn.ahu.edu.cn";
    public final static String URL_CARD_MONEY = URL_LOGIN_BASE + "/http/77726476706e69737468656265737421fff944d226387d1e7b0c9ce29b5b/tp_up/up/subgroup/getCardMoney";
    public final static String URL_TEACH_SYSTEM = URL_LOGIN_BASE + "/https/77726476706e69737468656265737421fae05988777e69586b468ca88d1b203b";
    public final static String URL_TEACH_SCHEDULE = URL_TEACH_SYSTEM + "/xsxkqk.aspx?xh=%s&gnmkdm=N121615";
    public final static String URL_TEACH_EXAM = URL_TEACH_SYSTEM + "/xskscx.aspx?xh=%s&gnmkdm=N121604";
    public final static String URL_TEACH_MAIN = URL_TEACH_SYSTEM + "/xs_main.aspx?xh=%s&type=1";
    public final static String URL_TEACH_GRADE = URL_TEACH_SYSTEM + "/xscj_gc2.aspx?xh=%s&gnmkdm=N121605";
    public final static String URL_TEACH_ROOM = URL_TEACH_SYSTEM + "/xxjsjy.aspx?xh=%s&gnmkdm=N121611";


    public final static String KEY_WVPN_LOGIN = "77726476706e69737468656265737421fff944d226387d1e7b0c9ce29b5b/cas/login";
    public final static String KEY_ONE_LOGIN = "one.ahu.edu.cn/cas/login";

    public final static String KEY_MAIN = "m=up#act=portal/viewhome";
    public final static String KEY_TEACH_MAIN = "77726476706e69737468656265737421fae05988777e69586b468ca88d1b203b/xs_main.aspx";
    public final static String KEY_TEACH_MAIN_ONE = "jwxt0.ahu.edu.cn/xs_main.aspx";

    public final static String CMD_LOGIN = "javascript:{" +
            "$('input#un').val('%s');" +
            "$('input#pd').val('%s');" +
            "login()" +
            "}";
    public final static String CMD_GOTO_TECH = "javascript:window.location='https://wvpn.ahu.edu.cn/https/77726476706e69737468656265737421fae05988777e69586b468ca88d1b203b/login_cas.aspx'";
    public final static String CMD_GOTO_TECH_ONE = "https://jwxt0.ahu.edu.cn/login_cas.aspx";

}
