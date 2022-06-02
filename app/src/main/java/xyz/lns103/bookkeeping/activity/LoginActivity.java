package xyz.lns103.bookkeeping.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


import com.google.android.material.snackbar.Snackbar;

import org.litepal.LitePal;
import org.litepal.LitePalDB;

import java.util.List;

import xyz.lns103.bookkeeping.NetworkTool;
import xyz.lns103.bookkeeping.R;
import xyz.lns103.bookkeeping.databinding.ActivityLoginBinding;

public class LoginActivity extends BaseActivity {

    ActivityLoginBinding binding;
    boolean register = false;
    String userID;
    String password;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("login_information",MODE_PRIVATE);
        editor=sharedPreferences.edit();

        boolean login = sharedPreferences.getBoolean("login",false);
        if(login) enterMainActivity();


        binding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(register){
                    register = false;
                    binding.enterButton.setText(R.string.login);
                    binding.title.setText(R.string.login);
                    binding.register.setText(R.string.register);
                    binding.passwordCheckView.setVisibility(View.INVISIBLE);
                }else{
                    register = true;
                    binding.enterButton.setText(R.string.register);
                    binding.title.setText(R.string.register);
                    binding.register.setText(R.string.login);
                    binding.passwordCheckView.setVisibility(View.VISIBLE);
                }
            }
        });
        
        binding.enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userID = String.valueOf(binding.userId.getText());
                password = String.valueOf(binding.password.getText());
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                if(userID.equals("")||password.equals("")) {
                    Snackbar.make(view, "用户名和密码不能为空", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(register){
                    String password2 = String.valueOf(binding.passwordCheck.getText());
                    if(password.equals(password2)) register();
                    else Snackbar.make(view,"两次密码不一致，请重新输入",Snackbar.LENGTH_SHORT).show();
                }
                else login();
            }
        });
    }

    private void login() {
        String url = "https://lns103.xyz/user/login/user="+userID+"/password="+password;
        NetworkTool.httpGet(url,loginHandler);
    }

    private void register() {
        String url = "https://lns103.xyz/user/register/user="+userID+"/password="+password;
        NetworkTool.httpGet(url,registerHandler);
    }

    private void loginSuccess() {
        editor.putString("user_id",userID);
        editor.putString("password",password);
        editor.putBoolean("login",true);
        editor.commit();
        enterMainActivity();
    }

    private void registerSuccess() {
        loginSuccess();
    }

    private void enterMainActivity(){
        String dbName = sharedPreferences.getString("user_id","default");
        LitePalDB litePalDB = LitePalDB.fromDefault(dbName);
        LitePal.use(litePalDB);
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(this,MainActivity.class);
        startActivity(intent);
    }

    private final Handler loginHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {
            String receive = (String) msg.obj;
            if(receive.equals("success")) loginSuccess();
            else Snackbar.make(binding.getRoot(),receive,Snackbar.LENGTH_SHORT).show();
        };
    };

    private final Handler registerHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {
            String receive = (String) msg.obj;
            if(receive.equals("success")) registerSuccess();
            else Snackbar.make(binding.getRoot(),receive,Snackbar.LENGTH_SHORT).show();
        };
    };
}