package date_tools;

import java.util.Date;

public class DateComputer {
				
	private DateComputer() {
		//private constructor
	}
	
	public static double getDifferenceInWeeksBetweenDates(Date date1, Date date2) {
		long epoc1 = date1.getTime();
		long epoc2 = date2.getTime();
		
		double seconds = ((double)epoc2 - (double)epoc1)/(double)1000;
		double secondsInAWeek = (double)(7*24*60*60);
		return seconds/secondsInAWeek; //weeks
	
	}
	
}
