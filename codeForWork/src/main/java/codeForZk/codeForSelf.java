package codeForZk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class codeForSelf {
	public static void main(String[] args) {

		System.out.println("Enter:");

		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader bufferedReader = new BufferedReader(isr);
		String line = "";
		StringBuilder paragraph = new StringBuilder();
		try {
			do {
				line = bufferedReader.readLine();
				paragraph.append(line);
			} while (!line.equals("exit"));
			isr.close();
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		
		
		System.out.println(paragraph);

	}
}
