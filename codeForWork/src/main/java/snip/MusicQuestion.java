package snip;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

//右鍵console=>preferences=>run/debug，可以把字換行
public class MusicQuestion {
	public static void main(String[] args) {
		System.out.println("\nOutput: \n" + callURL(
				"https://www.chabad.org/library/article_cdo/aid/910344/jewish/Deot-Chapter-Four.htm"));
	}

	public static String callURL(String myURL) {
		System.out.println("Requeted URL:" + myURL);
		StringBuilder sb = new StringBuilder();
		URLConnection urlConn = null;
		InputStreamReader in = null;
		try {
			URL url = new URL(myURL);
			urlConn = url.openConnection();
			// 發生403錯誤，server不讓我們用JAVA拿資料
			//把JSON中的User-Agent傳入URLConnection，讓我們程式的http request看起來像是從browser發出的
			urlConn.addRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36");
			if (urlConn != null)
				urlConn.setReadTimeout(60 * 1000);
			if (urlConn != null && urlConn.getInputStream() != null) {
				in = new InputStreamReader(urlConn.getInputStream(),
						Charset.defaultCharset());
				BufferedReader bufferedReader = new BufferedReader(in);
				if (bufferedReader != null) {
					int cp;
					while ((cp = bufferedReader.read()) != -1) {
						sb.append((char) cp);
					}
					bufferedReader.close();
				}
			}
			in.close();
		} catch (Exception e) {
			throw new RuntimeException("Exception while calling URL:" + myURL,
					e);
		}
		// 把所有擷取到的HTML轉成string(為了使用indexOf函式
		String completeHTML = sb.toString();

		// 正文是從 ContentBody tag 開始，把前面的文字都刪掉
		for (int i = completeHTML.lastIndexOf("ContentBody") - 1; i > -1; i--) {

			sb.deleteCharAt(i);

		}
		// 把現在string builder 剩下的文字再轉成string(也是為了使用indexOf函式，計算要刪到第幾格 (從最後一格開始刪
		// PS 刪除會讓index一直減少，所以用 i--
		String deleteHeadHTML = sb.toString();
		for (int i = sb.length() - 1; i > deleteHeadHTML
				.lastIndexOf("Copyright, all rights reserved."); i--) {

			sb.deleteCharAt(i);

		}
		// 剩下的文字是我們要的部分，但剩下的字中仍有 不要的:把所有 <tag>刪除
		CleanTag.CleanTagName(sb);
		// 刪除所有空格
		String articleContent = sb.toString().replaceAll("(?m)^[ \t]*\r?\n",
				"");

		return articleContent;
	}
}