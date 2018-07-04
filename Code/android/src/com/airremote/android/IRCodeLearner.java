package com.airremote.android;

import com.airremote.android.database.DbHelper;
import com.airremote.android.service.AirRemoteService;
import com.airremote.android.service.IAirRemoteService;
import com.airremote.android.service.IAirRemoteServiceCallback;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class IRCodeLearner extends Activity implements View.OnTouchListener {
    public static final String TAG = "IRCodeLearner";

    private static final int MENU_GROUP_NORMAL = 1;
    private static final int MENU_GROUP_LEARN = 2;

    private DbHelper mDbHelper;
    private IRDecoder mIRDecoder;
    private String mIRCodeString;

    private boolean mIsLearnMode = false;

    private Button mBtnVolUp;
    private Button mBtnVolDn;
    private Button mBtnMute;
    private Button mBtnPower;
    private Button mBtnPrev;
    private Button mBtnNext;

    private Button mBtnDigit_0;
    private Button mBtnDigit_1;
    private Button mBtnDigit_2;
    private Button mBtnDigit_3;
    private Button mBtnDigit_4;
    private Button mBtnDigit_5;
    private Button mBtnDigit_6;
    private Button mBtnDigit_7;
    private Button mBtnDigit_8;
    private Button mBtnDigit_9;

    private Button mBtnInfo;
    private Button mBtnMute2;

    IAirRemoteService mService = null;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IAirRemoteService.Stub.asInterface(service);
            try {
                mService.registerCallback(mCallback);
            } catch (RemoteException e) {

            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    private IAirRemoteServiceCallback mCallback = new IAirRemoteServiceCallback.Stub() {
        public void onDeviceRespond(int type, int options, byte[] data) {
            if (data != null && data.length > 0) {
                int length = data.length;
                Log.d(TAG, "onDeviceRespond, length: " + length);
                if ((length % 2) == 0) {
                    int bursts[] = new int[length / 2];
//                    String str = "received: ";
                    mIRCodeString = "";
                    for (int i = 0; i < bursts.length; i++) {
                        byte hi = data[i*2+1];
                        byte lo = data[i*2];
                        bursts[i] = ((((hi & 0xFF) << 8) | (lo & 0xFF)) * 8 / 26) & 0xFFFF;
                        mIRCodeString += Integer.toHexString(bursts[i]).toUpperCase() + " ";
//                        str += "0x" + Integer.toHexString(bursts[i]) + ", ";
                    }

//                    Log.d(TAG, str);
                    Log.d(TAG, mIRCodeString);

                    mIRDecoder.setBursts(bursts, 0);
                    mIRDecoder.setFrequency(-1);
                    mIRDecoder.init();

                    if (mIRDecoder.decode()) {
                        Toast.makeText(IRCodeLearner.this,
                                mIRDecoder.getProtocolName() +
                                " " + mIRDecoder.getDevice() +
                                " " + mIRDecoder.getSubDevice() +
                                " " + mIRDecoder.getOBC(), Toast.LENGTH_SHORT).show();
//                        Log.d(TAG, mIRDecoder.getProtocolName() +
//                                " " + mIRDecoder.getDevice() +
//                                " " + mIRDecoder.getSubDevice() +
//                               " " + mIRDecoder.getOBC());
                    } else {
                        mIRCodeString = null;
                        Toast.makeText(IRCodeLearner.this, "Unknown IR Code.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.apple);

        mBtnVolUp = (Button) findViewById(R.id.apple_volup);
        mBtnVolDn = (Button) findViewById(R.id.apple_voldn);
        mBtnMute  = (Button) findViewById(R.id.apple_mute);
        mBtnPower = (Button) findViewById(R.id.apple_power);
        mBtnPrev  = (Button) findViewById(R.id.apple_prev);
        mBtnNext  = (Button) findViewById(R.id.apple_next);

        mBtnDigit_0 = (Button) findViewById(R.id.digit_0);
        mBtnDigit_1 = (Button) findViewById(R.id.digit_1);
        mBtnDigit_2 = (Button) findViewById(R.id.digit_2);
        mBtnDigit_3 = (Button) findViewById(R.id.digit_3);
        mBtnDigit_4 = (Button) findViewById(R.id.digit_4);
        mBtnDigit_5 = (Button) findViewById(R.id.digit_5);
        mBtnDigit_6 = (Button) findViewById(R.id.digit_6);
        mBtnDigit_7 = (Button) findViewById(R.id.digit_7);
        mBtnDigit_8 = (Button) findViewById(R.id.digit_8);
        mBtnDigit_9 = (Button) findViewById(R.id.digit_9);

        mBtnInfo = (Button) findViewById(R.id.btn_info);
        mBtnMute2 = (Button) findViewById(R.id.btn_mute);

        mBtnVolUp.setOnTouchListener(this);
        mBtnVolDn.setOnTouchListener(this);
        mBtnMute.setOnTouchListener(this);
        mBtnPower.setOnTouchListener(this);
        mBtnPrev.setOnTouchListener(this);
        mBtnNext.setOnTouchListener(this);

        mBtnDigit_0.setOnTouchListener(this);
        mBtnDigit_1.setOnTouchListener(this);
        mBtnDigit_2.setOnTouchListener(this);
        mBtnDigit_3.setOnTouchListener(this);
        mBtnDigit_4.setOnTouchListener(this);
        mBtnDigit_5.setOnTouchListener(this);
        mBtnDigit_6.setOnTouchListener(this);
        mBtnDigit_7.setOnTouchListener(this);
        mBtnDigit_8.setOnTouchListener(this);
        mBtnDigit_9.setOnTouchListener(this);

        mBtnInfo.setOnTouchListener(this);
        mBtnMute2.setOnTouchListener(this);

/*
        mBtnVolUp.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionevent) {
                int action = motionevent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    try {
//                        mService.getDeviceType();
                        mService.DeviceEnterIRReceiveMode();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    if (mIRCodeString != null && !mIRCodeString.isEmpty()) {
                        mDbHelper.UpdateTestIRCode(1, mIRCodeString);
                    }
                }

                return false;
            }
         });

        mBtnPower.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionevent) {
                int action = motionevent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    try {
                        mService.getDeviceType();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                return false;
            }
         });
*/

        mDbHelper = new DbHelper(this);
        mIRDecoder = new IRDecoder();

//        int bursts[] = {0x00AB*26, 0x00AD*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0017*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0017*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0017*26, 0x0013*26, 0x0016*26, 0x0014*26, 0x0017*26, 0x0013*26, 0x0017*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0017*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0016*26, 0x0014*26, 0x0041*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0017*26, 0x0014*26, 0x0016*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0042*26, 0x0013*26, 0x0041*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x06F9*26, 0x00AB*26, 0x00AE*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x048D*26, 0x00AA*26, 0x00AE*26, 0x0014*26, 0x0041*26, 0x0014*26, 0x0458*26,};

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int keyIdx = 0;
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            if (v == mBtnVolUp) {
                keyIdx = 1;
            } else if (v == mBtnVolDn) {
                keyIdx = 2;
            } else if (v == mBtnMute) {
                keyIdx = 3;
            } else if (v == mBtnPower) {
                keyIdx = 4;
            } else if (v == mBtnPrev) {
                keyIdx = 5;
            } else if (v == mBtnNext) {
                keyIdx = 6;
            } else if (v == mBtnDigit_0) {
                keyIdx = 7;
            } else if (v == mBtnDigit_1) {
                keyIdx = 8;
            } else if (v == mBtnDigit_2) {
                keyIdx = 9;
            } else if (v == mBtnDigit_3) {
                keyIdx = 10;
            } else if (v == mBtnDigit_4) {
                keyIdx = 11;
            } else if (v == mBtnDigit_5) {
                keyIdx = 12;
            } else if (v == mBtnDigit_6) {
                keyIdx = 13;
            } else if (v == mBtnDigit_7) {
                keyIdx = 14;
            } else if (v == mBtnDigit_8) {
                keyIdx = 15;
            } else if (v == mBtnDigit_9) {
                keyIdx = 16;
            } else if (v == mBtnInfo) {
                keyIdx = 17;
            } else if (v == mBtnMute2) {    // same as mBtnMute
                keyIdx = 3;
            }

            if (mIsLearnMode) {
                if (keyIdx != 0 && mIRCodeString != null && mIRCodeString != "") {
                    mDbHelper.UpdateTestIRCode(keyIdx, mIRCodeString);
                    mIRCodeString = null;
                    Toast.makeText(this, "Learn IR Code Done.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Press remote controller key.", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (keyIdx != 0) {
                    String codeStr = mDbHelper.ReadTestIRCode(keyIdx);
                    if (codeStr == null || codeStr == "") {
                        return false;
                    }

                    String[] result = codeStr.split(" ");
                    if (result.length > 0) {
                        int[] codes = new int[result.length];
                        for (int i = 0; i < result.length; i++) {
                            codes[i] = Integer.parseInt(result[i].trim(), 16);
                        }

                        try {
                            mService.SendRawIRCode(codes);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        bindService(new Intent(this, AirRemoteService.class),
                mConnection, Context.BIND_AUTO_CREATE);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(MENU_GROUP_NORMAL, R.id.menu_start, 1, R.string.menu_start);
        menu.add(MENU_GROUP_NORMAL, R.id.menu_stop, 1, R.string.menu_stop);
        menu.add(MENU_GROUP_NORMAL, R.id.menu_devices, 1, R.string.menu_devices);
        menu.add(MENU_GROUP_NORMAL, R.id.menu_learn, 1, R.string.menu_learn);
        menu.add(MENU_GROUP_NORMAL, R.id.menu_about, 1, R.string.menu_about);
        menu.add(MENU_GROUP_NORMAL, R.id.menu_exit, 1, R.string.menu_exit);

        menu.add(MENU_GROUP_LEARN, R.id.menu_learn_done, 1, R.string.menu_learn_done);

        menu.setGroupVisible(MENU_GROUP_NORMAL, false);
        menu.setGroupVisible(MENU_GROUP_LEARN, false);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mIsLearnMode) {
            menu.setGroupVisible(MENU_GROUP_LEARN, true);
            menu.setGroupVisible(MENU_GROUP_NORMAL, false);
        } else {
            menu.setGroupVisible(MENU_GROUP_LEARN, false);
            menu.setGroupVisible(MENU_GROUP_NORMAL, true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_start:
            return true;
        case R.id.menu_stop:
            return true;
        case R.id.menu_devices:
            startActivity(new Intent(this, BluetoothSelection.class));
            return true;
        case R.id.menu_learn:
            try {
                mService.DeviceEnterIRReceiveMode();
                mIsLearnMode = true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return true;
        case R.id.menu_about:
            return true;
        case R.id.menu_exit:
            System.exit(0);
            return true;
        case R.id.menu_learn_done:
            mIsLearnMode = false;
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            try {
                mService.unregisterCallback(mCallback);
            } catch (RemoteException e) {

            }
        }

        unbindService(mConnection);
    }
}
