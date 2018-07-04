package com.airremote.android.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.airremote.android.AirRemote;
import com.airremote.android.service.IAirRemoteService;
import com.airremote.android.service.IAirRemoteServiceCallback;
import com.airremote.android.service.Protocol.HostMessage;
import com.airremote.android.R;


public class AirRemoteService extends Service {
    public static final String TAG = "AirRemoteService";

    private final RemoteCallbackList<IAirRemoteServiceCallback> mCallbacks
            = new RemoteCallbackList<IAirRemoteServiceCallback>();

    private Protocol mProtocol = new Protocol(this);
    private HostMessage mHostMsg = new HostMessage();
    private ArrayList<byte[]> mSendQueue = new ArrayList<byte[]>();
    private boolean isSending = false;

    private Context mCtx;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    private NotificationManager mNM;
    private RemoteViews remoteViews;
    private Notification notification;

    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    private int connectionState;

    final class ConnectionState {
        static final int DISCONNECTED = 0;
        static final int CONNECTING = 1;
        static final int CONNECTED = 2;
        static final int DISCONNECTING = 3;
    }

    public static class Preferences {
        public static boolean   startOnBoot = false;
        public static String    remoteMacAddress = "";
        public static int       packetWait = 10;
        public static boolean   skipSDP = false;
    }

    public void loadPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        Preferences.startOnBoot = sharedPreferences.getBoolean("StartOnBoot", Preferences.startOnBoot);
        Preferences.remoteMacAddress = sharedPreferences.getString("MAC", Preferences.remoteMacAddress);
        Preferences.skipSDP = sharedPreferences.getBoolean("SkipSDP", Preferences.skipSDP);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IAirRemoteService.Stub mBinder = new IAirRemoteService.Stub() {
        public void registerCallback(IAirRemoteServiceCallback cb) {
            if (cb != null) {
                mCallbacks.register(cb);
            }
        }

        public void unregisterCallback(IAirRemoteServiceCallback cb) {
            if (cb != null) {
                mCallbacks.unregister(cb);
            }
        }

        public void getDeviceType() throws RemoteException {
            mProtocol.getDeviceType();
        }

        @Override
        public void DeviceEnterIRReceiveMode() throws RemoteException {
            mProtocol.DeviceEnterIRReceiveMode();
        }

        @Override
        public void SendRawIRCode(int[] data) throws RemoteException {
            mProtocol.SendRawIRCode(data);
        }
    };

    public void createNotification() {
        notification = new Notification(R.drawable.disconnected_large, null, System.currentTimeMillis());
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        remoteViews.setImageViewResource(R.id.image, R.drawable.disconnected);
        remoteViews.setTextViewText(R.id.text, "AirRemote service is running");
        notification.contentView = remoteViews;

        Intent notificationIntent = new Intent(this, AirRemote.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.contentIntent = contentIntent;

        startForeground(1, notification);
    }

    public void updateNotification(boolean connected) {
        if (connected) {
            notification.icon = R.drawable.connected;
            remoteViews.setImageViewResource(R.id.image, R.drawable.connected_large);
        } else {
            notification.icon = R.drawable.disconnected;
            remoteViews.setImageViewResource(R.id.image, R.drawable.disconnected_large);
        }
        startForeground(1, notification);
    }

    public void removeNotification() {
        stopForeground(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mCtx = this;
        loadPreferences(mCtx);

        createNotification();

        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        connectionState = ConnectionState.CONNECTING;

        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AirRemote");

        start();
    }

    @Override
    public void onDestroy() {
        disconnectExit();
        super.onDestroy();

        removeNotification();

        mCallbacks.kill();
    }

    private void connect(Context context) {
        try {
            Log.d(TAG, "Remote device address: " + Preferences.remoteMacAddress);
            if (Preferences.remoteMacAddress.equals("")) {
                loadPreferences(context);
            }
            BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(Preferences.remoteMacAddress);

            if (Preferences.skipSDP) {
                Method method = bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
                mBluetoothSocket = (BluetoothSocket) method.invoke(bluetoothDevice, 1);
            } else {
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                mBluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            }

            mBluetoothSocket.connect();
            mInputStream = mBluetoothSocket.getInputStream();
            mOutputStream = mBluetoothSocket.getOutputStream();

            connectionState = ConnectionState.CONNECTED;
            updateNotification(true);
        } catch (IOException ioexception) {
            Log.d(TAG, ioexception.toString());
        } catch (SecurityException e) {
            Log.d(TAG, e.toString());
        } catch (NoSuchMethodException e) {
            Log.d(TAG, e.toString());
        } catch (IllegalArgumentException e) {
            Log.d(TAG, e.toString());
        } catch (IllegalAccessException e) {
            Log.d(TAG, e.toString());
        } catch (InvocationTargetException e) {
            Log.d(TAG, e.toString());
        }
    }

    private static class ServiceHandler extends Handler {
        private final WeakReference<AirRemoteService> mService;

        ServiceHandler(AirRemoteService service) {
            mService = new WeakReference<AirRemoteService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            AirRemoteService service = mService.get();
            if (service != null) {
                if (!service.handleMessage(msg)) {
                    super.handleMessage(msg);
                }
            }
        }
    }

    private static final int MSG_DEVICE_RESP = 1;

    private boolean handleMessage(Message msg) {
        boolean result = false;
        switch (msg.what) {
            case MSG_DEVICE_RESP:
                byte[] respData = null;
                byte[] payload;
                int type = mHostMsg.getType();
                int options = mHostMsg.getOptions();
                int length = mHostMsg.getLength() - HostMessage.HostMsgHeaderLength;
                if (length > 0) {
                    respData = new byte[length];
                    payload = mHostMsg.getPayload();
                    for (int i = 0; i < length; i++) {
                        respData[i] = payload[i];
                    }
                }

                final int N = mCallbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    try {
                        mCallbacks.getBroadcastItem(i).onDeviceRespond(
                                type,
                                options,
                                respData);
                    } catch (RemoteException e) {

                    }
                }
                mCallbacks.finishBroadcast();
                result = true;
                break;
        }

        return result;
    }

    private ServiceHandler mServiceHandler = new ServiceHandler(this);

    private void disconnect() {
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
            }
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }

        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }

        try {
            if (mBluetoothSocket != null) {
                mBluetoothSocket.close();
            }
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }

    private void disconnectExit() {
        connectionState = ConnectionState.DISCONNECTING;
        disconnect();
    }

    private void nap(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {

        }
    }

    private void start() {
        Thread thread = new Thread() {
            public void run() {
                boolean run = true;
                Looper.prepare();

                while (run) {
                    switch (connectionState) {
                    case ConnectionState.DISCONNECTED:
//                        Log.d(TAG, "state: disconnected");
                        break;
                    case ConnectionState.CONNECTING:
//                        Log.d(TAG, "state: connecting");
                        updateNotification(false);
                        connect(mCtx);
                        nap(2000);
                        break;
                    case ConnectionState.CONNECTED:
//                        Log.d(TAG, "state: connected");
                        readFromDevice();
                        break;

                    case ConnectionState.DISCONNECTING:
//                        Log.d(TAG, "state: disconnecting");
                        run = false;
                        break;
                    }
                }
            }
        };

        thread.start();
    }

    private byte[] dataBuffer = new byte[256];
    private void readFromDevice() {
        try {
//            Log.d(TAG, "before blocking read");
            int bytesRead = mInputStream.read(dataBuffer);
            mWakeLock.acquire(5000);

//            String str = "received: ";
//            for (int i = 0; i < bytesRead; i++) {
                //str+= Byte.toString(bytes[i]) + ", ";
//                str+= "0x" + Integer.toString((dataBuffer[i] & 0xff) + 0x100, 16).substring(1) + ", ";
//            }

//            Log.d(TAG, str);

            for (int i = 0; i < bytesRead; i++) {
                if (mHostMsg.parseNextByte(dataBuffer[i])) {
//                    Log.d(TAG, "Message parse done.");
//                    Log.d(TAG, "Type: " + Integer.toHexString(mHostMsg.getType()));
//                    Log.d(TAG, "Length: " + Integer.toHexString(mHostMsg.getLength()));
//                    Log.d(TAG, "Options: " + Integer.toHexString(mHostMsg.getOptions()));
//                    Log.d(TAG, "checkSum: " + Integer.toHexString(mHostMsg.getChecksum()));

                    mServiceHandler.sendEmptyMessage(MSG_DEVICE_RESP);
                }
            }

            // print received
//            String str = "received: " + bytesRead + " bytes";
//            Log.d(TAG, str);
        } catch (IOException e) {
            mWakeLock.acquire(5000);
            if (connectionState != ConnectionState.DISCONNECTING) {
                connectionState = ConnectionState.CONNECTING;
            }
        }
    }

    private void send(byte[] bytes) throws IOException, NullPointerException {
        if (bytes == null) {
            return;
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(bytes[0]);
        // skip first byte
        for (int i = 1; i < bytes.length; i++) {
            if (bytes[i] == com.airremote.android.service.Message.StartByteValue ||
                bytes[i] == com.airremote.android.service.Message.DataLinkEscapeValue) {
                byteArrayOutputStream.write(com.airremote.android.service.Message.DataLinkEscapeValue);
            }
            byteArrayOutputStream.write(bytes[i]);
        }

/*
        String str = "sending: ";
        byte[] b = byteArrayOutputStream.toByteArray();
        for (int i = 0; i < b.length; i ++) {
            //str+= Byte.toString(b[i]) + ", ";
            str+= "0x" + Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1) + ", ";
        }
        Log.d(TAG, str);
*/
        if (mOutputStream == null)
            throw new IOException("OutputStream is null");

        mOutputStream.write(byteArrayOutputStream.toByteArray());
        mOutputStream.flush();
    }

    private void processSendQueue() {
        if (isSending) {
            return;
        } else {
            isSending = true;
        }

        Thread thread = new Thread() {
            public void run() {
//                Log.d(TAG, "entering send queue");
                while (mSendQueue.size() > 0) {
                    try {
                        send(mSendQueue.get(0));
                        mSendQueue.remove(0);
                        Thread.sleep(Preferences.packetWait);
                    } catch (IOException e) {
                        mSendQueue.clear();
                    } catch (InterruptedException e) {

                    }
                }
                Log.d(TAG, "send queue finished");
                isSending = false;
            }
        };
        thread.start();
    }

    public void sendBytes(byte[] bytes) {
        mSendQueue.add(bytes);
        processSendQueue();
    }
}