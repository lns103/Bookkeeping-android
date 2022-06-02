package xyz.lns103.bookkeeping.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.snackbar.Snackbar;

import xyz.lns103.bookkeeping.NetworkTool;
import xyz.lns103.bookkeeping.databinding.ActivityEditPasswordBinding;

public class EditPasswordActivity extends BaseActivity {

    ActivityEditPasswordBinding binding;
    String oldPassword;
    String newPassword;
    String checkPassword;
    String userID;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("login_information",MODE_PRIVATE);
        editor=sharedPreferences.edit();

        userID = sharedPreferences.getString("user_id","");

        binding.userId.setText(userID);

        binding.okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                oldPassword = String.valueOf(binding.oldPassword.getText());
                newPassword = String.valueOf(binding.newPassword.getText());
                checkPassword = String.valueOf(binding.passwordCheck.getText());
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                if(oldPassword.equals("")||newPassword.equals("")||checkPassword.equals("")) {
                    Snackbar.make(view, "密码不能为空", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if(!newPassword.equals(checkPassword)){
                    Snackbar.make(view,"两次密码不一致，请重新输入",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(!sharedPreferences.getString("password","").equals(oldPassword)){
                    Snackbar.make(view,"原密码不正确，请重新输入",Snackbar.LENGTH_SHORT).show();
                }else changePassword();

            }
        });
    }

    private void changePassword() {
        String url = "https://lns103.xyz/user/edit/user="+userID+"/password="+oldPassword +"/new_password="+newPassword;
        NetworkTool.httpGet(url,changePasswordHandler);
    }

    private void changeSuccess() {
        editor.putString("password",newPassword);
        editor.commit();
        super.onBackPressed();
    }

    private final Handler changePasswordHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {
            String receive = (String) msg.obj;
            if(receive.equals("success")) changeSuccess();
            else Snackbar.make(binding.getRoot(),receive,Snackbar.LENGTH_SHORT).show();
        };
    };


}