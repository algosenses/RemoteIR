#include <string.h>
#include <util/delay.h>

#include "BluetoothSpp.h"
#include "Message.h"
#include "IRReceiver.h"
#include "IRTransmitter.h"

static unsigned char SppByteReceived;
static tHostMsg *SppMsgReceived;

static volatile unsigned char DeviceMode;
static volatile unsigned char MsgParserIsRunning;

static unsigned char changeDeviceMode(tHostMsg *pHostMsg)
{
    switch (pHostMsg->Type) {
    case GetDeviceType:
        DeviceMode = MODE_DEVICE_TYPE_RESP;
        break;

    case ReadBatteryVoltage:
        DeviceMode = MODE_BATTERY_VOLTAGE_RESP;
        break;

    case EnterIRReceiveMode:
        DeviceMode = MODE_IR_RECEIVE;
        IRRecvInit();
        IRRecvStart();
        break;

    case EnterIRTransmitMode:
        IRRecvStop();
        DeviceMode = MODE_IR_TRANSMIT;
        break;

    case TransmitIRRawDataMsg:
        DeviceMode = MODE_IR_TRANSMIT_RAW_DATA;
        break;

    default:
        break;
    }

    return 0;
}

#if 0
/*
static uint16_t testRawData[] = {
0x0152*26/8, 0x00AA*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x0015*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x003F*26/8, 0x0014*26/8, 0x0364*26/8, 0x0152*26/8, 0x00AA*26/8, 0x0014*26/8, 0x4EC4*26/8
};
*/
static uint16_t testRawData[] = {
0x44A, 0x228, 0x41, 0x44, 0x41, 0x44, 0x41, 0xCC, 0x41, 0xCC, 0x41, 0xCC, 0x41, 0x44, 0x41, 0x44, 0x41, 0x44, 0x41, 0xCC, 0x41, 0x44, 0x41, 0x44, 0x41, 0x44, 0x41, 0x44, 0x41, 0xCC, 0x41, 0xCC, 0x41, 0x44, 0x41, 0x44, 0x41, 0x44, 0x41, 0xCC, 0x41, 0xCC, 0x41, 0xCC, 0x41, 0x44, 0x41, 0xCC, 0x41, 0xCC, 0x41, 0xCC, 0x41, 0xCC, 0x41, 0x44, 0x41, 0x44, 0x41, 0x44, 0x41, 0x44, 0x41, 0xCC, 0x41, 0x44, 0x41, 0x44, 0x41, 0x44, 0x41, 0xCC, 0x41, 0xCC, 0x41, 0xCC, 0x41, 0xCC, 0x41, 0x44, 0x41, 0xCC, 0x41, 0xCC, 0x41, 0xCC, 0x41, 0xB05, 0x44A, 0x228, 0x41, 0xFFFD
};
#endif


#if 1
int main(void)
{
    BtSppInit();
    MsgParserInit();

    DeviceMode = MODE_IDLE;
    MsgParserIsRunning = 1;

//    BtSppPrint("AT");
//    BtSppPrint("AT+NAMEAirRemote");
//    BtSppPrint("AT+BAUD7");

    while (1) {
        if (MsgParserIsRunning) {
            while (BtSppGetByte(&SppByteReceived)) {
                if (MsgParserInputByte(SppByteReceived)) {        // receive a message from smartphone
                    SppMsgReceived = MsgParserGetBuffer();

//                    BtSppPrint("Host Message: Type: ");
//                    BtSppPrintHex(SppMsgReceived->Type);
//                    BtSppPrint("Length: ");
//                    BtSppPrintHex(SppMsgReceived->Length);
//                    BtSppPrint("\r\n");

                    MsgParserIsRunning = 0;

                    changeDeviceMode(SppMsgReceived);
                }

                if (!MsgParserIsRunning) {
                    break;
                }
            }
        }

        switch (DeviceMode) {
            case MODE_DEVICE_TYPE_RESP:
                SendDeviceType();
                DeviceMode = MODE_IDLE;
                break;

            case MODE_IR_RECEIVE:
                if (IRRecvCaptureFrame()) {
                    unsigned char size = IRRecvRawDataSize();
//                    BtSppPrint("IRRawDataSize: ");
//                    PrintHex(size / 2);
//                    BtSppPrint("\r\n");
                    UploadIRRawData(size);
                    IRRecvResume();
                }
                break;

            case MODE_IR_TRANSMIT:
                DeviceMode = MODE_IDLE;
                break;

            case MODE_IR_TRANSMIT_RAW_DATA: {
                unsigned int rawDataLength = SppMsgReceived->Length - HOST_MSG_HEADER_LENGTH;
//                PrintCommonBufferPool(rawDataLength);
                IRTransSendRawData((uint16_t *)CommonBufferPool, rawDataLength / 2, 38);
//                IRTransSendRawData(testRawData, sizeof(testRawData) / sizeof(testRawData[0]), 38);
                DeviceMode = MODE_IDLE;
                break;
            }

            default:
                DeviceMode = MODE_IDLE;
                break;
        }
        // after execute all actions, resume message parser
        MsgParserIsRunning = 1;
    }

    return 0;
}
#else
int main(void)
{
    BtSppInit();
    IRRecvInit();

    IRRecvStart();

    while (1) {
//        BtSppPrint("Hello World!\r\n");
//        _delay_ms(1000);
//    BtSppPrint("AT");
//    _delay_ms(3000);
//    BtSppPrint("AT+NAMEAirRemote");
//    _delay_ms(3000);
//    BtSppPrint("AT+BAUD7");     // 57600
//    _delay_ms(3000);
#if 1
        if (IRRecvCaptureFrame()) {
            unsigned char size = IRRecvRawDataSize();
            BtSppPrint("IRRawDataSize: ");
            BtSppPrintHex(size / 2);
            BtSppPrint("\r\n");
            PrintCommonBufferPool(size);
            IRRecvResume();
        }
#endif
    }
}
#endif
