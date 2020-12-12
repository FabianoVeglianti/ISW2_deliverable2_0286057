package date_tools;

import java.util.Calendar;
import java.util.Date;

public class DateCreator {
	
	private DateCreator() {
		//private constructor
	}
	
	public static Date getDateFromEpoch(long millisecondsFromEpoch) {
		Calendar calendar = Calendar.getInstance();
		
		calendar.setTimeInMillis(millisecondsFromEpoch);
		
		return calendar.getTime();
	}
	
	
	public static Date getTomorrow() {
		// get a calendar instance, which defaults to "now"
	    Calendar calendar = Calendar.getInstance();
	    
	 
	    // add one day to the date/calendar
	    calendar.add(Calendar.DAY_OF_YEAR, 1);
	    
	    // get and returns "tomorrow" 
	    return calendar.getTime();

	}

	public static Date getOldDate() {
		// get a calendar instance, which defaults to "now"
	    Calendar calendar = Calendar.getInstance();
	    
	    // add one day to the date/calendar
	    calendar.set(1971, 01, 01);
	    
	    // now get and returns an "old date"
	    return calendar.getTime();


	}

	public static Date getFutureDate() {
		// get a calendar instance, which defaults to "now"
	    Calendar calendar = Calendar.getInstance();
	    
	    // add one day to the date/calendar
	    calendar.set(2071, 01, 01);
	    
	    // now get and returns a "future date"
	    return calendar.getTime();
	}
	
	
}
