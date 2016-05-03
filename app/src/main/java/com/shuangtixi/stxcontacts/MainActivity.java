package com.shuangtixi.stxcontacts;

import android.Manifest;
import android.app.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.AutoCompleteTextView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.apistore.sdk.ApiStoreSDK;
import com.shuangtixi.stxcontacts.adapter.ContactAdapter;
import com.shuangtixi.stxcontacts.util.BmobUtils;
import com.shuangtixi.stxcontacts.db.Contact;
import com.shuangtixi.stxcontacts.util.FirstLetterUtil;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.listener.FindCallback;

public class MainActivity extends Activity implements ExpandableListView.OnChildClickListener, View.OnClickListener, View.OnTouchListener, AdapterView.OnItemClickListener {
    private BmobUtils bu; //Bmob工具类
    private Integer integer;//短信ID
    private List<Contact> contacts = new ArrayList<Contact>(); //联系人集合
    private Contact contact; //单个联系人
    private DbManager db; //xUntils数据库模块
    public static final String BMOB_APP_ID = "0256709a7854d209229528eb7f343ad9";
    public static final String API_STORE_KEY="65f6ef21e74d530593b8230aa2554d20";
    public static final String API_STORE_TEL_ADDRESS_URL="http://apis.baidu.com/apistore/mobilephoneservice/mobilephone";
    private AutoCompleteTextView actv_search_contact;//搜索框
    private ArrayAdapter<String> searchAdapter; //搜索框内容Adapter
    private ExpandableListView lstContacts;
    private ContactAdapter contactAdapter; //联系人Adapter
    private Map<String, List<Contact>> g_Contact_Relation;
    private SlideMenu slideMenu; //滑动侧边栏
    private ImageView imgSlideMenu;//图标
    private ImageView imgPersonalinformation;
    private LinearLayout menu_item1;//侧边栏按钮
    private LinearLayout menu_item2;
    private LinearLayout menu_item3;
    private Boolean isUpdate=false; //是否是更新数据
    public static final int LOADING_STATE=2;
    public static final String LOADING_SUCCESS="success";
    public static final int UPDATE_CONTACTS=1;
    public static final int UPDATE_CONTACTS_WITH_NO_DATA=3;
    public static final String KEY_USER_TEL="UserName";
    public static final int USER_LOGIN_STATE=4;

    private String storgeDir;//存储目录
    private final int REQUEST_CODE_ASK_CALL_PHONE=1;
    private Contact currentUser; ////当前用户----------------------------------
    private SharedPreferences sp;//SharePareferences，存储当前用户信息
    private long lngTime=0;
    private boolean isLatestData=false;
    //外部存储目录
    private File externalStorge=new File(Environment.getExternalStorageDirectory().getPath()+"/STXContacts");


    private DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
            .setDbName("STXContacts.db")
                    // 不设置dbDir时, 默认存储在app的私有目录.
          //.setDbDir(new File("sdcard/STXContacts/")) // "sdcard"的写法并非最佳实践, 这里为了简单, 先这样写了.
            .setDbDir(externalStorge)
            .setDbVersion(1)
            .setDbOpenListener(new DbManager.DbOpenListener() {
                @Override
                public void onDbOpened(DbManager db) {
                    // 开启WAL, 对写入加速提升巨大
                    db.getDatabase().enableWriteAheadLogging();
                }
            })
            .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                @Override
                public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
                    // TODO: ...
                    // db.addColumn(...);
                    // db.dropTable(...);
                    // ...
                    // or
                    // db.dropDb();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //test
//        sp=getSharedPreferences("STXContacts", Context.MODE_PRIVATE);
//        SharedPreferences.Editor se=sp.edit();
//        se.putString(MainActivity.KEY_USER_TEL, "");
//        se.commit();

        //申请权限
        requestPremisson();

        //弹出欢迎页
        Intent i=new Intent(this,LoadingActivity.class);
        startActivityForResult(i, 0);

        //Toast.makeText(this,"test",Toast.LENGTH_SHORT).show();
    }

    private void  requestPremisson(){


        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.INTERNET,Manifest.permission.CALL_PHONE,Manifest.permission.ACCESS_NETWORK_STATE
                }, REQUEST_CODE_ASK_CALL_PHONE);
                return;
            }else{

            }
        } else {

        }
    }

    private void initViews() {
        actv_search_contact = (AutoCompleteTextView) findViewById(R.id.actv_search_contact);
        actv_search_contact.setAdapter(searchAdapter);
        actv_search_contact.setOnItemClickListener(this);
        slideMenu = (SlideMenu) findViewById(R.id.slideMenu);
        imgSlideMenu = (ImageView) findViewById(R.id.imgSlideMenu);
        imgSlideMenu.setOnClickListener(this);
        imgPersonalinformation=(ImageView)findViewById(R.id.imgPersonalInformation);
        imgPersonalinformation.setOnClickListener(this);
        menu_item1 = (LinearLayout) findViewById(R.id.menuItemHome);
        menu_item1.setOnClickListener(this);
        menu_item1.setOnTouchListener(this);
        menu_item2 = (LinearLayout) findViewById(R.id.menuItemRefreshData);
        menu_item2.setOnClickListener(this);
        menu_item2.setOnTouchListener(this);
        menu_item3 = (LinearLayout) findViewById(R.id.menuItemAboutUs);
        menu_item3.setOnClickListener(this);
        menu_item3.setOnTouchListener(this);
        lstContacts = (ExpandableListView) findViewById(R.id.lstContacts);
        lstContacts.setAdapter(contactAdapter);
        lstContacts.setGroupIndicator(null);
        lstContacts.setDivider(null);
        for (int i = 0; i < contactAdapter.getGroupCount(); i++) {
            lstContacts.expandGroup(i);
        }
        lstContacts.setOnChildClickListener(this);

    }

    /**
     * 初始化数据
     */
    private void initData() {

        //初始化数据库以及Bmob工具类
        bu = new BmobUtils(this, BMOB_APP_ID, "contact");
        db = x.getDb(daoConfig);

        try {
            //从本地数据库获取联系人数据

            contacts = db.selector(Contact.class).limit(500).findAll();
        } catch (DbException e) {
            e.printStackTrace();
        }
        //检查是否已登录
        sp=getSharedPreferences("STXContacts", Context.MODE_PRIVATE);
        String sUserTel = sp.getString(KEY_USER_TEL, "");

        //本地无数据或者未登录时

        if ((contacts == null || sUserTel.equals("")) && isLatestData==false) {

            //弹出更新数据aty
            Intent i=new Intent(this,DownloadActivity.class);
            i.putExtra("UpdateWithNoData", "True");
            startActivityForResult(i, 0);

        } else {
            //初始化数据
            searchAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
            for (Contact contact : contacts) {
                if (contact.getName().equals(FirstLetterUtil.getFirstLetter(contact.getName())))//判断名字是否是中文，
                {
                    searchAdapter.add(contact.getName() + " " + contact.getTel());
                }else {
                    searchAdapter.add(contact.getName()+" "+contact.getTel()+" "+FirstLetterUtil.getFirstLetter(contact.getName()));
                }

            }
            contactAdapter = new ContactAdapter(this, contacts, R.layout.item_group_list, R.layout.item_contact_list);
            g_Contact_Relation = new HashMap<>();
            g_Contact_Relation = contactAdapter.getG_Contact_Relation();


            if (checkLogin()){
                //已登录
                initViews();
            }else {
                //未登录
                Intent i=new Intent(this,LoginActivity.class);
                List<String> tels=new ArrayList<>();
                for (Contact c:contacts){
                    tels.add(c.getTel());
                }
                i.putStringArrayListExtra("Tels", (ArrayList<String>) tels);
                startActivityForResult(i, 0);
            }

        }
    }

    private Boolean checkLogin(){
        sp=getSharedPreferences("STXContacts", Context.MODE_PRIVATE);
        String sUserTel = sp.getString(KEY_USER_TEL, "");
        if (sUserTel.equals("")){
            return false;
        }else {
            for (Contact c:contacts)
            {
                if (c.getTel().equals(sUserTel)){
                    currentUser = c;
                    return true;
                }
            }
        }
        return false;
    }
    @Override //联系人被点击
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        if (!slideMenu.isMainScreenShowing()) {
            slideMenu.closeMenu();
            return true;
        }

        String s = g_Contact_Relation.get(contactAdapter.getGroups().get(groupPosition)).get(childPosition).getName();
        String s2 = g_Contact_Relation.get(contactAdapter.getGroups().get(groupPosition)).get(childPosition).getTel();
        String s3 = g_Contact_Relation.get(contactAdapter.getGroups().get(groupPosition)).get(childPosition).getClassNum();
        String s4 = g_Contact_Relation.get(contactAdapter.getGroups().get(groupPosition)).get(childPosition).getSeason();
        showContactInformation(s,s2,s3,s4);

        return true;
    }

    private void showContactInformation(String ContactName,String Tel,String Class,String season){
        Intent i = new Intent(this, PersonInformationActivity.class);
        i.putExtra("ContactName", ContactName);
        i.putExtra("Tel", Tel);
        i.putExtra("Class", Class);
        i.putExtra("Season",season);
        startActivity(i);
    }

    private void checkUpdate(){
        BmobUtils bu2 = new BmobUtils(this, BMOB_APP_ID, "Version");
        bu2.queryData(new FindCallback() {
            @Override
            public void onSuccess(JSONArray jsonArray) {
                if (jsonArray.length() == 0) {
                    return;
                }
                int maxVersionCode = 0;
                String target_Url = "";
                String maxVersionName="";
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = (JSONObject) jsonArray.opt(i);
                    try {
                        if (jo.getInt("version_code") > maxVersionCode) {
                            maxVersionCode = jo.getInt("version_code");
                            target_Url = jo.getString("target_url");
                            maxVersionName = jo.getString("version_name");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    PackageManager pm = MainActivity.this.getPackageManager();
                    PackageInfo pi = pm.getPackageInfo(MainActivity.this.getPackageName(), 0);
                    if (pi.versionCode < maxVersionCode) {
                        alertUpdateDialog(target_Url,maxVersionName);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(int i, String s) {

            }
        });
    }

    private void alertUpdateDialog(final String target_Url,String version_name){

        new AlertDialog.Builder(this).setMessage("检测到新版本"+version_name+",是否现在更新?").setTitle("更新提示")
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse(target_Url);
                        intent.setData(content_url);
                        startActivity(intent);
                    }
                }).setNegativeButton("取消",null).show();



    }

    @Override//点击事件
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.imgSlideMenu:             //弹出侧边栏
                if (slideMenu.isMainScreenShowing()) {
                    slideMenu.openMenu();

                } else {
                    slideMenu.closeMenu();

                }
                break;
            case R.id.menuItemHome:
                slideMenu.closeMenu();
                break;
            case R.id.menuItemRefreshData:
                isUpdate=true;
                i = new Intent(this, DownloadActivity.class);
                i.putExtra("UpdateWithNoData", "False");
                startActivityForResult(i, 1);
                break;
            case R.id.menuItemAboutUs: //关于信息
                i = new Intent(this, AboutUsActivity.class);
                startActivity(i);
                break;
            case R.id.imgPersonalInformation:
                Intent ii = new Intent(this, PersonInformationActivity.class);
                ii.putExtra("ContactName", currentUser.getName());
                ii.putExtra("Tel", currentUser.getTel());
                ii.putExtra("Class", currentUser.getClassNum());
                ii.putExtra("IsCurrentUser","True");
                startActivity(ii);
                break;
        }
    }

    @Override //用于侧边栏点击效果
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.setBackgroundResource(R.color.menu_item_pressed);

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            v.setBackgroundResource(R.color.menu_background_color);
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode){
            case UPDATE_CONTACTS://更新数据页结束，获取返回结果
                if (data.getStringExtra("Result").equals("failed"))
                {
                    Toast.makeText(this,"获取联系人数据失败!",Toast.LENGTH_SHORT).show();

                }else {
                    Toast.makeText(this, "获取联系人数据成功!", Toast.LENGTH_SHORT).show();
                    isLatestData=true;
                    initData();
                }
                break;
            case LOADING_STATE:
                //欢迎页结束，初始化数据
                if (data.getStringExtra("LoadingState").equals(LOADING_SUCCESS)){//
                    initData();//初始化数据
                    checkUpdate();
                }
                break;
            case UPDATE_CONTACTS_WITH_NO_DATA://首次登录获取数据结束，返回结果
                if (data.getStringExtra("Result").equals("failed"))
                {
                    new AlertDialog.Builder(this).setMessage("首次登录需从服务器获取联系人数据!数据获取失败,请检查网络连接!").setTitle("错误")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.this.finish();
                                }
                            }).show();
                }else {
                    Toast.makeText(this, "获取联系人数据成功!", Toast.LENGTH_SHORT).show();
                    isLatestData = true;//当前数据是最新
                    initData();
                }

                break;
            case USER_LOGIN_STATE:
                switch (data.getStringExtra("Login_State")){
                    case "Force_Close":
                        finish();
                        break;
                    case "Verify_Success":
                        initViews();
                        break;
                }

            break;
        }
    }
    //搜索框检索事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String sTmp = searchAdapter.getItem(position);

        for (Contact c:contacts){
            String sTmp2 = c.getName()+" "+c.getTel();

            if (sTmp.contains(sTmp2)){
                actv_search_contact.setText("");
                showContactInformation(c.getName(), c.getTel(), c.getClassNum(),c.getSeason());
                return;
            }
        }
    }
    public DbManager.DaoConfig getDaoConfig() {
        return daoConfig;
    }

    //权限获取结果处理
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_CALL_PHONE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted

                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "CALL_PHONE Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    @Override
    public void onBackPressed() {
        if (lngTime!=0){
            long lngTime2 = System.currentTimeMillis();
            if (lngTime2-lngTime<=3000){
                super.onBackPressed();
                return;
            }else {
                lngTime = System.currentTimeMillis();
                Toast.makeText(this,"再按一次返回键退出程序!",Toast.LENGTH_SHORT).show();
            }
        } else {
            lngTime = System.currentTimeMillis();
            Toast.makeText(this,"再按一次返回键退出程序!",Toast.LENGTH_SHORT).show();
        }
    }
}







 /*
        findViewById(R.id.btnGetData).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bu.queryData(new FindCallback() {
                    @Override
                    public void onSuccess(JSONArray jsonArray) {
                        Toast.makeText(MainActivity.this, "Length:" + jsonArray.length(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        Toast.makeText(MainActivity.this, "Get Failed!" + i + ":" + s, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        findViewById(R.id.btnVerifyCode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bu.verifySMSCode("13101375734", et.getText().toString(), new VerifySMSCodeListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null) {
                            Toast.makeText(MainActivity.this, "验证成功!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "验证失败!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        findViewById(R.id.btnSendVerifyCode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bu.sendVerifySMS("13101375734", new RequestSMSCodeListener() {
                    @Override
                    public void done(Integer integer, BmobException e) {
                        if (e==null){
                            //MainActivity.this.integer = integer;
                            Toast.makeText(MainActivity.this,"短信发送成功.ID:"+integer,Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(MainActivity.this,"短信发送成功失败!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        */