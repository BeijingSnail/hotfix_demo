package com.example.snail.hotfix_demo;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Keep;
import android.util.Log;

import com.taobao.sophix.PatchStatus;
import com.taobao.sophix.SophixApplication;
import com.taobao.sophix.SophixEntry;
import com.taobao.sophix.SophixManager;
import com.taobao.sophix.listener.PatchLoadStatusListener;

/**
 * Sophix入口类，专门用于初始化Sophix，不应包含任何业务逻辑。
 * 此类必须继承自SophixApplication，onCreate方法不需要实现。
 * AndroidManifest中设置application为此类，而SophixEntry中设为原先Application类。
 * 注意原先Application里不需要再重复初始化Sophix，并且需要避免混淆原先Application类。
 * 如有其它自定义改造，请咨询官方后妥善处理。
 */
public class SophixStubApplication extends SophixApplication {
    private final String TAG = "SophixStubApplication";

    // 此处SophixEntry应指定真正的Application，也就是你的应用中原有的主Application，并且保证RealApplicationStub类名不被混淆。
    @Keep
    @SophixEntry(Application.class)
    static class RealApplicationStub {
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //         如果需要使用MultiDex，需要在此处调用。
        //         MultiDex.install(this);
        initSophix();
    }

    private void initSophix() {
        String appVersion = "0.0.0";
        try {
            appVersion = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), 0)
                    .versionName;
        } catch (Exception e) {
        }
        final SophixManager instance = SophixManager.getInstance();
        instance.setContext(this)
                .setAppVersion(appVersion)
                .setSecretMetaData(null, null, null) //三个参数分别对应AndroidManifest里面的//AppId、AppSecret、RSA密钥，可以不在AndroidManifest设置而是用此函数来设置//Secret。放到代码里面进行设置可以自定义混淆代码，更加安全，此函数的设置会覆盖//AndroidManifest里面的设置，如果对应的值设为null，默认会在使用AndroidManifest里面//的。
                .setEnableDebug(true)//默认为false，设为true即调试模式下会输出日志以及不进行  //补丁签名校验. 线下调试此参数可以设置为true, 它会强制不对补丁进行签名校验, 所有就算//补丁未签名或者签名失败也发现可以加载成功. 但是正式发布该参数必须为false, false会//对补丁做签名校验, 否则就可能存在安全漏洞风险。
                .setEnableFullLog()
                .setPatchLoadStatusStub(new PatchLoadStatusListener() {
                    @Override
                    public void onLoad(final int mode, final int code, final String info, final int handlePatchVersion) {
                        if (code == PatchStatus.CODE_LOAD_SUCCESS) {
                            Log.i(TAG, "sophix load patch success!");
                        } else if (code == PatchStatus.CODE_LOAD_RELAUNCH) {
                            // 如果需要在后台重启，建议此处用SharePreference保存状态。
                            Log.i(TAG, "sophix preload patch success. restart app to make effect.");
                            /** 不可以直接Process.killProcess(Process.myPid())来杀进程，这样会扰乱Sophix的内部状态。
                             * 因此如果需要杀死进程，建议使用这个方法，它在内部做一些适当处理后才杀死本进程。*/
                            instance.killProcessSafely();
                        }
                    }
                }).initialize();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // queryAndLoadNewPatch不可放在attachBaseContext 中，否则无网络权限，建议放在      //后面任意时刻，如onCreate中
        SophixManager.getInstance().queryAndLoadNewPatch();
        /** 补丁在后台发布之后, 并不会主动下行推送到客户端, 客户端通过调用queryAndLoadNewPatch方法查询后台补丁是否可用*/
    }
}