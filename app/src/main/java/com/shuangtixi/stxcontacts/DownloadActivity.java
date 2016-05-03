package com.shuangtixi.stxcontacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.shuangtixi.stxcontacts.db.Contact;
import com.shuangtixi.stxcontacts.util.BmobUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.listener.FindCallback;

public class DownloadActivity extends Activity {
    private BmobUtils bu;
    private List<Contact> contacts = new ArrayList<Contact>(); //联系人集合
    private Contact contact; //单个联系人
    private DbManager db; //xUntils数据库模块
    private long lngTime;
    private long lngTime2;

    DbManager.DaoConfig daoConfig ;
    private FindCallback findCallback = new FindCallback() {

        @Override
        public void onSuccess(JSONArray jsonArray) {

            if (jsonArray.length() == 0) {
                return;
            }

            contacts = new ArrayList<Contact>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jo = (JSONObject) jsonArray.opt(i);
                try {
                    contact = new Contact();
                    contact.setTel(jo.getString("tel"));
                    contact.setName(jo.getString("name"));
//                    contact.setInfo(jo.getString("info"));
                    contact.setSeason(jo.getString("season"));
                    contact.setClassNum(jo.getString("class"));
                    contacts.add(contact);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //
            Contact.deleteAllContacts(db);
            //将从服务器获取到的联系人数据存至SQLite数据库
            Contact.insertContacts(db, contacts);
            Intent i =new Intent();
            i.putExtra("Result", "success");
            setResult(MainActivity.UPDATE_CONTACTS, i);
            lngTime2=System.currentTimeMillis();
            if (lngTime2-lngTime<=3000){
                TimerTask task = new TimerTask() {
                    public void run() {
                        DownloadActivity.this.finish();
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 3000);
            }else {
                DownloadActivity.this.finish();
            }


        }

        @Override
        public void onFailure(int i, String s) {

            Intent ii =new Intent();
            ii.putExtra("Result", "failed");
            if (getIntent().getStringExtra("UpdateWithNoData").equals("True")){
                setResult(MainActivity.UPDATE_CONTACTS_WITH_NO_DATA, ii);
            }else {
                setResult(MainActivity.UPDATE_CONTACTS, ii);
            }

            lngTime2=System.currentTimeMillis();
            if (lngTime2-lngTime<=3000){
                TimerTask task = new TimerTask() {
                    public void run() {
                        DownloadActivity.this.finish();
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 3000);
            }else {
                DownloadActivity.this.finish();
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        daoConfig = new MainActivity().getDaoConfig();
        //获取Bmob通讯录数据到JSONArray并存储
        bu = new BmobUtils(this, MainActivity.BMOB_APP_ID, "contact");
        db = x.getDb(daoConfig);
        bu.queryData(findCallback);
        lngTime=System.currentTimeMillis();
    }

    @Override
    public void onBackPressed() {

    }
}
