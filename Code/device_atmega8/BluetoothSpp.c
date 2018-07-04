#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>

#include "BluetoothSpp.h"
#include "LightweightRingBuff.h"

#ifndef cbi
#define cbi(sfr, bit) (_SFR_BYTE(sfr) &= ~_BV(bit))
#endif

#ifndef sbi
#define sbi(sfr, bit) (_SFR_BYTE(sfr) |= _BV(bit))
#endif

/*
 * define CPU frequency in Mhz here if not defined in Makefile
 */
#ifndef F_CPU
    #define F_CPU 8000000UL
#endif

/*
 * UART Baudrate
 */
#ifndef BAUDRATE
    #define BAUDRATE 57600
#endif

#define UART_BAUDRATE_DOUBLE_SPEED 0

#define UART_BAUD_RATE_LOW          UBRRL
#define UART_STATUS_REG             UCSRA
#define UART_CONTROL_REG            UCSRB
#define UART_ENABLE_TRANSMITTER     TXEN
#define UART_ENABLE_RECEIVER        RXEN
#define UART_TRANSMIT_COMPLETE      TXC
#define UART_RECEIVE_COMPLETE       RXC
#define UART_DATA_REG               UDR
#define UART_DOUBLE_SPEED           U2X
#define UART_FRAME_ERROR            FE
#define UART_DATA_OVER_RUN          DOR
#define UART_PARITY_ERROR           PE
#define UART_MULTI_PROC_COMM_MODE   MPCM
#define UART_DATA_REG_EMPTY_INT_EN  UDRIE
#define UART_RX_COMPLETE_INT_EN     RXCIE
#define UART_TX_COMPLETE_INT_EN     TXCIE


#define UART_BAUD_SELECT(baudRate, xtalCpu) (((float)(xtalCpu))/(((float)(baudRate))*8.0)-1.0+0.5)

static RingBuff_t SppRxBuffer;
static RingBuff_t SppTxBuffer;

ISR(USART_RXC_vect)
{
    unsigned char c = UART_DATA_REG;
    if (bit_is_clear(UART_STATUS_REG, UART_PARITY_ERROR)) {
        if (!(RingBuffer_IsFull(&SppRxBuffer))) {
            RingBuffer_Insert(&SppRxBuffer, c);
        }
    }
}

ISR(USART_UDRE_vect)
{
    if (RingBuffer_IsEmpty(&SppTxBuffer)) {
        cbi(UART_CONTROL_REG, UART_DATA_REG_EMPTY_INT_EN);
    } else {
        unsigned char c = RingBuffer_Remove(&SppTxBuffer);
        UART_DATA_REG = c;
    }
}

unsigned char BtSppWriteByte(unsigned char c)
{
    while (1) {
        if (!(RingBuffer_IsFull(&SppTxBuffer))) {
            RingBuffer_Insert(&SppTxBuffer, c);
            sbi(UART_CONTROL_REG, UART_DATA_REG_EMPTY_INT_EN);
            // clear the TXC bit -- "can be cleared by writing a one to its bit location"
            sbi(UART_STATUS_REG, UART_TRANSMIT_COMPLETE);
            return 1;
        } else {
            _delay_ms(5);
        }
    }

    return 0;
}

static void SerialSetup(void)
{
    cli();  //disable interrupts while initializing the USART

    /* set transfer complete flag (TXC0 = 1).
    *  clear Frame Error flag (FE0 = 0).
    *  clear Data overrun flag (DOR0 = 0).
    *  clear Parity overrun flag (UPE0 = 0).
    *  Enable doubling of USART transmission speed (U2X0 = 1).
    *  Disable Multi-Processor Communication Mode-- whatever that is. (MCPM0 = 0)  */
    sbi(UART_STATUS_REG, UART_TRANSMIT_COMPLETE);
    cbi(UART_STATUS_REG, UART_FRAME_ERROR);
    cbi(UART_STATUS_REG, UART_DATA_OVER_RUN);
    cbi(UART_STATUS_REG, UART_PARITY_ERROR);
    sbi(UART_STATUS_REG, UART_DOUBLE_SPEED); //
    cbi(UART_STATUS_REG, UART_MULTI_PROC_COMM_MODE);

    /* Enable Receive Interrupt (RXCIE0 = 1).
    *  Disable Tranmission Interrupt (TXCIE = 0).
    *  Disable Data Register Empty interrupt (UDRIE0 = 0).
    *  Enable reception (RXEN0 = 1).
    *  Enable transmission (TXEN0 = 1).
    *  Set 8-bit character mode (UCSZ00, UCSZ01, and UCSZ02 together control this,
    *  But UCSZ00, UCSZ01 are in Register UCSR0C). */
    sbi(UART_CONTROL_REG, UART_RX_COMPLETE_INT_EN);
    cbi(UART_CONTROL_REG, UART_TX_COMPLETE_INT_EN);
    cbi(UART_CONTROL_REG, UART_DATA_REG_EMPTY_INT_EN);
    sbi(UART_CONTROL_REG, UART_ENABLE_RECEIVER);
    sbi(UART_CONTROL_REG, UART_ENABLE_TRANSMITTER);
//    cbi(UART_CONTROL_REG, UCSZ02);

    /* USART Mode select -- UMSEL00 = 0 and UMSEL01 = 0 for asynchronous mode.
    *  disable parity mode -- UPM00 = 0 and UPM01 = 0.
    *  Set USBS = 1 to configure to 2 stop bits per DMX standard.  The USART receiver ignores this
    *  setting anyway, and will only set a frame error flag if the first stop bit is 0.
    *  But, we have to set it to something.
    *  Finish configuring for 8 data bits by setting UCSZ00 and UCSZ01 to 1.
    *  Set clock parity to 0 for asynchronous mode (UCPOL0 = 0). */
//    cbi(UCSR0C, UMSEL00);
//    cbi(UCSR0C, UMSEL01);
//    cbi(UCSR0C, UPM00);
//    cbi(UCSR0C, UPM01);
//    sbi(UCSR0C, USBS0);
//    sbi(UCSR0C, UCSZ00);
//    sbi(UCSR0C, UCSZ01);
//    cbi(UCSR0C, UCPOL0);


//    UBRR0L = baudprescale; // Load lower 8-bits of the baud rate value into the low byte of the UBRR register
//    UBRR0H = (baudprescale >> 8); // Load upper 8-bits of the baud rate value into the high byte of the UBRR register
    // ***note to self: at some point add a command to write UBRR0H high bits to 0 per datasheet for "future compatibility"

//    UART_BAUD_RATE_LOW = UART_BAUD_SELECT(BAUDRATE, F_CPU);

    /* initialize UART(s) depending on CPU defined */
    /* m8 */
//    UBRRH = (((F_CPU/BAUDRATE)/16)-1)>>8; 	// set baud rate
//    UBRRL = (((F_CPU/BAUDRATE)/16)-1);

    /* 57.6kbps with U2X=1 */
    /* See datasheet page 155, Error=2.1% */
    UBRRH = 0x00;
    UBRRL = 0x10;

    UCSRB = (1 << RXEN) | (1 << TXEN) | (1 << RXCIE);       // enable Rx & Tx
    UCSRC = (1 << URSEL) | (1 << UCSZ1) | (1 << UCSZ0);     // config USART; 8N1


    asm volatile ("nop");			// wait until port has changed

    sei(); // Enable the Global Interrupt Enable flag so that interrupts can be processed
}

void BtSppInit(void)
{
    SerialSetup();

    RingBuffer_InitBuffer(&SppRxBuffer);
	RingBuffer_InitBuffer(&SppTxBuffer);
}

unsigned char BtSppGetByte(unsigned char *Out)
{
    if (!RingBuffer_IsEmpty(&SppRxBuffer)) {
        *Out = RingBuffer_Remove(&SppRxBuffer);
        return 1;
    } else {
        return 0;
    }
}

//************************************************************************
void BtSppPrint(char *Str)
{
    int i = 0;
    while (Str[i] != '\0') {
        BtSppWriteByte(Str[i++]);
    }
}

static signed char ConversionString[6];

/*! convert a 16 bit value into a hexadecimal string */
#if 1
static void ToHexString(unsigned int Value, signed char * pString)
{
  unsigned char parts[4];
  unsigned char first = 0;
  unsigned char index = 0;
  signed char i;

  parts[3] = (0xF000 & Value) >> 12;
  parts[2] = (0x0F00 & Value) >> 8;
  parts[1] = (0x00F0 & Value) >> 4;
  parts[0] = (0x000F & Value);


  for ( i = 3; i > -1; i-- )
  {
    if ( parts[i] > 0 || first || i == 0 )
    {
      if ( parts[i] > 9 )
      {
        pString[index++] = parts[i] + 'A' - 10;
      }
      else
      {
        pString[index++] = parts[i] + '0';
      }
      first = 1;
    }

  }

  pString[index] = 0;

}
#else
static void ToHexString(unsigned int Value, signed char *pString)
{
    unsigned char parts[4];
    unsigned index = 0;

    signed char i;

    parts[3] = (0xF000 & Value) >> 12;
    parts[2] = (0x0F00 & Value) >> 8;
    parts[1] = (0x00F0 & Value) >> 4;
    parts[0] = (0x000F & Value);

    for (i = 3; i > -1; i--) {
        if (parts[i] > 9) {
            pString[index++] = parts[i] + 'A' - 10;
        } else {
            pString[index++] = parts[i] + '0';
        }
    }

    pString[index] = '\0';
}
#endif

void BtSppPrintHex(unsigned int Value)
{
    BtSppPrint("0x");
    ToHexString(Value, ConversionString);
    BtSppPrint(ConversionString);
    BtSppPrint(" ");
}
