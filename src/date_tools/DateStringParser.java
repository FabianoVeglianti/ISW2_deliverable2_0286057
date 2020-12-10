package date_tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateStringParser {

	private static final String format = "yyyy-MM-dd";
	
	//ottiene i primi len(yyyy-MM-gg)=10 caratatteri di una stringa
	private static String cutDateString(String dateString) {
		return dateString.substring(0, format.length());
	}
	
	
	public static Date getDateFromString(String dateString) {
		dateString = cutDateString(dateString);
		
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		Date date = new Date();
		try {
			date = formatter.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}  
		return date;
	}
	
}
