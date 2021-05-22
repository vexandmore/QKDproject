package QKDproject;

import java.util.*;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Marc
 */
public class UtilsTest {
	
	public UtilsTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}

	/**
	 * Test of sampleIndices method, of class Utils.
	 */
	@Test
	public void testSampleIndices() {
		List<Integer> indices = Utils.sampleIndices(50, 100);
		assertEquals(50, indices.size());
		assertEquals(Integer.valueOf(2), indices.get(1));
		
		indices = Utils.sampleIndices(32, 100);
		assertEquals(34, indices.size());
		assertEquals(Integer.valueOf(6), indices.get(2));
		
		indices = Utils.sampleIndices(0, 100);
		assertEquals(Arrays.asList(), indices);
		
		indices = Utils.sampleIndices(200, 100);
		assertEquals(50, indices.size());
		assertEquals(Integer.valueOf(2), indices.get(1));
	}

	/**
	 * Test of isSorted method, of class Utils.
	 */
	@Test
	public void testIsSorted() {
		ArrayList<Integer> list = new ArrayList<>();
		
		//Tests 1 with a list of integers
		assertTrue(Utils.isSorted(list));
		
		list.addAll(Arrays.asList(1,2,3,4,5,6,1000));
		assertTrue(Utils.isSorted(list));
		list.add(2);
		assertFalse(Utils.isSorted(list));
		
		//Test with array of integers which are sorted except for an element in middle
		list.clear();
		for (int i = 0; i < 10; i++) {
			if (i == 5)
				list.add(20);
			else
				list.add(i);
		}
		assertFalse(Utils.isSorted(list));
		
		//Test with 2 calendars
		List<Calendar> calendars = new ArrayList<>();
		calendars.add(new GregorianCalendar());
		GregorianCalendar future = new GregorianCalendar();
		future.add(Calendar.MONTH, 1);
		calendars.add(future);
		assertTrue(Utils.isSorted(calendars));
		
		calendars.clear();
		
		//Test with 10 calendars in order
		for (int i = 0; i < 10; i++) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.add(Calendar.MONTH, i);
			calendars.add(cal);
		}
		assertTrue(Utils.isSorted(calendars));

		//Test with 1 calendar at the end not in order
		calendars.add(new GregorianCalendar());
		assertFalse(Utils.isSorted(calendars));
	}
}
