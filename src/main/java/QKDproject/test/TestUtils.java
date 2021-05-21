package QKDproject.test;
import QKDproject.Utils;
import java.util.*;
/**
 *
 * @author Marc
 */
public class TestUtils {
	public static void main(String[] args) {
		ArrayList<Integer> list = new ArrayList<>();
		
		//Tests 1 with a list of integers
		if (!Utils.isSorted(list)) {
			System.err.println("empty list is sorted");
		}
		list.addAll(Arrays.asList(1,2,3,4,5,6,1000));
		if (!Utils.isSorted(list)) {
			System.err.println("the list " + list + " is sorted");
		}
		list.add(2);
		if (Utils.isSorted(list)) {
			System.err.println("the list " + list + " is not sorted");
		}
		
		//Test with array of integers which are sorted except for an element in middle
		list.clear();
		for (int i = 0; i < 10; i++) {
			if (i == 5)
				list.add(20);
			else
				list.add(i);
		}
		if (Utils.isSorted(list))
			System.err.println("List " + list + " is not, in fact, sorted");
		
		//Test with 2 calendars
		List<Calendar> calendars = new ArrayList<>();
		calendars.add(new GregorianCalendar());
		GregorianCalendar future = new GregorianCalendar();
		future.add(Calendar.MONTH, 1);
		calendars.add(future);
		if (!Utils.isSorted(calendars))
			System.err.println("The list " + calendars + " is sorted");
		
		calendars.clear();
		
		//Test with 10 calendars in order
		for (int i = 0; i < 10; i++) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.add(Calendar.MONTH, i);
			calendars.add(cal);
		}
		if (!Utils.isSorted(calendars))
			System.err.println("The list " + calendars + " is sorted");
		
		//Test with 1 calendar at the end not in order
		calendars.add(new GregorianCalendar());
		if (Utils.isSorted(calendars))
			System.err.println("The list " + calendars + " is not sorted");
		
		System.out.println("isSorted test finished");
	}
}
