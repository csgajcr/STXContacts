package com.shuangtixi.stxcontacts.util;

import android.content.Context;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.listener.FindCallback;
import cn.bmob.v3.listener.FindStatisticsListener;
import cn.bmob.v3.listener.RequestSMSCodeListener;
import cn.bmob.v3.listener.VerifySMSCodeListener;


/**
 * Created by Jcr-PC on 2016/3/8.
 */
public class BmobUtils extends BmobObject {


    private String tableName;
    private Context context;
    private String appID;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public BmobUtils(Context context){
        this.context = context;

    }

    public BmobUtils(Context context,String appID,String tableName){
        this.tableName = tableName;
        this.context = context;
        this.appID = appID;
        Bmob.initialize(context,appID);
        setTableName(tableName);
    }

    public void sendVerifySMS(String tel,RequestSMSCodeListener requestSMSCodeListener){
        BmobSMS.requestSMSCode(context, tel, "双体通讯录", requestSMSCodeListener);

    }

    public void verifySMSCode(String tel,String smsCode,VerifySMSCodeListener verifySMSCodeListener){
        BmobSMS.verifySmsCode(context, tel, smsCode, verifySMSCodeListener);
    }



    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public void setTableName(String tableName) {
        this.tableName = tableName;
        super.setTableName(tableName);
    }

    //查询所有数据
    public void queryData(FindCallback findCallback){
        BmobQuery query = new BmobQuery(tableName);
        query.setLimit(1000);
        query.findObjects(context, findCallback);
    }





}
