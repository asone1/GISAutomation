package CommonAPI;

public class ChineseAddressHandler {
	public final static String relativeSymbol = "./";
	public final static String newLine = "\\n";

	public static String ConvertTai(String input) {
		return input.replace("台", "臺");
	}

	public static String charToSymbol(String input) {
		return input.replace("之", "-");
	}

	public static String preprocessAddress(String input) {
		return charToSymbol(ConvertTai(input));
	}

	public static String takeLastNum(String address) {
		int hau = address.lastIndexOf("號");
		for (int idx = hau - 1; idx >= 0; --idx) {
			if (!Character.isDigit(address.charAt(idx)) && address.charAt(idx)!='-') {
				return address.substring(idx+1, hau).trim();
			}
		}
		return address;
	}
}
