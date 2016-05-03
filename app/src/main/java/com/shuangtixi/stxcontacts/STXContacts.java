package com.shuangtixi.stxcontacts;

import android.app.Application;

import com.baidu.apistore.sdk.ApiStoreSDK;

/**
 * Created by Jcr-PC on 2016/3/15.
 */
public class STXContacts extends Application {
    @Override
    public void onCreate() {

        ApiStoreSDK.init(this, MainActivity.API_STORE_KEY);
        super.onCreate();
    }
}
