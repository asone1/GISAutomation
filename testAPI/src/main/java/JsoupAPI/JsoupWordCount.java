package JsoupAPI;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.docs.v1.Docs;

public class JsoupWordCount {

	public static void countWordsFromUrl(String url) throws IOException {
		//long time = System.currentTimeMillis();
		String regRule = "\\W+";

		Map<String, Word> countMap = new HashMap<String, Word>();
		Document doc = Jsoup.connect(url).get();

		// Get the actual text from the page, excluding the HTML
		String text = doc.body().text();

		// Create BufferedReader so the words can be counted
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))));
		String line;
		while ((line = reader.readLine()) != null) {
			String[] words = line.split(regRule);
			for (String word : words) {
				if (word.length() > 1) {
					if ("".equals(word)) {
						continue;
					}

					Word wordObj = countMap.get(word);
					if (wordObj == null) {
						wordObj = new Word();
						wordObj.word = word;
						wordObj.count = 0;
						countMap.put(word, wordObj);
					}

					wordObj.count++;
				}
			}

		}

		reader.close();

		SortedSet<Word> sortedWords = new TreeSet<Word>(countMap.values());
		int i = 0;
		int maxWordsToDisplay = 10;

		String[] wordsToIgnore = { "the", "of", "and", "a", "to", "in", "is", "you", "that", "it", "he", "was", "for",
				"on", "are", "as", "with", "his", "they", "I", "at", "be", "this", "have", "from", "or", "one", "had",
				"by", "word", "but", "not", "what", "all", "were", "we", "when", "your", "can", "said", "there", "use",
				"an", "each", "which", "she", "do", "how", "their", "if", "will", "up", "other", "about", "out", "many",
				"then", "them", "these", "so", "some", "her", "would", "make", "like", "him", "into", "time", "has",
				"look", "two", "more", "write", "go", "see", "number", "no", "way", "could", "people", "my", "than",
				"first", "water", "been", "call", "who", "oil", "its", "now", "find", "long", "down", "day", "did",
				"get", "come", "made", "may", "part" };

		for (Word word : sortedWords) {
			if (i >= maxWordsToDisplay) { // 10 is the number of words you want to show frequency for
				break;
			}

			if (Arrays.asList(wordsToIgnore).contains(word.word.toLowerCase())) {
				i++;
				maxWordsToDisplay++;
			} else {
				System.out.println(word.count + "\t" + word.word);
				i++;
			}
		}

	}
	
	public static void countCHWordsFromUrl(String url) throws IOException {
		//long time = System.currentTimeMillis();
		String regRule = "[,;:，!?。.]+";

		Map<String, Word> countMap = new HashMap<String, Word>();
		Document doc = Jsoup.connect(url).get();

		// Get the actual text from the page, excluding the HTML
		String text = doc.body().text();

		// Create BufferedReader so the words can be counted
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))));
		String line;
		while ((line = reader.readLine()) != null) {
			String[] words = line.split(regRule);
			
			System.out.println(line);
			for (String word : words) {
				if (word.length() > 1) {
					if ("".equals(word)) {
						continue;
					}

					Word wordObj = countMap.get(word);
					if (wordObj == null) {
						wordObj = new Word();
						wordObj.word = word;
						wordObj.count = 0;
						countMap.put(word, wordObj);
					}

					wordObj.count++;
				}
			}

		}

		reader.close();

		SortedSet<Word> sortedWords = new TreeSet<Word>(countMap.values());
		int i = 0;
		int maxWordsToDisplay = 10;

		String[] wordsToIgnore = { };

		for (Word word : sortedWords) {
			if (i >= maxWordsToDisplay) { // 10 is the number of words you want to show frequency for
				break;
			}

			if (Arrays.asList(wordsToIgnore).contains(word.word.toLowerCase())) {
				i++;
				maxWordsToDisplay++;
			} else {
				System.out.println(word.count + "\t" + word.word);
				i++;
			}
		}

	}

	public static class Word implements Comparable<Word> {
		String word;
		int count;

		@Override
		public int hashCode() {
			return word.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return word.equals(((Word) obj).word);
		}

		public int compareTo(Word b) {
			return b.count - count;
		}
	}

	public static void main(String... args) throws IOException, GeneralSecurityException {
//		countWordsFromUrl("https://developers.google.com/web/fundamentals/web-components/customelements");
//		countCHWordsFromUrl();
	}

}