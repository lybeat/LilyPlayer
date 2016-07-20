package com.lybeat.lilyplayer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.lybeat.lilyplayer.R;

public class MainActivity extends BaseActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BaseFragment currentFragment;
    private int itemId = R.id.menu_home;

    private VideoListFragment videoListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    @Override
    protected void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_tool_bar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
        toolbar.setTitleTextColor(0xfffafafa);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setCheckedItem(R.id.menu_home);
        navigationView.setNavigationItemSelectedListener(this);

        videoListFragment = new VideoListFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.main_fragment_container, videoListFragment).commit();
        currentFragment = videoListFragment;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        navigationView.setCheckedItem(itemId);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_home :
                if (videoListFragment == null) {
                    videoListFragment = new VideoListFragment();
                }
                switchFragment(videoListFragment);
                itemId = R.id.menu_home;
                break;
            case R.id.menu_setting :
                Intent intent = new Intent();
                intent.setClass(this, SettingActivity.class);
                startActivity(intent);
                break;
        }
        drawerLayout.closeDrawers();
        return true;
    }

    private void switchFragment(BaseFragment toFragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (currentFragment != toFragment) {
            if (toFragment.isAdded()) {
                ft.hide(currentFragment).show(toFragment).commit();
            } else {
                ft.hide(currentFragment).add(R.id.main_fragment_container, toFragment).commit();
            }
            currentFragment = toFragment;
        }
    }
}
