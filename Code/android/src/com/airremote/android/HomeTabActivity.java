package com.airremote.android;

import com.airremote.android.editor.RemoteControllerEditor;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

public class HomeTabActivity extends TabActivity {
    private static final String TAG = "HomeTabActivity";

    private LayoutInflater mInflater;
    private TabHost mTabHost;
    private TabWidget mTabWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_tab);

        mInflater = getLayoutInflater();
        mTabHost = getTabHost();
        mTabWidget = (TabWidget) findViewById(android.R.id.tabs);

        mTabHost.addTab(mTabHost
                .newTabSpec("ListDevice")
                .setIndicator(
                        getTab(getString(R.string.home_tab_list_device),
                                R.drawable.home_tab_button1))
                .setContent(new Intent(this, DeviceListActivity.class)));

        mTabHost.addTab(mTabHost.newTabSpec("AddDevice")
                .setIndicator(
                        getTab(getString(R.string.home_tab_add_device), R.drawable.home_tab_button2))
                .setContent(new Intent(this, IRCodeLearner.class)));

        mTabHost.addTab(mTabHost.newTabSpec("Settings")
                .setIndicator(
                        getTab(getString(R.string.home_tab_settings), R.drawable.home_tab_button3))
                .setContent(new Intent(this, SettingsActivity.class)));

        mTabHost.addTab(mTabHost.newTabSpec("Help")
                .setIndicator(
                        getTab(getString(R.string.home_tab_help), R.drawable.home_tab_button4))
                .setContent(new Intent(this, HelpActivity.class)));
    }

    private View getTab(String text, int tabResId) {
        View tab = mInflater.inflate(R.layout.home_tab_spec, mTabWidget, false);
        ((ImageView) tab.findViewById(R.id.icon)).setImageResource(tabResId);
        ((TextView) tab.findViewById(R.id.text)).setText(text);
        return tab;
    }
}