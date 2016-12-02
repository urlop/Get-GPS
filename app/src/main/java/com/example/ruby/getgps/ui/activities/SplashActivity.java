package com.example.ruby.getgps.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.example.ruby.getgps.R;
import com.example.ruby.getgps.ui.fragments.SplashFragment;
import com.example.ruby.getgps.utils.PreferencesManager;
import com.example.ruby.getgps.utils.UIHelper;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SplashActivity extends AppCompatActivity {

    @Bind(R.id.bt_sign_in)
    Button bt_sign_in;

    @Bind(R.id.bt_sign_up)
    Button bt_sign_up;

    private Fragment contentFragment;
    private SplashFragment splashFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        checkIfLoggedIn();

        UIHelper.setupAlternativeStatusBar(this);
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (savedInstanceState != null) {
            if (fragmentManager.findFragmentByTag(SplashFragment.ARG_ITEM_ID) != null) {
                splashFragment = (SplashFragment) fragmentManager
                        .findFragmentByTag(SplashFragment.ARG_ITEM_ID);
                contentFragment = splashFragment;
            }
        } else {
            splashFragment = new SplashFragment();
            switchContent(splashFragment);
        }

    }

    private void switchContent(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        while (fragmentManager.popBackStackImmediate()) ;
        if (fragment != null) {
            FragmentTransaction transaction = fragmentManager
                    .beginTransaction();
            transaction.replace(R.id.content_frame, fragment, SplashFragment.ARG_ITEM_ID);
            // Only ProductDetailFragment is added to the back stack.
            if (!(fragment instanceof SplashFragment)) {
                transaction.addToBackStack(SplashFragment.ARG_ITEM_ID);
            }
            transaction.commit();
            contentFragment = fragment;
        }
    }

    @OnClick(R.id.bt_sign_in)
    void toSignInScreen() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.bt_sign_up)
    void toSignUpScreen() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    private void checkIfLoggedIn() {
        if (!PreferencesManager.getInstance(this).getAuthToken().isEmpty()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}
