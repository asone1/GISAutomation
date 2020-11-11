package codeForZk;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class readPdf {

	public static void main(String[] args) {
		try {
			PDDocument pdfRef = PDDocument
					.load(new File("C:\\Users\\User\\Downloads\\resource-book-for-permaculture_1.pdf"));
			PDFTextStripper stripper = new PDFTextStripper();

			for (int pageNum = 1; pageNum < pdfRef.getNumberOfPages(); pageNum++)

			{
				stripper.setStartPage(pageNum);
				stripper.setEndPage(pageNum);
				System.out.println(pageNum);
				System.out.println(stripper.getText(pdfRef));
			}

		} catch (IOException e)

		{
			e.printStackTrace();
		}
		System.out.println("Done");
	}

}
