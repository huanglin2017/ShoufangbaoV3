package com.kupangstudio.shoufangbao.base;

import com.kupangstudio.shoufangbao.model.User;
import com.kupangstudio.shoufangbao.utils.SDCardUtils;
import java.io.File;

/**
 * Created by long on 15/11/2.
 * Copyright 2015 android_xiaobai.
 * APP里一些常用参数
 */
public class Constants {
    //http请求的host的url
    public static final String HOST_URL = "http://www.shoufangbao.com/index.php?r=apis/index";//正式服武器
    //public static final String HOST_URL = "http://192.168.1.250/www/frontend/web/index.php?r=api/index";//测试服务器
    //各个模块
    public static final String MODULE_ADVERT = "advert";//广告模块
    public static final String MODULE_DISTRICT = "district";//城市模块
    public static final String MODULE_BUILD = "build";//房源模块
    public static final String MODULE_USER = "user";//用户模块
    public static final String MODULE_CUSTOM = "customer";//客户模块
    public static final String MODULE_CONFIG = "config";//app配置模块
    public static final String MODULE_NEWS = "news";//资讯模块
    public static final String MODULE_MALL = "mall";//商城模块
    public static final String MODULE_PACKET = "packet";//红包模块
    public static final String MODULE_UPGRADE = "upgrade";//版本升级
    public static final String MODULE_COMMUNITY = "consult";//社区
    //获取验证码的类型
    public static final String SMSCODE_REGISTER = "1";//注册
    public static final String SMSCODE_REPHONE = "2";//修改号码
    public static final String SMSCODE_CHANGE_PASSWORD = "3";//忘记密码
    public static final String SMSCODE_WECHAT_LOGIN = "5";//微信登录

    //部分SharePrefrence存储的key名字
    public static final String MIUI_FIRST = "miui_first";
    public static final String HOME_SET_CITY = "home_set_city";
    public static final String IS_LOCK_OPEN = "is_lock_open";//手势解锁是否打开
    public static final String IS_LOCK_SET = "is_lock_set";//是否设置过手势解锁
    public static final String BUILD_CITY_NAME = "build_city_name";//列表城市名字
    public static final String BUILD_CITY_ID = "build_city_id";//列表城市id（APP展示有关的id）
    public static final String WIDOW_FLOATING = "window_floating";//悬浮窗
    public static final String SET_PUSH = "set_push";//推送开关
    public static final String HOT_LINE = "hot_line";//热线电话
    public static final String INVITE_URL = "invite_url";//我的邀请
    public static final String REPORT_SAFE_URL = "report_safe_url";//报备安全协议
    public static final String MMFIRST = "mmfirst"+ User.getInstance().uid;
    public static final String NAVI_SWITCH = "navi_switch";
    public static final String MAIN_ONE_URL = "main_one_url";
    public static final String MAIN_TWO_URL = "main_two_url";
    public static final String MAIN_THREE_URL = "main_three_url";
    public static final String MAIN_FOUR_URL = "main_four_url";
    public static final String MAIN_FIVE_URL = "main_five_url";
    public static final String NEWS_URL = "news_url";//资讯url
    public static final String H5_TIPS = "h5_tips";//h5开关
    public static final String JPUSH_ALIAS = "joush_alias";//极光别名设置是否成功
    public static final String BANK_CARD_NUM = "bank_card_num";//提现银行卡号
    //存储文件的文件名字

    //手机实用的常量
    public static final String VERSION_CODE = "23";//版本号
    public static final String VERSION_NAME = "V3.0";//版本名字
    public static final String IS_FROM_FIRST = "is_from_first";//是否来自首页
    public static String IMEI = "";//手机唯一标识imei
    public static final String UPDATE_URL = "";
    /**
     * app的几个存储路径（图片、数据、文件）
     */
    public static final String CACHE_PATH;
    public static final String CACHE_TMP_PATH;
    public static final String IMAGE_PATH;

    static {
        String root = SDCardUtils.getSDCardPath();
        CACHE_PATH = root + "Shoufangbao";
        CACHE_TMP_PATH = CACHE_PATH + File.separator + ".data";
        IMAGE_PATH = CACHE_PATH + File.separator + "images";
    }

    /*
    * 客户分析饼图需要数据
    * */
    public static final String[] LAYOUTITEMS = new String[]{"不限", "一居室", "两居室", "三居室", "四居室", "五居室及以上"};
    public static final String[] SIZEITEMS = new String[]{"不限", "50-100", "100-150", "150-200", "200-300", "300以上"};
    public static final String[] TYPEITEMS = new String[]{"不限", "住宅", "公寓", "别墅", "商铺", "写字楼", "其它"};
    public static final String[] PRICEITEMS = new String[]{"不限", "50w以下", "50-100w", "100-200w", "200-500w", "500-1000w", "1000w以上"};
    public static final String[] FEATUREITEMS = new String[]{"不限", "小户型", "将装修", "低总价", "品牌地产",
            "地铁房", "不限购", "自住型商品房", "刚需房", "南北通透", "学区房", "婚房", "复式"};
    public static final String[] WILLITEMS = new String[]{"高", "中", "弱", "暂无"};
    public static final String[] GENDERITEMS = new String[]{"先生", "女士"};
    public static final String[] REPORTITEMS = new String[]{"全部客户", "暂未报备", "已经报备", "已经确认", "已经带看", "已经认购", "已经成交", "已经结佣", "交易终止"};
    public static final String[] STATEITEMS = new String[]{"暂未报备", "已经报备", "已经确认", "已经带看", "已经认购", "已经成交", "已经结佣", "交易终止"};
    public static final int GENDER_MAN = 1;//客户性别
    public static final int GENDER_WOMEN = 2;

    //客户更新数据时候需要
    public static int CUSTOMDATAACTION;
    public static final int CUSTOM_ADD = 0;
    public static final int CUSTOM_UPDATA = 1;

    //客户跟进更新数据需要
    public static final int TYPE_EDIT = 1;//手动的
    public static final int TYPE_AUTO = 2;//自动的
    public static final int TYPE_ADD = 3;//添加客户

    public static final int TYPE_EDIT_CUSTOM = 4;//编辑客户
    public static final int INVALIDE_ID = -1;//存储在本地的

    //判断报备的位置
    public static final int REPORT_CUSTOM = 1;//在客户里面报备
    public static final int REPORT_BUILD = 2;//在房源详情里面宝贝

    //客户报备
    public static final int REPORT_STATUS_END = 6;//已结束

    //添加客户提示通讯录对话框
    public static final String CONTACT_TIP = "contact_tip";

    //任务
    public static final String TASK_USERINFO = "task_userinfo";
    public static final String TASK_SCHEDULE = "task_schedule";//
    public static final String TASK_CHECKSCH  = "task_checksch";
    public static final String TASK_CUSMSG = "task_addcustom";
    public static final String TASK_CUSFOLLOW = "task_addcusfollow";
    public static final String TASK_CUSCHART = "task_cuschart";
    public static final String TASK_BUSINESS = "task_addbusiness";
    public static final String TASK_SHARE = "task_share";
    public static final String TASK = "task";
    public static final String TASKCONTENT = "taskcontent";
    public static final String TASKTIME = "tasktime";
    public static final String TASKNUM = "tasknum";
    public static final String TASKPARAMETER = "taskparameter";
    public static final String TASKTITLE = "tasktitle";
    public static final String TASKTIP = "trasktip";
    public static final String TASKTRUE = "trasktrue";
    public static final String TASKFALSE = "traskfalse";
    public static final String TASKID = "traskid";
    public static final String TASKCHOICEA = "traskchoicea";
    public static final String TASKCHOICEB = "traskchoiceb";
    public static final String TASKCHOICEC = "traskchoicec";
    public static final String TASKISANSWER = "taskisanswer";

    //游戏开关,签到规则奖励
    public static final String GAMEURL = "game_url";
    public static final String NAVIGAME = "game";
    public static final String ACTIVITURL = "activity_url";
    public static final String NAVIACTIVITY = "activity";
    public static final String SIGNRULE = "sign_rule";
    public static final String SIGNMONEY = "sign_money";
    public static final String ISMEET= "ismeet";

    //1：登录注册页面
    public static final int TYPELOGIN = 1;
    //2：楼盘详情页面
    public static final int TYPEBUILDDETAIL = 2;
    //3：楼盘列表页面
    public static final int TYPEBUILD = 3;
    //4：合作品牌页面
    public static final int TYPEDEVELOP = 4;
    //5：客户首页
    public static final int TYPECUSTOM = 5;
    //6：个人中心首页
    public static final int TYPECENTER = 6;
    //7：身份认证页面
    public static final int TYPEIDAUTH = 7;
    //8：宣传页制作页面
    public static final int TYPEH5SHOW = 8;
    //9：意见反馈页面
    public static final int TYPESENDADVICE = 9;
    //10：邀请好友页面
    public static final int TYPEINENTFRIEND = 10;
    //11：社区模块首页
    public static final int TYPESOCIAL = 11;
    //12：积分商城首页
    public static final int TYPEPOINTMALL = 12;
    //13：单个url
    public static final int TYPEURL = 13;
    //添加积分
    public static final int TYPEINTEGRAL = 14;

    //首页接收外界页面跳转进入所需参数
    public static final String PARM_SHOW = "parmshow";
    public static final String PARM_BID = "parmbid";
    public static final String PARM_NEWSID = "parmnewsid";
    //积分规则
    public static final String INTEGRAL_RULE = "https://www.shoufangbao.com/index.php?r=appweb/integrate";
    //帮助中心
    public static final String HELPCENTER = "http://www.shoufangbao.com/index.php?r=appweb/help";
    //定义缩放动画效果
    public final static int KEY_FIRSTPAGE = 1;
    public final static int KEY_SECONDPAGE = 2;
    public final static int KEY_LAYOUT_ID = 3;

    //保存本次定位数据
    public final static String CURRENTLAT = "lat";
    public final static String CURRENTLNG = "lng";
    public final static String CURRENTADDR = "address";
    public final static String CURRENTCITY = "current_city";
    public final static String PERSONALORDER = "personal_order";
}
