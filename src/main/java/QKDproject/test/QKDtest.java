package QKDproject.test;
import QKDproject.*;
/**
 * Tests QKD class.
 * @author Marc
 */
public class QKDtest {
	public static void main(String[] args) {
		System.out.println("w/ eve");
		QKD qkd = new QKD(true, 50, true);
		QKD o = new QKD(true, 50, false);
		qkd.connect(o);
		qkd.makeKey();
		
		System.out.println("w/o eve");
		qkd = new QKD(false, 50, true);
		o = new QKD(false, 50, false);
		qkd.connect(o);
		qkd.makeKey();
	}
}
