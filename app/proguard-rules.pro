# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/long1/Documents/sdk/android-sdk-macosx/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

-keep class com.iflytek.**{*;}

-keep class android.net.http.SslError
-keep class android.webkit.**{*;}
-keep class cn.sharesdk.**{*;}
-keep class com.sina.**{*;}
-keep class m.framework.**{*;}

-keep class com.baidu.** { *; }
-keep class vi.com.gdi.bgl.android.**{*;}


-dontwarn cn.sharesdk.sina.weibo.**
-keep class cn.sharesdk.sina.weibo.** { *;}

-dontwarn cn.sharesdk.wechat.moments.**
-keep class cn.sharesdk.wechat.moments.** { *;}

-dontwarn cn.sharesdk.wechat.friends.**
-keep class cn.sharesdk.wechat.friends.** { *;}

-dontwarn cn.sharesdk.wechat.favorite.**
-keep class cn.sharesdk.wechat.favorite.** { *;}

-dontwarn cn.sharesdk.system.text.**
-keep class cn.sharesdk.system.text.** { *;}

-dontwarn cn.sharesdk.tencent.qq.**
-keep class cn.sharesdk.tencent.qq.** { *;}

-keep class cn.sharesdk.framework.**{*;}


-keepclassmembers class ** {
    @com.kupangstudio.shoufangbao.otto.Subscribe public *;
    @com.kupangstudio.shoufangbao.otto.Produce public *;
}
-keep class de.greenrobot.dao.**{*;}
-keep class com.kupangstudio.shoufangbao.greendao.**{*;}
-keep class com.squareup.**{*;}
-dontwarn com.squareup.**
-dontwarn net.soureceforge.pinyin4j.**
-dontwarn demo.**
-keep class net.sourceforge.pinyin4j.** { *;}
-keep class demo.** { *;}
-dontwarn com.tencent.smtt.sdk.**

-keepclassmembers class * extends android.app.Activity {
   public void setTask();
}
-keepattributes *Annotation*
-keepattributes *JavascriptInterface*
-keep class com.github.mikephil.charting.** {* ;}
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *;}
-dontwarn okio.**
-dontwarn com.handmark.pulltorefresh.library.**
-keep class com.handmark.pulltorefresh.library.** { *;}
-dontwarn com.handmark.pulltorefresh.library.extras.**
-keep class com.handmark.pulltorefresh.library.extras.** { *;}
-dontwarn com.handmark.pulltorefresh.library.internal.**
-keep class com.handmark.pulltorefresh.library.internal.** { *;}
-dontwarn org.litepal.*
-keep class org.litepal.** { *; }
-keep enum org.litepal.**
-keep interface org.litepal.** { *; }
-keep public class * extends org.litepal.**
-keepattributes *Annotation*
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * extends org.litepal.crud.DataSupport{*;}
##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }
-dontwarn com.google.gson.**
# Application classes that will be serialized/deserialized over Gson
##---------------End: proguard configuration for Gson  ----------
-keep public class com.kupangstudio.shoufangbao.MyInviteActivity$JS {*;}
-keep public class com.kupangstudio.shoufangbao.CommunityDetailActivity$JS {*;}
-keep public class com.kupangstudio.shoufangbao.ShareBannerActivity$JS {*;}
-keep class com.kupangstudio.shoufangbao.network.Result{*;}
-keep public class * implements java.io.Serializable {*;}
-keep class com.kupangstudio.shoufangbao.model.** { *; }
-keep class com.kupangstudio.shoufangbao.updateservice.Version{*;}
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
-keepclassmembers class ** {
    public void onEvent*(**);
    void onEvent*(**);
}
-keep public class [com.kupangstudio.shoufangbao].R$*{
public static final int *;
}
-dontwarn android.content.Context
#okhttputils
-dontwarn com.zhy.http.**
-keep class com.zhy.http.**{*;}
-keep interface com.zhy.http.**{*;}

#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}
-keep interface okhttp3.**{*;}

#okio
-dontwarn okio.**
-keep class okio.**{*;}
-keep interface okio.**{*;}
