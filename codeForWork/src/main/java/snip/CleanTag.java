package snip;

public class CleanTag {

	public static StringBuilder CleanTagName(StringBuilder sb) {

		for (int i = 0; i < sb.length(); i++) {

			if (sb.charAt(i) == '<') {
				for (int j = 0; j <= sb.length(); j++) {
					int count = i;
					if (sb.charAt(count) == ('>')) {
						sb.deleteCharAt(count);
						i--;
						break;					
					}
					
					sb.deleteCharAt(count);

				}
			}
			
		}
		return sb;
	}
}