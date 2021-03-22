package CommonAPI;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PDFHandler {
	public static String readPdfContent(String url) throws IOException {
		try {
			URL pdfUrl = new URL(url);
			InputStream in = pdfUrl.openStream();
			BufferedInputStream bf = new BufferedInputStream(in);
			PDDocument doc = PDDocument.load(bf);
			int numberOfPages = getPageCount(doc);
//	System.out.println("The total number of pages " + numberOfPages);
			String content = new PDFTextStripper().getText(doc);
			doc.close();
			return content;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";

	}

	public static int getPageCount(PDDocument doc) {
		// get the total number of pages in the pdf document
		int pageCount = doc.getNumberOfPages();
		return pageCount;

	}
}
