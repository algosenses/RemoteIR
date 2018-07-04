package com.airremote.android;

import android.widget.Toast;

public class BaseTabActivity extends BaseActivity {
    private long lastChangeTime = -1;
    private int period = 5000; // 5s

    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        if (lastChangeTime > 0 && (now - lastChangeTime) < period) {
            finish();
        } else {
            Toast.makeText(this, getResources().getString(R.string.quit_info_doubleclick), Toast.LENGTH_SHORT).show();
            lastChangeTime = now;
        }
    }
}