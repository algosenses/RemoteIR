package com.airremote.android.service;

import android.util.Log;

public class Protocol {
    public static final String TAG = "Protocol";

    public static final class HostMessage {
        public static final int HostMsgBufferLength = 512;
        public static final int HostMsgHeaderLength = 5;
        public static final int HostMsgMaxPayloadLength = (HostMsgBufferLength - HostMsgHeaderLength);

        public static final int ST_START_BYTE = 1;
        public static final int ST_LENGTH = 2;
        public static final int ST_TYPE = 3;
        public static final int ST_OPTIONS = 4;
        public static final int ST_CHECK_SUM = 5;
        public static final int ST_PAYLOAD = 6;
        public static final int ST_DATA_ESC = 7;

        private int mParseState = ST_START_BYTE;
        private boolean mPreStateIsEsc = false;
        private int mPayloadIndex = 0;

        private byte startByte;
        private int Length;
        private int Type;
        private int Options;
        private int checkSum;
        private byte[] Payload;

        public HostMessage() {
            Length = 0;
            Payload = new byte[HostMsgMaxPayloadLength];
        }

        public int getLength() {
            return Length;
        }

        public int getType() {
            return Type;
        }

        public int getOptions() {
            return Options;
        }

        public int getChecksum() {
            return checkSum;
        }

        public byte[] getPayload() {
            return Payload;
        }

        public boolean parseNextByte(byte b) {
            if (mParseState == ST_START_BYTE) {
                if (b == Message.StartByteValue) {
                    startByte = b;
                    mParseState = ST_LENGTH;
                }
                return false;
            }

            if (b != Message.DataLinkEscapeValue || mPreStateIsEsc) {
                if (mPreStateIsEsc) {
                    mPreStateIsEsc = false;
                }

                if (mParseState == ST_LENGTH) {
                    Length = (b & 0xFF);
                    if (Length == 0) {     // error
                        mParseState = ST_START_BYTE;
                    } else {
                        mParseState = ST_TYPE;
                    }
                    return false;
                }

                if (mParseState == ST_TYPE) {
                    Type = (b & 0xFF);
                    mParseState = ST_OPTIONS;
                    return false;
                }

                if (mParseState == ST_OPTIONS) {
                    Options = (b & 0xFF);
                    mParseState = ST_CHECK_SUM;
                    return false;
                }

                if (mParseState == ST_CHECK_SUM) {
                    checkSum = (b & 0xFF);
                    if (Length > HostMsgHeaderLength) {
                        mPayloadIndex = 0;
                        mParseState = ST_PAYLOAD;
                        return false;
                    } else {
                        mParseState = ST_START_BYTE;
                        return true;
                    }
                }

                if (mParseState == ST_PAYLOAD) {
                    Payload[mPayloadIndex] = b;
                    mPayloadIndex++;
                    if (mPayloadIndex >= (Length - HostMsgHeaderLength) ||
                        mPayloadIndex > HostMsgMaxPayloadLength) {
                        mParseState = ST_START_BYTE;
                        return true;
                    }
                    return false;
                }
            } else {
                mPreStateIsEsc = true;
                return false;
            }

            return false;
        }
    }

    private AirRemoteService mService;

    Protocol(AirRemoteService service) {
        mService = service;
    }

    private byte calcCheckSum(byte[] bytes) {
        return 0x55;
    }

    public void getDeviceType() {
        byte[] bytes = new byte[5];

        bytes[0] = Message.StartByteValue;
        bytes[1] = (byte) (bytes.length);
        bytes[2] = Message.GetDeviceType.msg;
        bytes[3] = 0x00;
        bytes[4] = calcCheckSum(bytes);

        mService.sendBytes(bytes);
    }

    public void DeviceEnterIRReceiveMode() {
        byte[] bytes = new byte[5];

        bytes[0] = Message.StartByteValue;
        bytes[1] = (byte) (bytes.length);
        bytes[2] = Message.EnterIRReceiveMode.msg;
        bytes[3] = 0x00;
        bytes[4] = calcCheckSum(bytes);

        mService.sendBytes(bytes);
    }

    public void SendRawIRCode(int[] data) {
        if (data == null) {
            return;
        }
/*
        String str = "SendRawIRCode: ";
        for (int i = 0; i < data.length; i++) {
            str += Integer.toHexString(data[i]).toUpperCase() + " ";
    //          str += "0x" + Integer.toHexString(bursts[i]) + ", ";
        }

        Log.d(TAG, str);
*/
        int size = data.length;
        byte[] bytes = new byte[HostMessage.HostMsgHeaderLength + size * 2];

        bytes[0] = Message.StartByteValue;
        bytes[1] = (byte) (bytes.length);
        bytes[2] = Message.TransmitIRRawDataMsg.msg;
        bytes[3] = 0x00;
        bytes[4] = calcCheckSum(bytes);

        for (int i = 0; i < size; i++) {
            int out = data[i] * 26 / 8;
            bytes[HostMessage.HostMsgHeaderLength+2*i]   = (byte)(out & 0xFF);
            bytes[HostMessage.HostMsgHeaderLength+2*i+1] = (byte)((out >> 8) & 0xFF);
        }

        mService.sendBytes(bytes);
    }

    public void sendTestErrorData() {
        byte[] bytes = new byte[11];
        bytes[0] = Message.StartByteValue;
        bytes[1] = 6;
        bytes[2] = Message.TransmitIRRawDataMsg.msg;
        bytes[3] = 0x00;
        bytes[4] = 0x5A;
        bytes[5] = 0x10;
        bytes[6] = 0x5A;
        bytes[7] = 0x5A;
        bytes[8] = 0x10;
        bytes[9] = 0x10;
        bytes[10] = 0x33;

        mService.sendBytes(bytes);
    }

    public void sendTestData() {
        byte[] bytes = new byte[11];
        bytes[0] = Message.StartByteValue;
        bytes[1] = (byte) bytes.length;
        bytes[2] = Message.TransmitIRRawDataMsg.msg;
        bytes[3] = 0x00;
        bytes[4] = 0x5A;
        bytes[5] = 0x10;
        bytes[6] = 0x5A;
        bytes[7] = 0x5A;
        bytes[8] = 0x10;
        bytes[9] = 0x10;
        bytes[10] = 0x33;

        mService.sendBytes(bytes);
    }

    public void sendIRRawData(int[] rawData) {
        int size = rawData.length;
        byte[] bytes = new byte[HostMessage.HostMsgHeaderLength + size * 2];
        bytes[0] = Message.StartByteValue;
        bytes[1] = (byte) bytes.length;
        bytes[2] = Message.TransmitIRRawDataMsg.msg;
        bytes[3] = 0x00;
        bytes[4] = 0x55;

        for (int i = 0; i < size; i++) {
            int raw = rawData[i] / 16;
            bytes[HostMessage.HostMsgHeaderLength + 2 * i]     = (byte) (raw & 0xFF);
            bytes[HostMessage.HostMsgHeaderLength + 2 * i + 1] = (byte) ((raw & 0xFF00) >> 8);
        }

        mService.sendBytes(bytes);
    }
}