package com.airremote.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.airremote.android.service.AirRemoteService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class BluetoothSelection extends Activity {
    class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                Log.d(AirRemote.TAG, "discovery finished");

                pdWait.dismiss();

                if (list.size() == 0) {
                    sendToast("No device found");
                    finish();
                }
            }

            if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                Log.d(AirRemote.TAG, "device found");

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBondState() == BluetoothDevice.BOND_BONDED)
                    return;

                String deviceName = device.getName();
                String deviceMac = device.getAddress();

                addToList(deviceMac, deviceName);
            }
        }
    }

    private ProgressDialog pdWait = null;

    private Context context;
    private ListView listView;

    private List<Map<String, String>> list = new ArrayList<Map<String, String>>();

    private Receiver receiver = new Receiver();

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        pdWait = ProgressDialog.show(this, "Please wait", "Searching Bluetooth devices...");
        pdWait.setCancelable(true);
        pdWait.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Log.d(AirRemote.TAG, "canceled");
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                finish();
            }
        });
        pdWait.show();

        setContentView(R.layout.device_selection);

        listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Log.d(AirRemote.TAG, "device selected: " + arg2);

                Map<String, String> map = list.get(arg2);
                String mac = map.get("mac");

                Log.d(AirRemote.TAG, "mac selected: " + mac);

                AirRemoteService.Preferences.remoteMacAddress = mac;
                saveMac(context, mac);

                sendToast("Select device set");
                finish();
            }
        });

        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                addToList(device.getAddress(), device.getName());
            }
        }

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);

        registerReceiver(receiver, intentFilter);

        mBluetoothAdapter.startDiscovery();
    }

    private void saveMac(Context context, String mac) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPreferences.edit();

        editor.putString("MAC", mac);
        editor.commit();
    }

    private void addToList(String mac, String name) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("mac", mac);
        map.put("name", name);
        list.add(map);

        displayList();
    }

    void displayList() {
        listView.setAdapter(new SimpleAdapter(this, list, R.layout.list_item, new String[] { "name", "mac"}, new int[] { R.id.text1, R.id.text2} ));
    }

    public void sendToast(String text) {
        Message m = new Message();
        m.what = 1;
        m.obj = text;
        messageHandler.sendMessage(m);
    }

    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 1:
                Toast.makeText(context, (CharSequence) msg.obj, Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}