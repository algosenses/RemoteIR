package com.airremote.android.service;

oneway interface IAirRemoteServiceCallback {
    void onDeviceRespond(int type, int options, out byte[] data);
}