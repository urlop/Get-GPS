package com.example.ruby.getgps.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.models.User;
import com.example.ruby.getgps.utils.retrofit.CustomCallback;
import com.example.ruby.getgps.utils.retrofit.RequestManager;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Bind(R.id.til_email)
    TextInputLayout til_email;
    @Bind(R.id.et_email)
    EditText et_email;
    @Bind(R.id.tv_confirmation_message)
    TextView tv_confirmation_message;
    @Bind(R.id.tv_sent_email)
    TextView tv_sent_email;
    @Bind(R.id.bt_forgot_password)
    Button bt_forgot_password;
    @Bind(R.id.iv_confirmation)
    ImageView iv_confirmation;
    @Bind(R.id.login_progress)
    ProgressBar login_progress;
    private String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(this);
        settingToolbar();
    }

    private void settingToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }


    // Check for a valid email address.
    private boolean checkValidEmail() {
        boolean valid = true;
        if (TextUtils.isEmpty(et_email.getText().toString())) {
            til_email.setError(getString(R.string.error_field_required));
            valid = false;
        } else if (!isEmailValid(et_email.getText().toString())) {
            til_email.setError(getString(R.string.error_invalid_email));
            valid = false;
        }
        return valid;
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    @OnClick(R.id.bt_forgot_password)
    protected void forgotPassword() {
        if (checkValidEmail()) {
            email = et_email.getText().toString();
            HashMap<String, Object> map = new HashMap<>();
            map.put("email", email);
            Call<User> call = RequestManager.getDefault(this).passwordReset(map);
            call.enqueue(new CustomCallback<User>(getApplicationContext(), call) {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    super.onResponse(call, response);
                    login_progress.setVisibility(View.VISIBLE);
                    if (response.isSuccessful()) {
                        successRequest();
                    } else {
                        login_progress.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void successRequest() {
        login_progress.setVisibility(View.GONE);
        bt_forgot_password.setVisibility(View.GONE);
        et_email.setVisibility(View.GONE);
        til_email.setVisibility(View.GONE);
        tv_sent_email.setVisibility(View.VISIBLE);
        tv_confirmation_message.setVisibility(View.VISIBLE);
        iv_confirmation.setVisibility(View.VISIBLE);
        tv_sent_email.setText("to " + email);
    }

    @OnClick(R.id.tv_nevermind_login)
    protected void toLoginScreen() {
        Intent intent = new Intent(ForgotPasswordActivity.this, SignInActivity.class);
        startActivity(intent);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }
}
