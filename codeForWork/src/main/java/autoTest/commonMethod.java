package autoTest;

public class commonMethod {
	public final static String relativeSymbol="./";
	public final static String newLine="\\n";
	public static String ConvertTai(String input) {
		return input.replace("台", "臺");
	}
	
	public static String charToSymbol(String input) {
		return input.replace("之", "-");
	}
	
	public static String preprocessAddress(String input) {
		return charToSymbol(ConvertTai(input));
	}
}
