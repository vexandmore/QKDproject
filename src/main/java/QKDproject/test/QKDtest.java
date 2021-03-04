package QKDproject.test;
import QKDproject.*;
/**
 * Tests QKD class.
 * @author Marc
 */
public class QKDtest {
	public static void main(String[] args) {
		QKD qkd = new QKD(true, 50, true);
		QKD o = new QKD(true, 50, false);
		qkd.connect(o);
		
		qkd.makeKey();
		//qkd.encryptMessage(Protocol.stringToBytes("hello"));
	}
}
