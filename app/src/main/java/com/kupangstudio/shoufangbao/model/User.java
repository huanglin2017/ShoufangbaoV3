package com.kupangstudio.shoufangbao.model;

import android.content.ContentValues;
import android.content.Context;
import android.widget.Toast;

import com.kupangstudio.shoufangbao.ToolsActivity;
import com.kupangstudio.shoufangbao.base.Constants;
import com.kupangstudio.shoufangbao.base.ShoufangbaoApplication;
import com.kupangstudio.shoufangbao.network.Result;
import com.kupangstudio.shoufangbao.utils.CommonUtils;
import com.kupangstudio.shoufangbao.utils.SPUtils;
import com.kupangstudio.shoufangbao.utils.StringUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;
import org.w3c.dom.UserDataHandler;

import java.util.HashMap;

import okhttp3.Call;

/**
 * 用户model类
 * Created by long on 15/11/2.
 * Copyright 2015 android_xiaobai.
 */
public class User {
    //账户类型
    public static final int TYPE_DEFAULT_USER = -1;//本地账号（请求临时账号失败）
    public static final int TYPE_TMP_USER = 22;//临时账号
    public static final int TYPE_NORMAL_USER = 23;//正式账号
    //认证状态
    public static final int USER_NOT_SUBMIT = 0;//未认证，未提交
    public static final int USER_THROUGH = 1;//已认证，已通过
    public static final int USER_UN_THROUGH = 2;//未通过
    public static final int USER_DEAL = 3;//审核中
    //经纪人标准
    public static final int NORMAL_BROKER = 1;//标准经济人
    public static final int NORMAL_BROKER_NOT = 0;//无门店经纪人

    public int userType;//账户类型
    public int style;//经纪人类型
    public int uid;//id
    public String salt;//id加密字段
    public String mobile;//手机号
    public String password;//密码
    public String realName;//真实姓名
    public int verify;//认证类型
    public int credit;//用户信用
    public int interage;//用户实时积分
    public int totalinterage;//用户积累积分
    public String level;//会员等级
    public int nextlevel;//距离下一等级积分
    public int grade;//会员等级
    public int ammount;//总金额
    public int account;//已提现金额
    public int cityId;//城市id
    public String city;//城市名字
    public int areaId;//区域id
    public String area;//区域名字
    public int districtId;//商圈id
    public String district;//商圈名字
    public String uniodId;//微信id
    public String wxFace;//微信头像
    public String wxName;//微信名字
    public String project;//门店/项目
    public String card;//名片
    public String idCard;//身份证
    public String face;//头像
    public int message;//未读信息
    public String bankCard;//银行卡
    //用户信息缓存SharePrefrence文件名字
    private static final String USERTYPE = "extgroupid";
    private static final String STYLE = "style";
    private static final String UID = "uid";
    private static final String SALT = "id";
    private static final String MOBILE = "mobile";
    private static final String PASSWORD = "password";
    private static final String REALNAME = "realname";
    private static final String VERIFY = "verify";
    private static final String CREDIT = "credit";
    private static final String LEVEL = "level";
    private static final String NEXTLEVEL = "nextlevel";
    private static final String GRADE = "grade";
    private static final String INTERAGE = "integrate";
    private static final String AMMOUNT = "ammount";
    private static final String ACCOUNT = "account";
    private static final String CITYID = "cityid";
    private static final String CITY = "city";
    private static final String AREAID = "areaid";
    private static final String AREA = "area";
    private static final String DISTRICTID = "districtid";
    private static final String DISTRICT = "district";
    private static final String VCITYID = "vcityid";
    private static final String UNIONID = "uniodid";
    private static final String WXFACE = "wxface";
    private static final String WXNAME = "wxname";
    private static final String PROJECT = "project";
    private static final String CARD = "card";
    private static final String IDCARD = "idcard";
    private static final String FACE = "face";
    private static final String MESSAGE = "message";
    private static final String BANKCARD = "bankcard";
    /**
     * 单例模式
     */
    private static User instance = null;

    private User() {

    }

    public static User getInstance() {
        if (instance == null) {
            synchronized (User.class) {
                if (instance == null) {
                    int uid = (int) SPUtils.get(ShoufangbaoApplication.getContext(), UID, -1);
                    if (uid < 0) {
                        instance = new User();
                        instance.userType = TYPE_DEFAULT_USER;
                        instance.style = NORMAL_BROKER;
                        instance.uid = -1;
                        instance.salt = "eb892f";
                        instance.mobile = "";
                        instance.password = "";
                        instance.realName = "";
                        instance.verify = USER_NOT_SUBMIT;
                        instance.credit = 0;
                        instance.interage = 0;
                        instance.totalinterage = 0;
                        instance.level = "";
                        instance.nextlevel = 1000;
                        instance.grade = 0;
                        instance.account = 0;
                        instance.ammount = 0;
                        instance.cityId = 1;
                        instance.city = "";
                        instance.areaId = 0;
                        instance.area = "";
                        instance.districtId = 0;
                        instance.district = "";
                        instance.uniodId = "";
                        instance.wxFace = "";
                        instance.wxName = "";
                        instance.project = "";
                        instance.card = "";
                        instance.idCard = "";
                        instance.face = "";
                        instance.message = 0;
                        instance.bankCard = "";
                        return instance;
                    }
                    instance = new User();
                    Context context = ShoufangbaoApplication.getContext();
                    instance.mobile = (String) SPUtils.get(context, MOBILE, "");
                    instance.userType = (int) SPUtils.get(context, USERTYPE, TYPE_DEFAULT_USER);
                    instance.style = (int) SPUtils.get(context, STYLE, NORMAL_BROKER);
                    if (instance.userType == TYPE_DEFAULT_USER) {
                        if (StringUtils.isEmpty((String) SPUtils.get(context, MOBILE, ""))) {
                            instance.userType = TYPE_TMP_USER;//临时帐号
                        } else {
                            instance.userType = TYPE_NORMAL_USER;//正式账号
                        }
                    }
                    instance.uid = uid;
                    instance.salt = (String) SPUtils.get(context, SALT, "eb892f");
                    instance.password = (String) SPUtils.get(context, PASSWORD, "");
                    instance.realName = (String) SPUtils.get(context, REALNAME, "");
                    instance.verify = (int) SPUtils.get(context, VERIFY, 0);
                    instance.credit = (int) SPUtils.get(context, CREDIT, 0);
                    instance.interage = (int) SPUtils.get(context, INTERAGE, 0);
                    instance.level = (String) SPUtils.get(context, LEVEL, "");
                    instance.nextlevel = (int) SPUtils.get(context, NEXTLEVEL, 1000);
                    instance.grade = (int) SPUtils.get(context, GRADE, 0);
                    instance.account = (int) SPUtils.get(context, ACCOUNT, 0);
                    instance.ammount = (int) SPUtils.get(context, AMMOUNT, 0);
                    instance.cityId = (int) SPUtils.get(context, CITYID, 0);
                    instance.city = (String) SPUtils.get(context, CITY, "");
                    instance.areaId = (int) SPUtils.get(context, AREAID, 0);
                    instance.area = (String) SPUtils.get(context, AREA, "");
                    instance.districtId = (int) SPUtils.get(context, DISTRICTID, 0);
                    instance.district = (String) SPUtils.get(context, DISTRICT, "");
                    instance.uniodId = (String) SPUtils.get(context, UNIONID, "");
                    instance.wxFace = (String) SPUtils.get(context, WXFACE, "");
                    instance.wxName = (String) SPUtils.get(context, WXNAME, "");
                    instance.project = (String) SPUtils.get(context, PROJECT, "");
                    instance.card = (String) SPUtils.get(context, CARD, "");
                    instance.idCard = (String) SPUtils.get(context, IDCARD, "");
                    instance.face = (String) SPUtils.get(context, FACE, "");
                    instance.message = (int) SPUtils.get(context, MESSAGE, 0);
                    instance.bankCard = (String) SPUtils.get(context, BANKCARD, "");
                }
                return instance;
            }
        }
        return instance;
    }

    /**
     * 退出登录时清空数据
     */
    public static void logout() {
        if (instance != null) {
            instance = null;
        }
        SPUtils.put(ShoufangbaoApplication.getContext(), UID, -1);
        SPUtils.put(ShoufangbaoApplication.getContext(), USERTYPE, TYPE_DEFAULT_USER);
    }

    /**
     * 存储user信息
     *
     * @param user
     */
    public static void saveUser(User user, Context context) {
        if (user == null || user.userType == TYPE_DEFAULT_USER) {
            return;
        }
        instance = user;
        SPUtils.put(context, USERTYPE, user.userType);
        SPUtils.put(context, STYLE, user.style);
        SPUtils.put(context, UID, user.uid);
        SPUtils.put(context, SALT, user.salt);
        SPUtils.put(context, MOBILE, user.mobile);
        SPUtils.put(context, PASSWORD, user.password);
        SPUtils.put(context, REALNAME, user.realName);
        SPUtils.put(context, VERIFY, user.verify);
        SPUtils.put(context, CREDIT, user.credit);
        SPUtils.put(context, INTERAGE, user.interage);
        SPUtils.put(context, LEVEL, user.level);
        SPUtils.put(context, NEXTLEVEL, user.nextlevel);
        SPUtils.put(context, GRADE, user.grade);
        SPUtils.put(context, AMMOUNT, user.ammount);
        SPUtils.put(context, ACCOUNT, user.account);
        SPUtils.put(context, CITYID, user.cityId);
        SPUtils.put(context, CITY, user.city);
        SPUtils.put(context, AREAID, user.areaId);
        SPUtils.put(context, AREA, user.area);
        SPUtils.put(context, DISTRICTID, user.districtId);
        SPUtils.put(context, DISTRICT, user.district);
        SPUtils.put(context, UNIONID, user.uniodId);
        SPUtils.put(context, WXFACE, user.wxFace);
        SPUtils.put(context, WXNAME, user.wxName);
        SPUtils.put(context, PROJECT, user.project);
        SPUtils.put(context, CARD, user.card);
        SPUtils.put(context, IDCARD, user.idCard);
        SPUtils.put(context, FACE, user.face);
        SPUtils.put(context, MESSAGE, user.message);
        SPUtils.put(context, BANKCARD, user.bankCard);
    }

    public static User parseUser(Context context, JSONObject str, int userType) {
        User user = new User();
        try {
            JSONObject object = str;
            JSONObject count = object.getJSONObject("count");
            JSONObject credit = object.getJSONObject("credit");
            JSONObject profile = object.getJSONObject("profile");
            user.userType = userType;
            if(object.isNull("mobile")){
                user.mobile = "";
            }else {
                user.mobile = object.getString("mobile");
            }
            if(object.isNull("password")){
                user.password = "";
            }else {
                user.password = object.getString("password");
            }
            if(object.isNull("salt")){
                user.salt = "eb892f";
            }else {
                user.salt = object.getString("salt");
            }
            if(object.isNull("uid")){
                user.uid = -1;
            }else {
                user.uid = object.getInt("uid");
            }
            //credit字典
            if(credit.isNull("credit")){
                user.credit = 0;
            }else {
                user.credit = credit.getInt("credit");
            }

            //count字典
            if(count.isNull("integrate")){
                user.interage = 0;
            }else {
                user.interage = count.getInt("integrate");
            }
            if(count.isNull("totalintegrate")){
                user.totalinterage = 0;
            }else {
                user.totalinterage = count.getInt("totalintegrate");
            }
            if(count.isNull("level")){
                user.level = "";
            }else {
                user.level = count.getString("level");
            }

            if(count.isNull("nextlevel")){
                user.nextlevel = 1000;
            }else {
                user.nextlevel = count.getInt("nextlevel");
            }

            if(count.isNull("grade")){
                user.grade = 0;
            }else {
                user.grade = count.getInt("grade");
            }
            if(count.isNull("amount")){
                user.ammount = 0;
            }else {
                user.ammount = count.getInt("amount");
            }
            if(count.isNull("account")){
                user.account = 0;
            }else {
                user.account = count.getInt("account");
            }
            if(count.isNull("message")){
                user.message = 0;
            }else {
                user.message = count.getInt("message");
            }

            //profie字典
            if(profile.isNull("style")){
                user.style = NORMAL_BROKER_NOT;
            }else {
                user.style = profile.getInt("style");
            }
            if(profile.isNull("realname")){
                user.realName = "";
            }else {
                user.realName = profile.getString("realname");
            }
            if(profile.isNull("cityid")){
                user.cityId = 0;
            }else {
                user.cityId = profile.getInt("cityid");
            }

            if(profile.isNull("city")){
                user.city = "北京";
            }else{
                user.city = profile.getString("city");
            }

            if(profile.isNull("areaid")){
                user.areaId = 0;
            }else {
                user.areaId = profile.getInt("areaid");;
            }

            if(profile.isNull("area")){
                user.area = "";
            }else {
                user.area = profile.getString("area");
            }


            if(profile.isNull("districtid")){
                user.districtId = 0;
            }else {
                user.districtId = profile.getInt("districtid");
            }

            if(profile.isNull("district")){
                user.district = "";
            }else {
                user.district = profile.getString("district");
            }

            if(profile.isNull("UnionID")){
                user.uniodId = "";
            }else {
                user.uniodId = profile.getString("UnionID");
            }

            if(profile.isNull("wxface")){
                user.wxFace = "";
            }else {
                user.wxFace = profile.getString("wxface");
            }

            if(profile.isNull("wxname")){
                user.wxName = "";
            }else {
                user.wxName = profile.getString("wxname");
            }

            if(profile.isNull("project")){
                user.project = "";
            }else {
                user.project = profile.getString("project");
            }

            if(profile.isNull("card")){
                user.card = "";
            }else {
                user.card = profile.getString("card");
            }

            if(profile.isNull("IDcard")){
                user.idCard = "";
            }else {
                user.idCard = profile.getString("IDcard");
            }

            if(profile.isNull("face")){
                user.face = "";
            }else {
                user.face = profile.getString("face");
            }

            if(profile.isNull("bandcard")){
                user.bankCard = "";
            }else {
                user.bankCard = profile.getString("bandcard");
            }

            if(profile.isNull("verify")){
                user.verify = USER_NOT_SUBMIT;
            }else {
                user.verify = profile.getInt("verify");
            }
        } catch (JSONException e) {
            Toast.makeText(context, "数据格式错误", Toast.LENGTH_SHORT).show();
        }
        return user;
    }

    public static void logout(final Context context) {
        if (instance != null) {
            instance = null;
        }
        SPUtils.put(context, UID, -1);
        SPUtils.put(context, USERTYPE, TYPE_DEFAULT_USER);
        User user = User.getInstance();
        HashMap<String, String> map = CommonUtils.getUpHashMap();
        map.put("action", "userAnonymous");
        map.put("module", Constants.MODULE_USER);
        map.put("uid", String.valueOf(user.uid));
        map.put("id", user.salt);
        OkHttpUtils.post().url(Constants.HOST_URL)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code > Result.RESULT_OK) {
                                String jsonStr = jsonObject.getJSONObject("content").toString();
                                JSONObject json = new JSONObject(jsonStr);
                                User user = User.parseUser(context, json, User.TYPE_TMP_USER);
                                User.saveUser(user, context);
                                ContentValues values = new ContentValues();
                                values.put("uid", String.valueOf(User.getInstance().uid));
                                DataSupport.updateAll(Custom.class, values, "uid = ?", "-1");
                            }
                        } catch (JSONException e) {
                            return;
                        }
                    }
                });
    }

}
