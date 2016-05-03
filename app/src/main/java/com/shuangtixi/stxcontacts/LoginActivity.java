package com.shuangtixi.stxcontacts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.shuangtixi.stxcontacts.util.BmobUtils;
import com.shuangtixi.stxcontacts.util.DisplayUtil;

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.RequestSMSCodeListener;
import cn.bmob.v3.listener.VerifySMSCodeListener;

public class LoginActivity extends Activity implements View.OnClickListener {
    private EditText txtUserTel;
    private EditText txtVerifyCode;
    private Button btnLogin;
    private Button btnGetVerifyCode;
    private BmobUtils bu;
    private List<String> contactTels;
    private String currentTel;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        bu = new BmobUtils(this,MainActivity.BMOB_APP_ID,"contact");
        sp=getSharedPreferences("STXContacts", Context.MODE_PRIVATE);

        txtUserTel=(EditText)findViewById(R.id.txtUserTel);
        txtUserTel.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        txtVerifyCode=(EditText)findViewById(R.id.txtVerifyCode);
        txtVerifyCode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        btnLogin=(Button)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
        btnGetVerifyCode=(Button)findViewById(R.id.btnGetVerifyCode);
        btnGetVerifyCode.setOnClickListener(this);

        Drawable drawable= getResources().getDrawable(R.drawable.pic_login_phone);
        drawable.setBounds(0, 0, DisplayUtil.dip2px(this, 30), DisplayUtil.dip2px(this, 30));
        txtUserTel.setCompoundDrawables(drawable,null,null,null);
        //------------------------------------------------------------------------------
        drawable= getResources().getDrawable(R.drawable.pic_login_code);
        drawable.setBounds(0, 0, DisplayUtil.dip2px(this, 30), DisplayUtil.dip2px(this, 30));
        txtVerifyCode.setCompoundDrawables(drawable, null, null, null);

        contactTels=getIntent().getStringArrayListExtra("Tels");
    }

    @Override
    public void onBackPressed() {
        Intent i =new Intent();
        i.putExtra("Login_State","Force_Close");
        setResult(MainActivity.USER_LOGIN_STATE, i);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLogin:
                currentTel=txtUserTel.getText().toString();
                if (!currentTel.equals("") && !txtVerifyCode.getText().toString().equals("") && isInContactList(currentTel)){
                    bu.verifySMSCode(txtUserTel.getText().toString(), txtVerifyCode.getText().toString(), new VerifySMSCodeListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                Toast.makeText(LoginActivity.this, "验证成功!", Toast.LENGTH_SHORT).show();
                                SharedPreferences.Editor se=sp.edit();
                                se.putString(MainActivity.KEY_USER_TEL, currentTel);
                                se.commit();
                                Intent i=new Intent();
                                i.putExtra("Login_State", "Verify_Success");
                                setResult(MainActivity.USER_LOGIN_STATE, i);
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "验证失败!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else {
                    Toast.makeText(LoginActivity.this,"请检查用户名是否正确!",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnGetVerifyCode:

                if (!txtUserTel.getText().toString().equals("") && isInContactList(txtUserTel.getText().toString())){
                    btnGetVerifyCode.setClickable(false);
                    bu.sendVerifySMS(txtUserTel.getText().toString(), new RequestSMSCodeListener() {
                        @Override
                        public void done(Integer integer, BmobException e) {
                            if (e==null){
                                //MainActivity.this.integer = integer;
                                Toast.makeText(LoginActivity.this,"短信发送成功!请注意查收!",Toast.LENGTH_SHORT).show();

                                ColorStateList whiteColor=getResources().getColorStateList(R.color.color_white);
                                btnGetVerifyCode.setTextColor(whiteColor);
                            }else {

                                if (e.getErrorCode()==10010){
                                    Toast.makeText(LoginActivity.this,"该号码今日短信发送上限!请隔日再登录!",Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(LoginActivity.this,"发送失败!请检查网络连接!",Toast.LENGTH_SHORT).show();
                                }
                                btnGetVerifyCode.setClickable(true);
                            }
                        }
                    });
                } else {
                    Toast.makeText(LoginActivity.this,"请检查用户名是否正确!",Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    private Boolean isInContactList(String tel){
        for (String s:contactTels){
            if (s.equals(tel)){
                return true;
            }
        }
        return false;
    }
}
