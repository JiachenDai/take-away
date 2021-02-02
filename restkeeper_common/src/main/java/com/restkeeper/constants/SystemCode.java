package com.restkeeper.constants;

/**
 * 系统常量类
 */
public class SystemCode {

    // 口味描述
    public final static String DISH_FLAVOR ="flavor";

    //运营端账号下发队列
    public final static String SMS_ACCOUNT_QUEUE="account_queue";

    //1 集团类型  2 门店类型
    public  final  static String  USER_TYPE_SHOP="1"; //集团用户类型
    public  final  static String  USER_TYPE_STORE_MANAGER="2"; //门店管理员类型
    public  final  static String  USER_TYPE_STAFF="3";  //普通员工

    //禁用
    public  final  static int  FORBIDDEN=0;
    //开启
    public  final  static int  ENABLED=1;

    public  final  static int  DISH_TYPE_MORMAL=1; //普通菜品
    public  final  static int  DISH_TYPE_SETMEAL=2; //套餐

    // 挂账类型：1 公司 2 个人
    public final static int CREDIT_TYPE_USER = 2;
    public final static int CREDIT_TYPE_COMPANY = 1;

    public  final  static int  TABLE_STATUS_FREE=0; // 0空闲
    public  final  static int  TABLE_STATUS_LOCKED=1; // 1 锁定
    public  final  static int  TABLE_STATUS_OPEND=2; // 2 已开桌


    public  final  static  String DICTIONARY_REMARK="remark"; //字典表备注类型
    public static final int ORDER_STATUS_NOTPAY = 0;
    public static final int ORDER_SOURCE_STORE = 0;
    public static final int ORDER_STATUS_PAYED = 1;
}
