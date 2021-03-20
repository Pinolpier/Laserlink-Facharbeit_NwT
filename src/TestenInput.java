import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class TestenInput implements GpioPinListenerDigital {
	
	public static void main(String[] args) throws InterruptedException {
		GpioController gpio = GpioFactory.getInstance();
		GpioPinDigitalInput in = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04);
		in.setShutdownOptions(true);
		while(true){
			if (in.isHigh()) {
				System.out.println("1");
			} else {
				System.out.println("0");
			}
			Thread.sleep(100);
		}
	}
	
	@Override
	public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent arg0) {
		if (arg0.getPin() == RaspiPin.GPIO_04) {
			System.out.println("State changed!");
		}
	}
}