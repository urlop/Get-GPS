package com.example.ruby.getgps.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.models.APIError;
import com.example.ruby.getgps.models.SignUpUser;
import com.example.ruby.getgps.models.User;
import com.example.ruby.getgps.utils.PreferencesManager;
import com.example.ruby.getgps.utils.retrofit.CustomCallback;
import com.example.ruby.getgps.utils.retrofit.RequestManager;

import java.lang.annotation.Annotation;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import timber.log.Timber;


public class SignUpActivity extends AppCompatActivity {

    @Bind(R.id.bt_sign_up)
    Button bt_sign_up;
    @Bind(R.id.et_email)
    EditText et_email;
    @Bind(R.id.et_password)
    EditText et_password;
    @Bind(R.id.et_invite_code)
    EditText et_invite_code;
    @Bind(R.id.login_form)
    View login_form;
    @Bind(R.id.til_email)
    TextInputLayout til_email;
    @Bind(R.id.til_password)
    TextInputLayout til_password;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        settingToolbar();
        et_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attempSignUp();
                    return true;
                }
                return false;
            }
        });
        et_invite_code.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attempSignUp();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        et_email.requestFocus();
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;
        super.onDestroy();
    }

    private void settingToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
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


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attempSignUp() {
        // Reset errors.
        et_email.setError(null);
        et_password.setError(null);

        // Store values at the time of the login attempt.
        String email = et_email.getText().toString();
        String password = et_password.getText().toString();

        boolean cancel;
        View focusView = null;
        if (et_password.getText().toString().isEmpty()) {
            focusView = et_password;
            cancel = true;
            til_password.setError(getString(R.string.error_field_required));
        } else {
            cancel = false;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            til_password.setError(getString(R.string.error_invalid_password));
            focusView = et_password;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            til_email.setError(getString(R.string.error_field_required));
            focusView = et_email;
            cancel = true;
        } else if (!isEmailValid(email)) {
            til_email.setError(getString(R.string.error_invalid_email));
            focusView = et_email;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            signUpUser(email, password);
        }
    }

    private void signUpUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        SignUpUser signUpUser = new SignUpUser(user);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.signing_up));
        progressDialog.show();
        Call<User> call = RequestManager.getDefault(this).signUpUser(signUpUser);
        call.enqueue(new CustomCallback<User>(this, call) {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                super.onResponse(call, response);
                if (response.isSuccessful()) {
                    User user = response.body();
                    saveAuthTokenToSharedPreferences(user.getAuthToken());

                    //TODO: render to Tutorial first. When in tutorial, user can't go back to login. But in work sources, can go back to tutorial.
                    Intent intent = new Intent(SignUpActivity.this, WorkSourcesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // TODO: check this migration to Retrofit2
                    Converter<ResponseBody, APIError> converter = RequestManager.getRetrofit().responseBodyConverter(APIError.class, new Annotation[0]);
                    try {
                        APIError errors = converter.convert(response.errorBody());

                        //TODO: Check what will happen if the error is not email related
                        Toast.makeText(SignUpActivity.this, errors.getEmail().get(0), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Timber.e(e, "method=signUpUser");
                    }
                }

            }
        });
    }

    private void saveAuthTokenToSharedPreferences(String authToken) {
        PreferencesManager.getInstance(this).saveAuthToken(authToken);
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    @OnClick(R.id.bt_sign_up)
    void signUpUser() {
        attempSignUp();
    }
}
