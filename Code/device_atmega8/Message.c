#include <string.h>
#include <util/delay.h>

#include "Message.h"
#include "BluetoothSpp.h"
#include "IRReceiver.h"

enum {
    ST_START_BYTE = 1,
    ST_LENGTH,
    ST_TYPE,
    ST_OPTIONS,
    ST_CHECK_SUM,
    ST_PAYLOAD,
    ST_DATA_ESC,
};

static unsigned char ParseState = ST_START_BYTE;
static unsigned char PreStateIsEsc = 0;
static unsigned char PayloadIndex = 0;

//static unsigned char Payload[HOST_MSG_MAX_PAYLOAD_LENGTH];
static tHostMsg BtSppRecvMsg;
static tHostMsg OutgoingMsg;

extern void delayMicroseconds(unsigned int us);
//==============================================================================
unsigned char CommonBufferPool[COMMON_BUFFER_POOL_SIZE];
//==============================================================================

void MsgParserInit(void)
{
    ParseState = ST_START_BYTE;
    PreStateIsEsc = 0;
    PayloadIndex = 0;

    BtSppRecvMsg.pPayload = CommonBufferPool;
}

unsigned char MsgParserInputByte(unsigned char ucData)
{
    if (ParseState == ST_START_BYTE) {
        if (ucData == MSG_START_BYTE_VALUE) {
            BtSppRecvMsg.startByte = ucData;
            ParseState = ST_LENGTH;
        }
        return 0;
    }

    if (ucData != DATA_LINK_ESCAPE_VALUE || PreStateIsEsc) {
        if (PreStateIsEsc) {
            PreStateIsEsc = 0;
        }

        if (ParseState == ST_LENGTH) {
            BtSppRecvMsg.Length = ucData;
            if (BtSppRecvMsg.Length == 0) {     // error
                ParseState = ST_START_BYTE;
            } else {
                ParseState = ST_TYPE;
            }
            return 0;
        }

        if (ParseState == ST_TYPE) {
            BtSppRecvMsg.Type = ucData;
            ParseState = ST_OPTIONS;
            return 0;
        }

        if (ParseState == ST_OPTIONS) {
            BtSppRecvMsg.Options = ucData;
            ParseState = ST_CHECK_SUM;
            return 0;
        }

        if (ParseState == ST_CHECK_SUM) {
            BtSppRecvMsg.checkSum = ucData;
            if (BtSppRecvMsg.Length > HOST_MSG_HEADER_LENGTH) {
                PayloadIndex = 0;
                ParseState = ST_PAYLOAD;
                return 0;
            } else {
                ParseState = ST_START_BYTE;
                return 1;
            }
        }

        if (ParseState == ST_PAYLOAD) {
            BtSppRecvMsg.pPayload[PayloadIndex] = ucData;
            PayloadIndex++;
            if (PayloadIndex >= (BtSppRecvMsg.Length - HOST_MSG_HEADER_LENGTH) ||
                PayloadIndex > HOST_MSG_MAX_PAYLOAD_LENGTH) {
                ParseState = ST_START_BYTE;
                return 1;
            }
            return 0;
        }
    } else {
        PreStateIsEsc = 1;
        return 0;
    }

    return 0;
}

tHostMsg *MsgParserGetBuffer(void)
{
    return (tHostMsg *)&BtSppRecvMsg;
}

static unsigned char calcChecksum(tHostMsg *pHostMsg)
{
    unsigned char checkSum = 0x0;
    unsigned char i;

    checkSum = pHostMsg->startByte ^ pHostMsg->Length ^ pHostMsg->Type ^ pHostMsg->Options;

    for (i = 0; i < (pHostMsg->Length - HOST_MSG_HEADER_LENGTH); i++) {
        checkSum ^= pHostMsg->pPayload[i];
    }

    return checkSum;
}

static void SendMessage(tHostMsg *pHostMsg)
{
    unsigned char i;
    unsigned char *pOutMsg = (unsigned char *)pHostMsg;

    BtSppWriteByte(pHostMsg->startByte);

    for (i = 1; i < HOST_MSG_HEADER_LENGTH; i++) {
        if (pOutMsg[i] == DATA_LINK_ESCAPE_VALUE || pOutMsg[i] == MSG_START_BYTE_VALUE) {
            BtSppWriteByte(DATA_LINK_ESCAPE_VALUE);
        }
        BtSppWriteByte(pOutMsg[i]);
    }

    for (i = 0; i < pHostMsg->Length - HOST_MSG_HEADER_LENGTH; i++) {
        if (pHostMsg->pPayload[i] == DATA_LINK_ESCAPE_VALUE || pHostMsg->pPayload[i] == MSG_START_BYTE_VALUE) {
            BtSppWriteByte(DATA_LINK_ESCAPE_VALUE);
//            delayMicroseconds(100);
        }
        BtSppWriteByte(pHostMsg->pPayload[i]);
//        delayMicroseconds(100);     /* transmission speed cannot be too fast */
    }

//    PrintString("SendMessage:");
//    PrintHex(pHostMsg->Type);
//    PrintHex(pHostMsg->Length);
//    PrintHex(pHostMsg->Options);
//
//    PrintCommonBufferPool(pHostMsg->Options);
}

void SendDeviceType(void)
{
    OutgoingMsg.startByte   = HOST_MSG_START_FLAG;
    OutgoingMsg.Length      = HOST_MSG_HEADER_LENGTH;
    OutgoingMsg.Type        = GetDeviceTypeResponse;
    OutgoingMsg.Options     = DEVICE_REVISION;
    OutgoingMsg.checkSum    = calcChecksum(&OutgoingMsg);

    SendMessage(&OutgoingMsg);
}

void SendDebugMsg(char *msg)
{
    int Length = strlen(msg);
    OutgoingMsg.startByte   = HOST_MSG_START_FLAG;
    OutgoingMsg.Length      = HOST_MSG_HEADER_LENGTH + Length;
    OutgoingMsg.Type        = PrintDebugMsg;
    OutgoingMsg.Options     = Length;
    OutgoingMsg.pPayload    = (unsigned char *)msg;
    OutgoingMsg.checkSum    = calcChecksum(&OutgoingMsg);

    SendMessage(&OutgoingMsg);
}

void UploadIRRawData(unsigned char Length)
{
    OutgoingMsg.startByte   = HOST_MSG_START_FLAG;
    OutgoingMsg.Length      = HOST_MSG_HEADER_LENGTH + Length;
    OutgoingMsg.Type        = UploadIRRawDataMsg;
    OutgoingMsg.Options     = Length;
    OutgoingMsg.pPayload    = CommonBufferPool;
    OutgoingMsg.checkSum    = calcChecksum(&OutgoingMsg);

    SendMessage(&OutgoingMsg);
}

#if 1
void PrintCommonBufferPool(unsigned int length)
{
    unsigned char i;
    uint16_t *rawData = (uint16_t *)CommonBufferPool;

    BtSppPrint("Buffer Pool:");
    for (i = 0; i < length / 2; i++) {
        BtSppPrintHex(rawData[i]);
    }

    BtSppPrint("\r\n");
}
#endif
