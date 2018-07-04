#ifndef BLUETOOTH_SPP_H
#define BLUETOOTH_SPP_H

void BtSppInit(void);
unsigned char BtSppGetByte(unsigned char *Out);
unsigned char BtSppWriteByte(unsigned char c);
void BtSppPrint(char *Str);
void BtSppPrintHex(unsigned int Value);

#endif /* BLUETOOTH_SPP_H */
