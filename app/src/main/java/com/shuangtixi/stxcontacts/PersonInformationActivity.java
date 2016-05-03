package com.shuangtixi.stxcontacts;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.ClipboardManager;
import android.widget.Toast;

import com.baidu.apistore.sdk.ApiCallBack;
import com.baidu.apistore.sdk.ApiStoreSDK;
import com.shuangtixi.stxcontacts.db.Contact;
import com.shuangtixi.stxcontacts.util.FirstLetterUtil;

import java.io.IOException;
import java.io.InputStream;
import com.baidu.apistore.sdk.network.Parameters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PersonInformationActivity extends Activity implements View.OnClickListener {
    private Contact currentContact = new Contact();

    private TextView txtContactName;
    private TextView txtContactTel;
    private TextView txtClass;
    private AssetManager assetManager;//
    private ImageView imgBg;
    private ImageView imgCallPhone;
    private TextView txtTelAddress;
    private Button btnLogout;

    private SharedPreferences sp;//SharePareferences，当前用户信息
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Transparent);
        setContentView(R.layout.activity_person_information);
        currentContact.setName(getIntent().getStringExtra("ContactName"));
        currentContact.setTel(getIntent().getStringExtra("Tel"));
        currentContact.setClassNum(getIntent().getStringExtra("Class"));
        currentContact.setSeason(getIntent().getStringExtra("Season"));
        //Toast.makeText(this,currentContact.getName()+currentContact.getTel()+"class:"+currentContact.getClassNum(),Toast.LENGTH_SHORT).show();
        txtContactName = (TextView) findViewById(R.id.txtContactName);
        txtContactTel = (TextView) findViewById(R.id.txtContactTel);
        txtContactTel.setOnClickListener(this);
        txtClass = (TextView) findViewById(R.id.txtClass);
        imgBg = (ImageView) findViewById(R.id.img_bg);
        imgCallPhone = (ImageView) findViewById(R.id.imgCallPhone);
        imgCallPhone.setOnClickListener(this);
        txtContactName.setText(currentContact.getName());
        txtContactTel.setText(Contact.splitTel(currentContact.getTel()));
        txtTelAddress=(TextView)findViewById(R.id.txtTelAddress);
        btnLogout=(Button)findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(this);

        sp=getSharedPreferences("STXContacts", Context.MODE_PRIVATE);//初始化SharedPreferencese
       if (getIntent().getStringExtra("IsCurrentUser")!=null && getIntent().getStringExtra("IsCurrentUser").equals("True"))
       {
           btnLogout.setVisibility(View.VISIBLE);
       }

        if (currentContact.getClassNum().equals("0")) {
            String sTmp = "开发中心";
            txtClass.setText(sTmp);
        }else if (currentContact.getClassNum().equals("4")){
            String sTmp = "双体系教师组";
            txtClass.setText(sTmp);
        } else {
            String sTmp = "第"+ currentContact.getSeason() + "期项目" + currentContact.getClassNum() + "部";
            txtClass.setText(sTmp);
        }
        assetManager = getAssets();

        try {
            InputStream in = assetManager.open("512px/" + FirstLetterUtil.getFirstLetterFromChinese(currentContact.getName()) + ".jpg");
            Bitmap bit = BitmapFactory.decodeStream(in);
            imgBg.setImageBitmap(bit);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Parameters para = new Parameters();
        para.put("tel",currentContact.getTel());
        ApiStoreSDK.execute(MainActivity.API_STORE_TEL_ADDRESS_URL,ApiStoreSDK.GET,para,new ApiCallBack(){
            @Override
            public void onSuccess(int status, String responseString) {
                try {
                    JSONObject jo = new JSONObject(responseString);

                    if (jo.getString("errNum").equals("0")){
                        //成功获取到归属地信息
                        JSONObject jo2 = jo.getJSONObject("retData");
                        txtTelAddress.setText(jo2.getString("carrier"));

                    }else {
                        txtTelAddress.setText("未知运营商");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onComplete() {

            }

            @Override
            public void onError(int status, String responseString, Exception e) {

               System.out.println("错误:" + e.getMessage());


            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgCallPhone:
                Intent intent = new Intent(Intent.ACTION_CALL);
                Uri data = Uri.parse("tel:" + currentContact.getTel());
                intent.setData(data);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.
                        return;
                    }
                }
                startActivity(intent);
                break;
            case R.id.txtContactTel:
                ClipboardManager cmb = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(currentContact.getTel());
                Toast.makeText(this,"已成功复制号码!",Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnLogout:
                sp=getSharedPreferences("STXContacts", Context.MODE_PRIVATE);
                SharedPreferences.Editor se=sp.edit();
                se.putString(MainActivity.KEY_USER_TEL, "");
                se.commit();
                Toast.makeText(this,"已成功注销登录!",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
