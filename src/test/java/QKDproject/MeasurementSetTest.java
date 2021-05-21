package QKDproject;

import java.util.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author Marc
 */
public class MeasurementSetTest {
	private MeasurementSet t1, t2, t3;
	
	public MeasurementSetTest() {
		t1 = new MeasurementSet("abcdefghijk");
		t2 = t1.makeSubstring(0,1,2,9,10);
		t3 = t2.makeSubstring(0,1,3);
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}

	/**
	 * Test of makeSubstring method, of class MeasurementSet.
	 */
	@Test
	public void testMakeSubstring() {
		String actual = t1.makeSubstring(Arrays.asList(0,1,2,8,10)).toString();
		assertEquals("abcik", actual);
		String actual2 = t1.makeSubstring(0,1,2,8,10).toString();
		assertEquals("abcik", actual);
	}

	/**
	 * Test of complement method, of class MeasurementSet.
	 */
	@Test
	public void testComplement() {
		String actual = t1.complement().toString();
		assertEquals("", actual);
		String actual2 = t2.complement().toString();
		assertEquals("defghi", actual2);
		String actual3 = t1.complement().complement().toString();
		assertEquals("abcdefghijk", actual3);
	}

	/**
	 * Test of indicesRoot method, of class MeasurementSet.
	 */
	@Test
	public void testIndicesRoot() {
		assertEquals(Arrays.asList(0,1,2,3,4,5,6,7,8,9,10), t1.indicesRoot());
		assertEquals(Arrays.asList(0,1,2,9,10), t2.indicesRoot());
		assertEquals(Arrays.asList(0,1,9), t3.indicesRoot());
	}

	/**
	 * Test of length method, of class MeasurementSet.
	 */
	@Test
	public void testLength() {
		assertEquals(11, t1.length());
		assertEquals(5, t2.length());
		assertEquals(3, t3.length());
	}

	/**
	 * Test of charAt method, of class MeasurementSet.
	 */
	@Test
	public void testCharAt() {
		assertEquals('b', t2.charAt(1));
		assertEquals('k', t2.charAt(4));
		assertEquals('j', t3.charAt(2));
	}

	/**
	 * Test of subSequence method, of class MeasurementSet.
	 */
	@Test
	public void testSubSequence() {
		assertEquals("bcj", t2.subSequence(1, 4));
	}
	
	/**
	 * Test that they are really immutable.
	 */
	@Test
	public void testImmutability() {
		List<Integer> indices = new ArrayList<>(Arrays.asList(0,1,3));
		MeasurementSet test = t1.makeSubstring(indices);
		assertEquals("abd", test.toString());
		indices.clear();
		assertEquals("abd", test.toString());
	}
	
	/**
	 * Test equals method of MeasurmentSet.
	 */
	@Test
	public void testEquals() {
		assertFalse(t1.equals("abcdefghijk"));
		assertFalse("abcdefghijk".equals(t1));
		assertFalse(t2.equals(new MeasurementSet("abcjk")));
		assertTrue(t2.equals(new MeasurementSet(t1, Arrays.asList(0,1,2,9,10))));
	}
	
}
