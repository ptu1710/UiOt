package com.ixxc.uiot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;


public class LoginActivity extends AppCompatActivity {
    public static boolean isDefaultPage;
    public Fragment welcome, sign_in, sign_up;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        InitVars();
        InitViews();
        InitEvent();

        replaceFragment(welcome);
    }

    private void InitVars() {
        welcome = new WelcomeFragment(this);
        sign_in = new SignInFragment(this);
        sign_up = new SignUpFragment(this);

        isDefaultPage = true;
    }

    private void InitViews() { }

    private void InitEvent() { }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        if (fragment == welcome) {
            isDefaultPage = true;
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        } else {
            isDefaultPage = false;
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }

        ft.replace(R.id.loginFrame, fragment);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        if (isDefaultPage) {
            super.onBackPressed();
            finish();
        }
        else {
            replaceFragment(welcome);
        }
    }
}