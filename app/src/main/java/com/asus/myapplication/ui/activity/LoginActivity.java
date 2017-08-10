package com.asus.myapplication.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.asus.myapplication.R;
import com.asus.myapplication.api.ILoginService;
import com.asus.myapplication.model.RegisterBean;
import com.asus.myapplication.util.BaseResp;
import com.asus.myapplication.util.HRetrofitNetHelper;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;

public class LoginActivity extends BaseActivity implements View.OnClickListener, HRetrofitNetHelper.RetrofitCallBack<RegisterBean> {
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.login_top)
    AppBarLayout loginTop;
    @Bind(R.id.iv_icon_left)
    ImageView ivIconLeft;
    @Bind(R.id.logo_ll)
    RelativeLayout logoLl;
    @Bind(R.id.email)
    AutoCompleteTextView email;
    @Bind(R.id.passwords)
    EditText passwords;
    @Bind(R.id.sign_in_button)
    Button signInButton;
    @Bind(R.id.email_register_button)
    Button emailRegisterButton;
    @Bind(R.id.email_login_form)
    LinearLayout emailLoginForm;
    @Bind(R.id.login_form)
    ScrollView loginForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        passwords.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    return true;
                }
                return false;
            }
        });


        signInButton.setOnClickListener(this);

    }


    public void startRegister(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                mDialog.setMessage("正在登录中，请稍后...");
                mDialog.show();
                ILoginService loginService = retrofitNetHelper.getAPIService(ILoginService.class);
                String username = email.getText().toString();
                String password = passwords.getText().toString();
                if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                    final Call<BaseResp<RegisterBean>> repos = loginService.userLogin(username, password);
                    retrofitNetHelper.enqueueCall(repos, this);
                }
                break;
        }
    }

    @Override
    public void onSuccess(BaseResp<RegisterBean> baseResp) {
        Log.d("zgx", "onResponse======" + baseResp.getData().getErrorCode());
        Date date = baseResp.getResponseTime();
        Log.d("zgx", "RegisterBean======" + date);
        if (baseResp.getData().getErrorCode() == 1) {
            Intent intent = new Intent(LoginActivity.this, NewsActivity.class);
            intent.putExtra("intent_user_id", String.valueOf(baseResp.getData().getUserId()));
            startActivity(intent);
            Toast.makeText(getBaseContext(), "登录成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getBaseContext(), "用户不存在", Toast.LENGTH_SHORT).show();
        }
        mDialog.dismiss();
    }

    @Override
    public void onFailure(String error) {
        Log.d("zgx", "onFailure======" + error);
        mDialog.dismiss();
    }
}

