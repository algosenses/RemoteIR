package com.airremote.android.service;

import com.airremote.android.service.IAirRemoteServiceCallback;

interface IAirRemoteService {
    void registerCallback(IAirRemoteServiceCallback cb);
    void unregisterCallback(IAirRemoteServiceCallback cb);

    void getDeviceType();
    void DeviceEnterIRReceiveMode();
    void SendRawIRCode(in int[] data);
}
