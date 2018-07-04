#ifndef MESSAGE_H
#define MESSAGE_H

#include <stdint.h>

#define DEVICE_REVISION         (0x01)

#define HOST_MSG_BUFFER_LENGTH  ( 16 )
#define HOST_MSG_HEADER_LENGTH  ( 5 )
#define HOST_MSG_START_FLAG     ( 0x5A )

#define MSG_START_BYTE_VALUE        HOST_MSG_START_FLAG
#define DATA_LINK_ESCAPE_VALUE      (0x10)

/* 11 */
//#define HOST_MSG_MAX_PAYLOAD_LENGTH     (HOST_MSG_BUFFER_LENGTH - HOST_MSG_HEADER_LENGTH)

#define MAX_IR_BURST_PAIR_SIZE      (100)
#define COMMON_BUFFER_POOL_SIZE     (MAX_IR_BURST_PAIR_SIZE * 2 * sizeof(uint16_t))     // in bytes

#define HOST_MSG_MAX_PAYLOAD_LENGTH (COMMON_BUFFER_POOL_SIZE)

extern unsigned char CommonBufferPool[COMMON_BUFFER_POOL_SIZE];

typedef struct
{
    unsigned char  startByte;
    unsigned char  Length;
    unsigned char  Type;
    unsigned char  Options;
    unsigned char  checkSum;
    unsigned char* pPayload;
} tHostMsg;

enum {
    MODE_IDLE,
    MODE_DEVICE_TYPE_RESP,
    MODE_BATTERY_VOLTAGE_RESP,
    MODE_IR_RECEIVE,
    MODE_IR_TRANSMIT,
    MODE_IR_TRANSMIT_RAW_DATA,
};

typedef enum
{
    InvalidMessage              = 0x00,
    PrintDebugMsg               = 0x01,
    GetDeviceType               = 0x02,
    GetDeviceTypeResponse       = 0x03,
    ReadBatteryVoltage          = 0x04,
    ReadBatteryVoltageResponse  = 0x05,
    EnterIRReceiveMode          = 0x06,
    EnterIRTransmitMode         = 0x07,
    UploadIRRawDataMsg          = 0x08,     /* upload IR raw data from device to smartphone */
    TransmitIRRawDataMsg        = 0x09,     /* download IR raw data from smartphone to device */
    TransmitIRCode              = 0x0A,
} eMessageType;

void            MsgParserInit(void);
unsigned char   MsgParserInputByte(unsigned char ucData);
tHostMsg*       MsgParserGetBuffer(void);

void SendDebugMsg(char *msg);
void SendDeviceType(void);
void UploadIRRawData(unsigned char Length);
void PrintCommonBufferPool(unsigned int length);

#endif /* MESSAGE_H */
