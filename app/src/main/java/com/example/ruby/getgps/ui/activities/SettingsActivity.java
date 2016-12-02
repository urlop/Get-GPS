package com.example.ruby.getgps.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.models.TripSave;
import com.example.ruby.getgps.models.User;
import com.example.ruby.getgps.ui.fragments.TripTabFragment;
import com.example.ruby.getgps.utils.Constants;
import com.example.ruby.getgps.utils.GeneralHelper;
import com.example.ruby.getgps.utils.PreferencesManager;
import com.example.ruby.getgps.utils.ServiceHelper;
import com.example.ruby.getgps.utils.TripHelper;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        user = (User) getIntent().getSerializableExtra(Constants.EDIT_PROFILE_USER_EXTRA);
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

    @OnClick(R.id.ll_sign_out)
    public void signingOut() {
        ServiceHelper.stopStartTripService();
        ServiceHelper.stopTripTrackingService(this, Constants.APP_WILL_TERMINATE); //stopMethod: "AppWillTerminate"
        PreferencesManager.getInstance(this).deleteAllSharedPreferences();
        if (TripTabFragment.getInstance() != null) {
            if (TripTabFragment.getInstance().getAllTripCardsAdapter() != null) {
                TripTabFragment.getInstance().getAllTripCardsAdapter().clear();
            }
            if (TripTabFragment.getInstance().getNewTripCardsAdapter() != null) {
                TripTabFragment.getInstance().getNewTripCardsAdapter().nullifyViewHolderLiveMap();
                TripTabFragment.getInstance().getNewTripCardsAdapter().clear();
            }
        }

        if (TripHelper.tripOngoing() != null) {
            TripSave.delete(TripHelper.tripOngoing());
        }

        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @OnClick(R.id.ll_change_password)
    public void changePassword() {
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        intent.putExtra(Constants.EDIT_PROFILE_USER_EXTRA, user);
        startActivity(intent);
    }

    @OnClick(R.id.ll_business_line)
    public void businessLine() {
        Intent intent = new Intent(SettingsActivity.this, WorkSourcesActivity.class);
        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra(Constants.EDIT_PROFILE_EXTRA, true);
        intent.putExtra(Constants.EDIT_PROFILE_USER_EXTRA, user);
        startActivityForResult(intent, Constants.EDIT_PROFILE);
    }

    @OnClick(R.id.ll_profile)
    public void editProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.putExtra(Constants.EDIT_PROFILE_USER_EXTRA, user);
        this.startActivityForResult(intent, Constants.EDIT_PROFILE);
    }

    @OnClick(R.id.ll_feedback)
    public void sendFeedback() {
        String userEmail = TripTabFragment.getInstance().getMainActivity().getUser().getEmail();
        String version = GeneralHelper.getAppVersionName(getApplication());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.support_email)});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject));
        intent.putExtra(Intent.EXTRA_TEXT, "app version: " + version + "\n" + "user email: " + userEmail + "\n \n \n");
        startActivity(Intent.createChooser(intent, ""));
    }

    @OnClick(R.id.ll_faq)
    public void toFaq() {
        String url = getString(R.string.help_center_url);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    @OnClick(R.id.ll_rate_everlance)
    public void rateEverlance() {
        String url = getString(R.string.rate_everlance_url);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.EDIT_PROFILE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        User user = (User) data.getSerializableExtra(Constants.EDIT_PROFILE_USER_EXTRA);
                        this.user = user;
                        TripTabFragment.getInstance().getMainActivity().setUserIncomeSources(user.getIncomeSources());
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
        }
    }
}
