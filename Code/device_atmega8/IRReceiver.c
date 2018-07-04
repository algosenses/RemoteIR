#include <stdint.h>

#include <avr/io.h>
#include <avr/interrupt.h>

#include "Message.h"
#include "IRReceiver.h"
#include "BluetoothSpp.h"

#define IR_RAW_SIZE         (COMMON_BUFFER_POOL_SIZE / 2)

#define CaptureEnable()     do { TIMSK |= ((1 << TICIE1) | (1 << TOIE1)); } while (0)
#define CaptureDisable()    do { TIMSK &= ~((1 << TICIE1) | (1 << TOIE1)); } while (0)

/* falling edge */
#define SetIntFallingEdge()     do { TCCR1B &= ~(1 << ICES1); } while (0)
/* rising edge */
#define SetIntRisingEdge()      do { TCCR1B |= (1 << ICES1); } while (0)

#define TIMER_OVER_FLOW_START_COUNT       (49536)
// start from (2^16 - OVER_FLOW_TICKS_COUNT = 65536 - 16000 = 49536 = 0xC180)
#define SET_TCNT_VALUE()      do { TCNT1H = 0xC1; TCNT1L = 0x80; } while (0)

static volatile unsigned long CaptureInterval = 0;
static volatile unsigned char CaptureDone = 0;
static volatile unsigned char CaptureFirstEdge = 0;

static unsigned int *IRRawDataBuffer;
static uint8_t  IRRawDataPtr;

// setup timer1
static void CaptureIntInit(void)
{
    // normal mode
    TCCR1A = 0;
    /* (00000011) Falling edge trigger, Timer = F_CPU/64 = 8000000/64 = 125KHz */
    /* t = 64 / 8 = 8us */
    /* T = 64 / 8 * (OVER_FLOW_TICKS_COUNT)     (OVER_FLOW_TICKS_COUNT = 16000) */
    /* T = 64 / 8 * 16000 = 128000us (timer1's overflow interrupt fired every 128ms) */
    /* because the width of all known _IR_burst_ is shorter than 128ms, 128ms may been a large enough value to
     * determine a IR signal is completely done or not.
     * NOTICE: a IR signal contains may bursts.
     * If a bursts is detected, timer1 will be reset and start over again. */
    TCCR1B = (1 << CS11) | (1 << CS10);
    // (00100001) Input capture and overflow interupts enabled
//    TIMSK = (1 << TICIE1) | (1 << TOIE1);
    SET_TCNT_VALUE();

    ICR1H = 0x00;
    ICR1L = 0x00;
    OCR1AH = 0x00;
    OCR1AL = 0x00;

    OCR1BH = 0x00;
    OCR1BL = 0x00;
}

/*
 * ir_disable : disable ir reception
 */
void IRRecvStop(void)
{
    CaptureDisable();
}

/*
 * ir_interrupt : this function must be called when the capture interrupt
 *                occurs.
 */
/* PULSE DETECTED! */
ISR(TIMER1_CAPT_vect)
{
    CaptureInterval = ICR1;         // save duration of last revolution
//    TCNT1 = 0;                    // restart timer for next revolution
    SET_TCNT_VALUE();

    if (!(TCCR1B & (1 << ICES1))) {     // looking for falling edge
        if (bit_is_clear(PINB, 0)) {    // falling edge
        	SetIntRisingEdge();
        }
    } else {
        if (bit_is_set(PINB, 0)) {
            SetIntFallingEdge();
        }
    }

    if (CaptureFirstEdge) {
	    /* This is the first edge. */
	    IRRawDataPtr = 0;
	    CaptureFirstEdge = 0;
	    return;
	}

	IRRawDataBuffer[IRRawDataPtr] = CaptureInterval - TIMER_OVER_FLOW_START_COUNT;
	IRRawDataPtr++;
	if (IRRawDataPtr == IR_RAW_SIZE - 1) {
		IRRawDataPtr = 0;
	}
}

// counter overflow/timeout (T=128ms)
ISR(TIMER1_OVF_vect)
{
    SET_TCNT_VALUE();

    IRRawDataBuffer[IRRawDataPtr++] = 0xFFFF;

    if (IRRawDataPtr <= MIN_BURST_NUM) {        // treat as interference
        IRRawDataPtr = 0;
        CaptureFirstEdge = 1;
        CaptureDone = 0;
        return;
    }

    IRRecvStop();

    CaptureFirstEdge = 1;
    CaptureDone = 1;

    /* reset capture edge */
    SetIntFallingEdge();
}


/*
 * IRRecvInit : this function must be called to initialize the library.
 */
void IRRecvInit(void)
{
    IRRawDataBuffer = (uint16_t *)CommonBufferPool;

    cli();

    CaptureIntInit();

    sei();

    CaptureFirstEdge = 1;
    CaptureDone = 0;
}

/* enable ir reception */
void IRRecvStart(void)
{
    IRRawDataPtr = 0;
    CaptureFirstEdge = 1;
    CaptureDone = 0;
    CaptureEnable();
}

unsigned char IRRecvCaptureFrame(void)
{
    return CaptureDone;
}

void IRRecvResume(void)
{
    IRRawDataPtr = 0;
    CaptureFirstEdge = 1;
    CaptureDone = 0;
    CaptureEnable();
}

unsigned char IRRecvRawDataSize(void)
{
    return IRRawDataPtr * 2;
}
