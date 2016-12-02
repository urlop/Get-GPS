package com.example.ruby.getgps.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.models.ReferralCodeAttributes;
import com.example.ruby.getgps.models.User;
import com.example.ruby.getgps.utils.Constants;
import com.example.ruby.getgps.utils.UIHelper;
import com.example.ruby.getgps.utils.retrofit.CustomCallback;
import com.example.ruby.getgps.utils.retrofit.RequestManager;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Response;

@SuppressWarnings({"ALL", "WeakerAccess"})
public class EditProfileActivity extends AppCompatActivity {


    @Bind(R.id.et_name)
    EditText et_name;
    @Bind(R.id.et_last_name)
    EditText et_last_name;
    @Bind(R.id.et_email)
    EditText et_email;
    @Bind(R.id.et_mobile)
    EditText et_mobile;
    @Bind(R.id.et_invite_code)
    EditText et_invite_code;
    @Bind(R.id.et_mileage_rate)
    EditText et_mileage_rate;
    @Bind(R.id.v_progress)
    ProgressBar v_progress;
    @Bind(R.id.ti_email)
    TextInputLayout ti_email;
    @Bind(R.id.ti_mileage_rate)
    TextInputLayout ti_mileage_rate;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        user = (User) getIntent().getSerializableExtra(Constants.EDIT_PROFILE_USER_EXTRA);
        if (user != null) {
            fillUserData(user);
        }
    }

    private void fillUserData(final User user) {
        et_name.setText(user.getName());
        et_last_name.setText(user.getLast());
        et_email.setText(user.getEmail());
        et_mobile.setText(user.getMobile());
        et_invite_code.setText(user.getReferralCodeName());
        et_mileage_rate.setText(user.getCustomMileageRate().toString());
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

    @OnClick(R.id.ib_update_profile)
    protected void uploadTrip() {
        if (validEmail(et_email.getText().toString()) && validMileageRate(et_mileage_rate.getText().toString())) {
            User user = new User();
            user.setName(et_name.getText().toString());
            user.setLast(et_last_name.getText().toString());
            user.setMobile(et_mobile.getText().toString());
            user.setEmail(et_email.getText().toString());
            user.setCustomMileageRate(Float.parseFloat(et_mileage_rate.getText().toString()));
            ReferralCodeAttributes referralCodeAttributes = new ReferralCodeAttributes();
            referralCodeAttributes.setName(et_invite_code.getText().toString());
            user.setReferralCodeAttributes(referralCodeAttributes);
            updateProfile(user);
        }
    }

    private boolean validMileageRate(String mileageRate) {
        if (TextUtils.isEmpty(mileageRate)) {
            ti_mileage_rate.setError(getString(R.string.error_field_required));
            return false;
        }
        return true;
    }

    private boolean validEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            ti_email.setError(getString(R.string.error_field_required));
            return false;

        } else if (!isEmailValid(email)) {
            ti_email.setError(getString(R.string.error_invalid_email));
            return false;
        }
        return true;
    }

    private void updateProfile(User user) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("user", user);
        Call<User> call = RequestManager.getDefault(this).updateCurrentUser(map);
        call.enqueue(new CustomCallback<User>(this, call) {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                super.onResponse(call, response);
                UIHelper.showProgress(EditProfileActivity.this, v_progress, null, false);
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, R.string.ws_ok_send, Toast.LENGTH_SHORT).show();
                    User user = response.body();
                    Intent intent = new Intent();
                    intent.putExtra(Constants.EDIT_PROFILE_USER_EXTRA, user);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, R.string.ws_error_send, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
