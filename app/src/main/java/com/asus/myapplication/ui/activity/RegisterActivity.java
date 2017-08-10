package com.asus.myapplication.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.asus.myapplication.R;
import com.asus.myapplication.api.IRegisterService;
import com.asus.myapplication.model.RegisterBean;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity implements Callback<RegisterBean> {
    @Bind(R.id.id_username)
    AutoCompleteTextView idUsername;
    @Bind(R.id.passwords)
    EditText passwords;
    @Bind(R.id.confirmation_password)
    EditText confirmationPassword;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);


    }

    public void startRegister(View view) {
        String userName = idUsername.getText().toString();
        String password = passwords.getText().toString();
        String mConfirmation = confirmationPassword.getText().toString();
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password)
                && !TextUtils.isEmpty(mConfirmation)) {
            if (password.equals(mConfirmation)) {
                IRegisterService loginService = retrofitNetHelper.getAPIService(IRegisterService.class);
                Map<String, String> mParamsMap = new HashMap<>();
                mParamsMap.put("username", userName);
                mParamsMap.put("password", password);
                Call<RegisterBean> call = loginService.createUser(mParamsMap);
                call.enqueue(this);
            } else {
                Toast.makeText(getBaseContext(), "密码不一致", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getBaseContext(), "请填写完整", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResponse(Call<RegisterBean> call, Response<RegisterBean> response) {
        if (response.body().getErrorCode() == 1) {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(getBaseContext(), "注册失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(Call<RegisterBean> call, Throwable t) {

    }
}

