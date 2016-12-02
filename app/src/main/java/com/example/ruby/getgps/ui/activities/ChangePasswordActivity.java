package com.example.ruby.getgps.ui.activities;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.models.User;
import com.example.ruby.getgps.utils.retrofit.CustomCallback;
import com.example.ruby.getgps.utils.retrofit.RequestManager;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    @Bind(R.id.til_current_password)
    TextInputLayout til_current_password;
    @Bind(R.id.et_current_password)
    EditText et_current_password;
    @Bind(R.id.til_new_password)
    TextInputLayout til_new_password;
    @Bind(R.id.et_new_password)
    EditText et_new_password;
    @Bind(R.id.til_confirm_new_password)
    TextInputLayout til_confirm_new_password;
    @Bind(R.id.et_confirm_new_password)
    EditText et_confirm_new_password;
    private boolean request = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (request) {
            menu.getItem(0).setEnabled(false);
        } else {
            menu.getItem(0).setEnabled(true);
        }
        return true;
    }

    private void attemptChangePassword() {
        boolean valid;
        til_current_password.setErrorEnabled(false);
        til_confirm_new_password.setErrorEnabled(false);
        til_new_password.setErrorEnabled(false);

        if (!isValidPassword(et_current_password.getText().toString())) {
            til_current_password.setErrorEnabled(true);
            til_current_password.setError(getString(R.string.password_invalid));
        }
        if (!isValidPassword(et_confirm_new_password.getText().toString())) {
            til_confirm_new_password.setErrorEnabled(true);
            til_confirm_new_password.setError(getString(R.string.password_invalid));
        }
        if (!isValidPassword(et_new_password.getText().toString())) {
            til_new_password.setErrorEnabled(true);
            til_new_password.setError(getString(R.string.password_invalid));
        }
        if (!et_new_password.getText().toString().isEmpty()
                && !et_confirm_new_password.getText().toString().isEmpty()
                && !et_confirm_new_password.getText().toString().equals(et_new_password.getText().toString())) {
            til_confirm_new_password.setErrorEnabled(true);
            til_confirm_new_password.setError(getString(R.string.password_does_not_match));
        }
        valid = isValidPassword(et_current_password.getText().toString())
                && isValidPassword(et_new_password.getText().toString())
                && isValidPassword(et_confirm_new_password.getText().toString()) &&
                et_confirm_new_password.getText().toString().equals(et_new_password.getText().toString());
        if (valid) {
            changePassword();
        }
    }


    private void changePassword() {
        HashMap<String, HashMap<String, Object>> userMap = new HashMap<>();
        HashMap<String, Object> valuesMap = new HashMap<>();
        valuesMap.put("verify_password", et_current_password.getText().toString());
        valuesMap.put("password", et_new_password.getText().toString());
        valuesMap.put("password_confirmation", et_confirm_new_password.getText().toString());
        userMap.put("user", valuesMap);
        request = true;
        invalidateOptionsMenu();
        Call<User> call = RequestManager.getDefault(this).changeUsersPassword(userMap);
        call.enqueue(new CustomCallback<User>(this, call) {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                super.onResponse(call, response);
                if (response.isSuccessful()) {
                    Toast.makeText(ChangePasswordActivity.this, R.string.ws_ok_send, Toast.LENGTH_SHORT).show();
                    request = false;
                    finish();
                } else {
                    Toast.makeText(ChangePasswordActivity.this, R.string.ws_error_send, Toast.LENGTH_SHORT).show();
                    request = false;
                    invalidateOptionsMenu();
                }
            }
        });
    }

    private boolean isValidPassword(final String password) {
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
