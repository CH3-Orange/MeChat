package com.example.orangepi.materialdesigntest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.orangepi.me.UserHelper;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.atomic.AtomicInteger;

public class SignInActiviy extends AppCompatActivity {
    Button Sign;
    TextInputEditText UserInput,UserName,PswInput,PswInputAgain,SexText;
    RadioGroup SexRadio;
    TextInputLayout PswInputLayout,PswInputAgainLayout;
    MaterialToolbar SignBar;
    boolean ifQuery=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_activiy);
        UserInput=findViewById(R.id.UserInput);
        UserName=findViewById(R.id.UserName);
        PswInput=findViewById(R.id.PswInput);
        PswInputAgain=findViewById(R.id.PswInputAgain);
        Sign=findViewById(R.id.Sign);
        SexRadio=findViewById(R.id.SexRadio);
        SexText=findViewById(R.id.SexText);
        PswInputLayout=findViewById(R.id.PswInputLayout);
        PswInputAgainLayout=findViewById(R.id.PswInputAgainLayout);
        SignBar = findViewById(R.id.SignAppBar);
        SignBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ifQuery)return ;//避免多次请求
                String name,uname,psw1,psw2,sex;
                AtomicInteger isNameRepeat= new AtomicInteger(0);
                name=UserInput.getText().toString();
                uname=UserName.getText().toString();
                psw1=PswInput.getText().toString();
                psw2=PswInputAgain.getText().toString();
                if(SexRadio.getCheckedRadioButtonId()==-1){
                    SexText.setError("请至少选择一项喔");
                    ifQuery=false;
                    return ;
                }
                sex=findViewById(SexRadio.getCheckedRadioButtonId()).getTag().toString();
                try{
                    UserHelper.CheckNameLegal(name);

                }catch (Exception ee){
                    UserInput.setError(ee.getMessage());
                    ifQuery=false;
                    return ;
                }
                new Thread(()->{
                    if(UserHelper.CheckNameRepeat(name)){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UserInput.setError("用户名已存在");

                            }
                        });
                        isNameRepeat.set(1);
                    }
                    else
                    {
                        isNameRepeat.set(-1);
                    }
                }).start();

                try {
                    UserHelper.CheckPasswordLegal(psw1);
                }catch (Exception ee){
                    //先关掉眼睛图标，再显示错误信息和错误提示图标
                    PswInputLayout.setEndIconVisible(false);
                    PswInput.setError(ee.getMessage());
                    ifQuery=false;
                    return;
                }
                if(!psw1.equals(psw2)){
                    //先关掉眼睛图标，再显示错误信息和错误提示图标
                    PswInputAgainLayout.setEndIconVisible(false);
                    PswInputAgain.setError("密码不一致");
                    ifQuery=false;
                    return;
                }

                new Thread(()->{
                    while(isNameRepeat.get() ==0);
                    if(isNameRepeat.get() ==1){
                        return;
                    }
                    long uid= UserHelper.SignUserInDatabase(name,uname,sex,psw1);
                    ifQuery=false;
                    if(uid!=-1){
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(),"Sign OK!  "+uname+" uid="+uid,Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                    else {
                        Looper.prepare();
                        Toast.makeText(getApplicationContext(),"错误用户: "+uname,Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }).start();
            }
        });

        PswInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //当文字改变的时候把错误信息消除，并可视化眼睛图标
                PswInputLayout.setError(null);
                PswInputLayout.setEndIconVisible(true);
            }
        });

        PswInputAgain.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //当文字改变的时候把错误信息消除，并可视化眼睛图标
                PswInputAgainLayout.setError(null);
                PswInputAgainLayout.setEndIconVisible(true);
            }
        });
    }
}
