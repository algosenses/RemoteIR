#include <avr/io.h>
#include <avr/interrupt.h>

#include "Message.h"
#include "IRTransmitter.h"

/* Delay for the given number of microseconds.  Assumes a 8 or 16 MHz clock. */
void delayMicroseconds(unsigned long us)
{
	// calling avrlib's delay_us() function with low values (e.g. 1 or
	// 2 microseconds) gives delays longer than desired.
	//delay_us(us);
#if F_CPU >= 20000000L
	// for the 20 MHz clock on rare Arduino boards

	// for a one-microsecond delay, simply wait 2 cycle and return. The overhead
	// of the function call yields a delay of exactly a one microsecond.
	__asm__ __volatile__ (
		"nop" "\n\t"
		"nop"); //just waiting 2 cycle
	if (--us == 0)
		return;

	// the following loop takes a 1/5 of a microsecond (4 cycles)
	// per iteration, so execute it five times for each microsecond of
	// delay requested.
	us = (us<<2) + us; // x5 us

	// account for the time taken in the preceeding commands.
	us -= 2;

#elif F_CPU >= 16000000L
	// for the 16 MHz clock on most Arduino boards

	// for a one-microsecond delay, simply return.  the overhead
	// of the function call yields a delay of approximately 1 1/8 us.
	if (--us == 0)
		return;

	// the following loop takes a quarter of a microsecond (4 cycles)
	// per iteration, so execute it four times for each microsecond of
	// delay requested.
	us <<= 2;

	// account for the time taken in the preceeding commands.
	us -= 2;
#else
	// for the 8 MHz internal clock on the ATmega168

	// for a one- or two-microsecond delay, simply return.  the overhead of
	// the function calls takes more than two microseconds.  can't just
	// subtract two, since us is unsigned; we'd overflow.
	if (--us == 0)
		return;
	if (--us == 0)
		return;

	// the following loop takes half of a microsecond (4 cycles)
	// per iteration, so execute it twice for each microsecond of
	// delay requested.
	us <<= 1;

	// partially compensate for the time taken by the preceeding commands.
	// we can't subtract any more than this or we'd overflow w/ small delays.
	us--;
#endif

	// busy wait
	__asm__ __volatile__ (
		"1: sbiw %0,1" "\n\t" // 2 cycles
		"brne 1b" : "=w" (us) : "0" (us) // 2 cycles
	);
}


static void mark(unsigned int time) {
    // Sends an IR mark for the specified number of microseconds.
    // The mark output is modulated at the PWM frequency.
    TIMER_ENABLE_PWM; // Enable pin 3 PWM output
    delayMicroseconds(time);
}

/* Leave pin off for time (given in microseconds) */
static void space(unsigned int time) {
    // Sends an IR space for the specified number of microseconds.
    // A space is no output, so the PWM output is disabled.
    TIMER_DISABLE_PWM; // Disable pin 3 PWM output
    delayMicroseconds(time);
}

static void enableIROut(int khz) {
    // Enables IR output.  The khz value controls the modulation frequency in kilohertz.
    // The IR output will be on pin 3 (OC2B).
    // This routine is designed for 36-40KHz; if you use it for other values, it's up to you
    // to make sure it gives reasonable results.  (Watch out for overflow / underflow / rounding.)
    // TIMER2 is used in phase-correct PWM mode, with OCR2A controlling the frequency and OCR2B
    // controlling the duty cycle.
    // There is no prescaling, so the output frequency is 16MHz / (2 * OCR2A)
    // To turn the output on and off, we leave the PWM running, but connect and disconnect the output pin.
    // A few hours staring at the ATmega documentation and this will all make sense.
    // See my Secrets of Arduino PWM at http://arcfn.com/2009/07/secrets-of-arduino-pwm.html for details.


    // Disable the Timer2 Interrupt (which is used for receiving IR)
    TIMER_DISABLE_INTR; //Timer2 Overflow Interrupt

    DDRB |= (0x1 << PB1);
    PORTB &= ~(0x1 << PB1);     // When not sending PWM, we want it low

    // COM2A = 00: disconnect OC2A
    // COM2B = 00: disconnect OC2B; to send signal set to 10: OC2B non-inverted
    // WGM2 = 101: phase-correct PWM with OCRA as top
    // CS2 = 000: no prescaling
    // The top value for the timer.  The modulation frequency will be SYSCLOCK / 2 / OCR2A.
    TIMER_CONFIG_KHZ(khz);
}

void IRTransSendNEC(unsigned long data, int nbits)
{
    enableIROut(38);
    mark(NEC_HDR_MARK);
    space(NEC_HDR_SPACE);
    for (int i = 0; i < nbits; i++) {
        if (data & TOPBIT) {
            mark(NEC_BIT_MARK);
            space(NEC_ONE_SPACE);
        } else {
            mark(NEC_BIT_MARK);
            space(NEC_ZERO_SPACE);
        }
        data <<= 1;
    }
    mark(NEC_BIT_MARK);
    space(0);
}

#if 0
void IRSendNECTest(void)
{
    BitBuf BitBuffer;
    unsigned char rawData[] = { 0x38, 0x86, 0x3B, 0xD2, 0x2D, 0xC0 };
    unsigned char bitCount = 42;

    bitbufInit(&BitBuffer, rawData, 6);

    enableIROut(38);
    mark(NEC_HDR_MARK);
    space(NEC_HDR_SPACE);

    while (bitCount--) {
        if (bitbufGet(&BitBuffer)) {
            mark(NEC_BIT_MARK);
            space(NEC_ONE_SPACE);
        } else {
            mark(NEC_BIT_MARK);
            space(NEC_ZERO_SPACE);
        }
    }
    mark(NEC_BIT_MARK);
    space(0);
}
#else
void IRSendNECTest(void)
{
    IRTransSendSamsung(0x38863BD2, 0x2DC00000, 42);
}
#endif

// Added Samsung code - a 42 bit protocol
// First 32 bit in data1
// Remaining bits left-adjusted in data2
// Code is exact copy of NEC with extra loop for data2
void IRTransSendSamsung(unsigned long data1, unsigned long data2, int nbits)
{
    enableIROut(38);
    mark(NEC_HDR_MARK);
    space(NEC_HDR_SPACE);

    for (int i = 0; i < 32; i++) {
        if (data1 & TOPBIT) {
            mark(NEC_BIT_MARK);
            space(NEC_ONE_SPACE);
        } else {
            mark(NEC_BIT_MARK);
            space(NEC_ZERO_SPACE);
        }
        data1 <<= 1;
    }

    //last bits
    nbits = nbits - 32;
    for (int i = 0; i < nbits; i++) {
        if (data2 & TOPBIT) {
            mark(NEC_BIT_MARK);
            space(NEC_ONE_SPACE);
        } else {
            mark(NEC_BIT_MARK);
            space(NEC_ZERO_SPACE);
        }
        data2 <<= 1;
    }
    mark(NEC_BIT_MARK);
    space(0);
}

void IRTransSendRawData(uint16_t *data, unsigned int len, int hz)
{
    unsigned int i;

    enableIROut(hz);
    for (i = 0; i < len; i++) {
        if (i & 1) {
            space(data[i] << 3);     // 8us unit, so multiply by 8
        } else {
            mark(data[i] << 3);
        }
    }
}
