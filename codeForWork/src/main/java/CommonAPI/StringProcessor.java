package CommonAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.*;

public class StringProcessor {

    //    final static String spe ="`!@#$%^&*()_+':\"-=\\[\\]{};\\|,.<>/?~";
    final static String any_special_symbol = "`|!|@|#|$|%|^|&|*|(|)|_|+|'|:|\"|-|=|\\[|\\]|{|}|;|\\||,|.|<|>|/|?|~";
    final static String contain_special_symbol = "[.*" + any_special_symbol + ".*]+";

    public static boolean ifContainSpecialSymbol(String strToCheck){
        return Pattern.matches(contain_special_symbol, strToCheck);
    }

    public static String replaceSpecialSymbol(String strToCheck, String replacement){
        return  strToCheck.replaceAll("["+any_special_symbol+"]+", replacement);
    }

    public static String CapitalCharToLowerCaseWithDelimitor(String strToCheck, String Delimitor){

        List<Integer> indexOfCapital =IndexOfMatches(strToCheck,"[A-Z]");
        String result =strToCheck;
        for(Integer i: indexOfCapital){
            String capitalStr = String.valueOf(strToCheck.charAt(i));
            result = result.replace(capitalStr, Delimitor+capitalStr.toLowerCase());
        }
        return  result.trim();
    }

public static List<Integer> IndexOfMatches(String StrToCheck, String regularExp){
    List<Integer> result = new ArrayList<>();
    Pattern pattern = Pattern.compile(regularExp);
    Matcher matcher = pattern.matcher(StrToCheck);
    while (matcher.find()){
        result.add(matcher.start());//this will give you index
    }
    return result;
}

    public static void main(String... arg) {

        System.out.println(CapitalCharToLowerCaseWithDelimitor("ActKind"," "));
    }
}
