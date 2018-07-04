package com.airremote.android.service;

public enum Message {
    InvalidMessage(0x00),
    PrintDebugMsg(0x01),
    GetDeviceType(0x02),
    GetDeviceTypeResponse(0x03),
    ReadBatteryVoltage(0x04),
    ReadBatteryVoltageResponse(0x05),
    EnterIRReceiveMode(0x06),
    EnterIRTransmitMode(0x07),
    UploadIRRawDataMsg(0x08),
    TransmitIRRawDataMsg(0x09),
    TransmitIRCode(0x0A);

    static public final byte StartByteValue = 0x5A;
    static public final byte DataLinkEscapeValue = 0x10;

    public byte msg;

    Message(int msg) {
        this.msg = (byte)msg;
    }
}