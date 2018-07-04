package com.airremote.android;

import com.airremote.android.service.AirRemoteService;
import com.airremote.android.service.Protocol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.Button;

public class AirRemote extends Activity {
    public static final String TAG = "AirRemote";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.apple);

        Button btnVolUp = (Button) findViewById(R.id.apple_volup);
        Button btnVolDn = (Button) findViewById(R.id.apple_voldn);
        Button btnMute  = (Button) findViewById(R.id.apple_mute);
        Button btnPower = (Button) findViewById(R.id.apple_power);
        Button btnPrev  = (Button) findViewById(R.id.apple_prev);
        Button btnNext  = (Button) findViewById(R.id.apple_next);

        btnVolUp.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionevent) {
                int action = motionevent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                }

                return false;
            }
         });

        btnPower.setOnTouchListener(new OnTouchListener() {
           @Override
           public boolean onTouch(View view, MotionEvent motionevent) {
               int action = motionevent.getAction();
               if (action == MotionEvent.ACTION_DOWN) {
//                   Protocol.getDeviceType();
//                   int bursts[] = {0x00AB*26, 0x00AD*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0017*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0017*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0017*26, 0x0013*26, 0x0016*26, 0x0014*26, 0x0017*26, 0x0013*26, 0x0017*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0017*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0041*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0017*26, 0x0014*26, 0x0016*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0041*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x06F9*26, 0x00AB*26, 0x00AE*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x048D*26, 0x00AA*26, 0x00AE*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0458*26,};
                   int bursts[] = {0x0156*26, 0x00AC*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0018*26, 0x0013*26, 0x0017*26, 0x0013*26, 0x0018*26, 0x0013*26, 0x0018*26, 0x0013*26, 0x0017*26, 0x0013*26, 0x0018*26, 0x0013*26, 0x0018*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0017*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0040*26, 0x0016*26, 0x0042*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0017*26, 0x0013*26, 0x0018*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0017*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0043*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0017*26, 0x0013*26, 0x0018*26, 0x0013*26, 0x0017*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0017*26, 0x0013*26, 0x0017*26, 0x0013*26, 0x060F*26, 0x0156*26, 0x0057*26, 0x0013*26, 0x0498*26, 0x0156*26, 0x0057*26, 0x0013*26, 0x0458*26,
};
                   Log.d(TAG, "bursts length:" + bursts.length);
//                   Protocol.sendTestData();
               }

               return false;
           }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.start:
            startService();
            return true;
        case R.id.stop:
            stopService();
            return true;
        case R.id.devices:
            startActivity(new Intent(this, BluetoothSelection.class));
            return true;
        case R.id.about:
            showAbout();
            return true;
        case R.id.exit:
            exit();
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    void startService() {
        Log.d(TAG, "startService");
        startService(new Intent(this, AirRemoteService.class));
    }

    void stopService() {
        stopService(new Intent(this, AirRemoteService.class));
    }

    void exit() {
        System.exit(0);
    }

    void showAbout() {
        WebView webView = new WebView(this);
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><title>About</title></head><body>" +
                "<h1>AirRemote</h1>" +
                "<p>Version " + "0.01" + ".</p>" +
                "<p>&copy; Copyright 2013 Air Remote Ltd.</p>" +
                "</body></html>";
        webView.loadData(html, "text/html", "utf-8");

        new AlertDialog.Builder(this).setView(webView).setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }
}
