package numeric_tools;

import java.util.regex.Pattern;

public class IntegerTool {
	/*Leggenda espressioni regolari (regex) in java:
	 * a-z <- da a a z
	 * [abc] <- a b oppure c
	 * \d <- qualsiasi cifra da 0 a 9 - analogo a [0-9]
	 * per altro consultare https://www.javatpoint.com/java-regex
	 * */
	private Pattern pattern = Pattern.compile("\\d");
	 
	public boolean isNumeric(String strNum) {
	    if (strNum == null) {
	        return false; 
	    }
	    return pattern.matcher(strNum).matches();
	}
}
