#ifndef IR_RECEIVER_H
#define IR_RECEIVER_H

#define MIN_BURST_NUM       (4)

void            IRRecvInit(void);
void            IRRecvStart(void);
void            IRRecvStop(void);
unsigned char   IRRecvCaptureFrame(void);
void            IRRecvResume(void);
unsigned char   IRRecvRawDataSize(void);

#endif /* IR_RECEIVER_H */
