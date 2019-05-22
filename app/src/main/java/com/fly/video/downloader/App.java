package com.fly.video.downloader;

import android.app.Application;
import android.content.Context;

import com.fly.iconify.Iconify;
import com.fly.iconify.fontawesome.module.FontAwesomeLightModule;
import com.fly.iconify.fontawesome.module.FontAwesomeModule;
import com.payelves.sdk.EPay;

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

    }

    public static Application getApp()
    {
        return app;
    }

    public static Context getAppContext() {
        return getApp().getApplicationContext();
    }

}
