package com.fly.video.downloader;

import android.app.Application;
import android.content.Context;

import com.fly.iconify.Iconify;
import com.fly.iconify.fontawesome.module.FontAwesomeLightModule;
import com.fly.iconify.fontawesome.module.FontAwesomeModule;
import com.payelves.sdk.EPay;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;

public class App extends Application {

    private static Application app;

    @Override
    public void onCreate() {
        super.onCreate();
        Iconify.with(new FontAwesomeLightModule())
                .with(new FontAwesomeModule());

        app = this;
        /**
         * 支付服务初始化
         * @param openId
         *      用户id(不能为null和空字符串,区分大小写,数据来源:后台->设置->API接口信息->OPEN_ID)
         * @param token
         *      秘钥(不能为null和空字符串,区分大小写,数据来源:后台->设置->API接口信息->TOKEN)
         * @param appKey
         *      appKey(不能为null和空字符串,数据来源:后台->应用->该应用appKey)
         * @param channel
         *      channel(不能为null和空字符串)"baidu","xiaomi" ,"360"
         * @return
         */
        EPay.getInstance(getApplicationContext()).init("C8KjKKIA9","0499a1d129ef48958871426985154aab",
               " \t8328148005879813", "baidu");
        // 必须在调用任何统计SDK接口之前调用初始化函数
        UMConfigure.init(this, "5ce4ff8d570df3b97e000204", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, null);
        UMConfigure.setLogEnabled(true);
        UMShareAPI.get(this);//初始化sdk
        //开启debug模式，方便定位错误，具体错误检查方式可以查看http://dev.umeng.com/social/android/quick-integration的报错必看，正式发布，请关闭该模式
        Config.DEBUG = true;
    }

    public static Application getApp()
    {
        return app;
    }

    public static Context getAppContext() {
        return getApp().getApplicationContext();
    }
    //各个平台的配置
    {
        //微信
        PlatformConfig.setWeixin("wxdc1e388c3822c80b", "3baf1193c85774b3fd9d18447d76cab0");
        //新浪微博(第三个参数为回调地址)
        PlatformConfig.setSinaWeibo("3921700954", "04b48b094faeb16683c32669824ebdad","http://sns.whalecloud.com/sina2/callback");
        //QQ
        PlatformConfig.setQQZone("100424468", "c7394704798a158208a74ab60104f0ba");
    }
}
