package codeForZk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class zkCodeGenerator {

	private static String attributesOfShipment[] = { "customerCode", "customerOrderId", "bookingDate", "containerType",
			"storeId", "shipmentDate", "itemCode", "batchNo", "expirationDate", "unit", "qty", "note", };

	public static void main(String[] args) {

		System.out.println("Enter zk code:");
		String array[] = attributesOfShipment;

		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader bufferedReader = new BufferedReader(isr);
		String line = "";
		StringBuilder paragraph = new StringBuilder();
		try {
			  do
              {
                  line = bufferedReader.readLine();
                  paragraph.append(line);
              }while(!line.equals("exit"));
           isr.close();
           bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String variable : array) {

			System.out.println(paragraph.substring(0, paragraph.length()-4).toString().replaceAll("#", variable));

		}

	}
}
