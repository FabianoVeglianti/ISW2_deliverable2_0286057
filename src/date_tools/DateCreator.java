package date_tools;

import java.util.Calendar;
import java.util.Date;

public class DateCreator {
	
	public static Date getTomorrow() {
		// get a calendar instance, which defaults to "now"
	    Calendar calendar = Calendar.getInstance();
	    
	 
	    // add one day to the date/calendar
	    calendar.add(Calendar.DAY_OF_YEAR, 1);
	    
	    // now get "tomorrow"
	    Date tomorrow = calendar.getTime();

	    // returns tomorrow
	   return tomorrow;
	}

	public static Date getOldDate() {
		// get a calendar instance, which defaults to "now"
	    Calendar calendar = Calendar.getInstance();
	    
	    // add one day to the date/calendar
	    calendar.set(1971, 01, 01);
	    
	    // now get an "old date"
	    Date oldDate = calendar.getTime();

	    // returns tomorrow
	   return oldDate;
	}

	public static Date getFutureDate() {
		// get a calendar instance, which defaults to "now"
	    Calendar calendar = Calendar.getInstance();
	    
	    // add one day to the date/calendar
	    calendar.set(2071, 01, 01);
	    
	    // now get an "old date"
	    Date futureDate = calendar.getTime();

	    // returns tomorrow
	   return futureDate;
	}
	
	
}
