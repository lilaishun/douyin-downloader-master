package com.fly.video.downloader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fly.video.downloader.core.app.BaseActivity;
import com.fly.video.downloader.layout.fragment.HistoryFragment;
import com.fly.video.downloader.layout.fragment.VideoFragment;
import com.fly.video.downloader.layout.fragment.VideoSearchFragment;
import com.fly.video.downloader.util.content.Recv;
import com.fly.video.downloader.util.model.Video;
import com.github.florent37.runtimepermission.RuntimePermission;
import com.github.florent37.runtimepermission.callbacks.PermissionListener;
import com.payelves.sdk.EPay;
import com.payelves.sdk.enums.EPayResult;
import com.payelves.sdk.listener.PayResultListener;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.github.florent37.runtimepermission.RuntimePermission.askPermission;

public class MainActivity extends BaseActivity {
    @BindView(R.id.navigation)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.progress)
    ProgressBar progressBar;


    private Unbinder unbinder;
    protected VideoFragment videoFragment;
    protected HistoryFragment historyFragment;
    protected VideoSearchFragment searchFragment = null;

    private Date backPressAt = null;
    private boolean fromSend = false;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            int id = item.getItemId();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            if (id == R.id.navigation_history) showFragment(historyFragment); else showFragment(videoFragment);
            ft.commit();

            return true;
        }
    };

    private FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        // 设置Toolbar
        setSupportActionBar(toolbar);
        //底部状态栏
 /*       bottomNavigationView.setBackground(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
        bottomNavigationView.setItemBackgroundResource(R.drawable.transparent);*/
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);


        videoFragment = VideoFragment.newInstance();
        historyFragment = HistoryFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.full_pager, videoFragment).add(R.id.view_pager, historyFragment).hide(historyFragment).show(videoFragment).commit();

        fromSend = this.getIntent() != null && Intent.ACTION_SEND.equals(this.getIntent().getAction());

        askPermission(this).ask(new PermissionListener() {
            @Override
            public void onAccepted(RuntimePermission runtimePermission, List<String> accepted) {
                Toast.makeText(MainActivity.this,"OK", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDenied(RuntimePermission runtimePermission, List<String> denied, List<String> foreverDenied) {
                Toast.makeText(MainActivity.this,"Why?", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.pay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pay();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        // 最后一次 并且大于2秒
        if (fm.getBackStackEntryCount() == 0 && !fromSend) {
            if (backPressAt == null || new Date().getTime() - backPressAt.getTime() > 2000) {
                Toast.makeText(this, R.string.one_more_exit, Toast.LENGTH_SHORT).show();
                backPressAt = new Date();
                return;
            } else {
                com.fly.video.downloader.core.app.Process.background(this);
                //super.onBackPressed();
                return;
            }
        }

        if (fm.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry back = fm.getBackStackEntryAt(fm.getBackStackEntryCount() -1);
            switch (back.getName())
            {
                case "video":
                    showFragment(videoFragment);
                    break;
            }

            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        fromSend = false;

        showFragment(videoFragment);
        Recv recv = new Recv(intent);
        if (recv.isActionSend() && videoFragment.isAdded()) {
            fromSend = true;
            videoFragment.Analyze(recv.getContent());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        //finish();
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public void setMainProgress(int progress)
    {
        if (progress >= 100) progress = 0;
        progressBar.setProgress(progress);
    }

    public void showVideoSearchFragment()
    {
        if (null == searchFragment)
            searchFragment = VideoSearchFragment.newInstance();

        if (!searchFragment.isAdded())
            getSupportFragmentManager().beginTransaction().add(R.id.no_navigation_pager, searchFragment).commit();

        showFragment(searchFragment);
        getSupportFragmentManager().beginTransaction().addToBackStack("video").commit();
    }

    public void onVideoChange(String str)
    {
        //Toast.makeText(this, str, Toast.LENGTH_LONG).show();
        showFragment(videoFragment);
        videoFragment.Analyze(str);
    }

    public void onVideoChange(Video video)
    {
        onVideoChange(video, false);
    }

    public void onVideoChange(Video video, boolean fromHistory)
    {
        showFragment(videoFragment);
        videoFragment.Analyze(video, fromHistory);
    }

    public void onHistoryAppend(Video video)
    {
        historyFragment.perpendHistory(video);
    }





    private void  pay(){
        /**
         * 发起支付
         *
         * @param subject       商品名称,不可为空和空字符串
         * @param body          商品内容,不可为空和空字符串
         * @param amount        支付金额，单位分，不能为null和<1
         * @param orderId       商户系统的订单号(如果有订单的概念),没有可为空
         * @param payUserId     商户系统的用户id(如果有用户的概念),没有可为空
         * @param backPara      支付成功后支付精灵会用此参数回调配置的url
         *					(回调url在后台应用->添加应用时候配置)
         * demo: backPara 的value(建议json) ： {"a":1,"b":"2"},如不需要可为空。
         * @param payResultListener，不能为null 支付结果回调
         */
        EPay.getInstance(this).pay("视频下载", "视频下载量", 1,
                "", "","", new PayResultListener() {
                    /**
                     * @param context
                     * @param payId   支付精灵支付id
                     * @param orderId   商户系统订单id
                     * @param payUserId 商户系统用户ID
                     * @param payResult
                     * @param payType   支付类型:1 支付宝，2 微信 3 银联
                     * @param amount    支付金额
                     * @see EPayResult#FAIL_CODE
                     * @see EPayResult#SUCCESS_CODE
                     * 1支付成功，2支付失败
                     */
                    @Override
                    public void onFinish(Context context, Long payId, String orderId, String payUserId,
                                         EPayResult payResult , int payType, Integer amount) {
                        EPay.getInstance(context).closePayView();//关闭快捷支付页面
                        if(payResult.getCode() == EPayResult.SUCCESS_CODE.getCode()){
                            //支付成功逻辑处理
                            Toast.makeText(MainActivity.this, payResult.getMsg(), Toast.LENGTH_LONG).show();
                        }else if(payResult.getCode() == EPayResult.FAIL_CODE.getCode()){
                            //支付失败逻辑处理
                            Toast.makeText(MainActivity.this, payResult.getMsg(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
