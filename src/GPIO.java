import java.util.BitSet;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class GPIO extends Thread {
	private final GpioController gpio;
	private GpioPinDigitalOutput out;
	private GpioPinDigitalInput in;
	private BitSet send, receive, sendLength, receiveLength;
	boolean inPinState;
	int receiveWaitMillis;
	
	public GPIO(int integer) {
		receiveWaitMillis = integer;
		gpio = GpioFactory.getInstance();
		out = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, PinState.LOW);
		out.setShutdownOptions(true, PinState.LOW);
		in = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, PinPullResistance.PULL_DOWN);
		in.setShutdownOptions(true);
		send = new BitSet();
		receive = new BitSet();
		sendLength = new BitSet(32);
		receiveLength = new BitSet(32);
		start();
	}
	
	public void sendByteArray(byte[] bytesToSend) {
		send = BitSet.valueOf(bytesToSend);
//		send.set(send.length() + 1);
		System.out.println("Text wurde vom BitArray zum BitSet konvertiert. Ausgabe des BitSet:\n");
		for (int i = 0; i < send.length(); i++) {
			System.out.println(send.get(i));
		}
		// Initializing the data transfer by setting voltage on output pin to high which wakes up the receiving RasPi.
		out.high();
		// Now Waiting for 5ms so the receiver starts listening on the input pin
		try {
			Thread.sleep(receiveWaitMillis);
			System.out.println("\n50ms Warten, nachdem Voltage auf High gesetzt wurde, um Transfer zu initialisieren.");
		}
		catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		// Now sending the amount of data in bits, so the receiver stops at the right time.
		sendLength = BitSet.valueOf(new long[] { send.length() });
//		sendLength.set(32);
		System.out.println("Sende Länge in Bits des zu sendenden Textes: " + send.length());
		for (int i = 0; i < 32; i++) {
			if (sendLength.get(i)) {
				out.high();
				System.out.println("sendlength Bit " + i + " ist true");
			}
			else {
				out.low();
				System.out.println("sendlength Bit " + i + " ist false");
			}
			try {
				Thread.sleep(receiveWaitMillis);
				System.out.println("50ms wurden gewartet, bevor ein neues Bit der Nachrichtenlänge gesendet wird.");
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// Now Sending Bits in BitSet send
		for (int i = 0; i < send.length(); i++) {
			if (send.get(i)) {
				out.high();
				System.out.println("Nachrichten Bit " + i + " ist true");
			}
			else {
				out.low();
				System.out.println("Nachrichten Bit " + i + " ist false");
			}
			try {
				Thread.sleep(receiveWaitMillis);
				System.out.println("50ms wurden gewartet, bevor ein neues Nachrichtenbit gesendet wird.");
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		out.low();
		System.out.println("Zum Abschluss wurde die Spannung wieder auf low gesetzt.");
	}
	
	public byte[] receiveByteArray() {
		System.out.println("Methode zum Empfangen von Daten wurde aufgerufen.");
//		try {
//			Thread.sleep(30);
//			System.out.println("Initial wurde 30ms gewartet.");
//		}
//		catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		for (int i = 0; i < 32; i++) {
			if (in.isHigh()) {
				receiveLength.set(i);
				System.out.println("receiveLength BitSet Bit " + i + " wurde gesetzt, weil Spannung anliegt.");
			}
			try {
				Thread.sleep(receiveWaitMillis);
				System.out.println("50ms wurde gewartet, bevor der neue Zustand eingelesen wird.");
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		int a = (int) (receiveLength.toLongArray()[0]);
		System.out.println("expected length of expected text in bits: " + a);
		try {
			Thread.sleep(receiveWaitMillis);
			System.out.println("50ms wurden gewartet, nachdem die Bitlänge berechnet wurde.");
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < a; i++) {
			if (in.isHigh()) {
				receive.set(i);
				System.out.println("Bit " + i + " des Nachrichten BitSet wurde auf true gesetzt.");
			}
			try {
				Thread.sleep(receiveWaitMillis);
				System.out.println("50ms wurden vor dem nächsten Einlesen gewartet.");
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Nachricht hat die Länge in Bits: " + receive.length());
		return receive.toByteArray();
	}
	
	public void shutdown() {
		gpio.shutdown();
	}
	
	public void run() {
		while (true) {
			
			if (in.isHigh()) {
				inPinState = true;
			}
			else {
				inPinState = false;
			}
			try {
				Thread.sleep(1);
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if ((in.isHigh() && !inPinState) || (!in.isHigh() && inPinState)) {

				System.out.println("Input Pin Voltage Change to: " + in.isHigh());
				inputPinChanged();
			}
		}
	}
	
	public void inputPinChanged() {
		System.out.println("Begin to receive text. Received text will be printed here:\n");
		System.out.println(new String(receiveByteArray()));
	}
}
