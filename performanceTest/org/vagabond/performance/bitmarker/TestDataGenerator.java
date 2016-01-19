package org.vagabond.performance.bitmarker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.csvreader.CsvWriter;

public class TestDataGenerator {
	static Logger log = Logger.getLogger(TestDataGenerator.class);
	
	public static void main(String[] args) {
		
		PropertyConfigurator.configure("resource/test/perfLog4jproperties.txt");
		
	
		// Homeless		
		String outputFile1 = "resource/exampleData/tramp_1000000.csv";
		String outputFile2 = "resource/exampleData/socialworker_1000000.csv";
		String outputFile3 = "resource/exampleData/soupkitchen_1000000.csv";
		
		// before we open the file check to see if it already exists
		boolean alreadyExists1 = new File(outputFile1).exists();
		boolean alreadyExists2 = new File(outputFile2).exists();
		boolean alreadyExists3 = new File(outputFile3).exists();

		if (alreadyExists1) {
			new File(outputFile1).delete();
		}
		if (alreadyExists2) {
			new File(outputFile2).delete();
		}
		if (alreadyExists3) {
			new File(outputFile3).delete();
		}
		
		try {
			
			CsvWriter csvOutput1 = new CsvWriter(new FileWriter(outputFile1, true), ',');
			CsvWriter csvOutput2 = new CsvWriter(new FileWriter(outputFile2, true), ',');
			CsvWriter csvOutput3 = new CsvWriter(new FileWriter(outputFile3, true), ',');
			
			// write out a few records
			Random random = new Random();

			char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
			char[] ints = "0123456789".toCharArray();
			
			String outPut = "";
			StringBuilder sb = new StringBuilder();

			for(int j = 1; j < 1000001; j++) {

				csvOutput1.write("" + j);
				csvOutput2.write("" + j);
				csvOutput3.write("" + j);
				
				for (int l = 0; l < 4; l++) {
					for (int i = 0; i < 10; i++) {
						char c = chars[random.nextInt(chars.length)];
						sb.append(c);
					}
					String output = sb.toString();
					csvOutput1.write(output);
					if (l == 2)
						csvOutput3.write(output);
					if (l == 3)
						csvOutput2.write(output);
					if (l == 2)
						outPut = output;

					sb.delete(0, sb.length());
				}
				
				for (int l = 0; l < 3; l++) {
					for (int i = 0; i < 10; i++) {
						char c = chars[random.nextInt(chars.length)];
						sb.append(c);
					}
					String output = sb.toString();
					if (l == 1)
						csvOutput2.write(output);
					if (l == 2)
						csvOutput2.write(outPut);
					
					sb.delete(0, sb.length());
				}
				
				for (int l = 0; l < 3; l++) {
								
					if (l == 1) {
						for (int i = 0; i < 10; i++) {
							char c = chars[random.nextInt(chars.length)];
							sb.append(c);
						}
						String output = sb.toString();
						csvOutput3.write(output);
					}
					
					if (l == 2) {
						for (int i = 0; i < 10; i++) {
							char num = ints[random.nextInt(ints.length)];
							sb.append(num);
						}
						String output = sb.toString();
						csvOutput3.write(output);						
					}
					
					sb.delete(0, sb.length());
				}

				csvOutput1.endRecord();
				csvOutput2.endRecord();
				csvOutput3.endRecord();
				sb.delete(0, sb.length());
			}

			csvOutput1.close();
			csvOutput2.close();
			csvOutput3.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		

		// Employee
		String outputFile4 = "resource/exampleData/employee_100000.csv";
		String outputFile5 = "resource/exampleData/firm_100000.csv";
		String outputFile6 = "resource/exampleData/address_100000.csv";
		String outputFile7 = "resource/exampleData/customer_100000.csv";
		
		boolean alreadyExists4 = new File(outputFile4).exists();
		boolean alreadyExists5 = new File(outputFile5).exists();
		boolean alreadyExists6 = new File(outputFile6).exists();
		boolean alreadyExists7 = new File(outputFile7).exists();
				
		if (alreadyExists4) {
			new File(outputFile4).delete();
		}
		if (alreadyExists5) {
			new File(outputFile5).delete();
		}
		if (alreadyExists6) {
			new File(outputFile6).delete();
		}
		if (alreadyExists7) {
			new File(outputFile7).delete();
		}
		
		
		try {
		
			CsvWriter csvOutput4 = new CsvWriter(new FileWriter(outputFile4, true), ',');
			CsvWriter csvOutput5 = new CsvWriter(new FileWriter(outputFile5, true), ',');
			CsvWriter csvOutput6 = new CsvWriter(new FileWriter(outputFile6, true), ',');
			CsvWriter csvOutput7 = new CsvWriter(new FileWriter(outputFile7, true), ',');
			
			// write out a few records
			Random random = new Random();

			char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
			char[] ints = "012345678901234567890123456789".toCharArray();
			
			String OutPut = "";
			String IntOutPut = "";
			String firmName = "";
			StringBuilder sb = new StringBuilder();

			for(int j = 1; j < 100001; j++) {

				csvOutput4.write("" + j);
				csvOutput5.write("" + j);
				csvOutput6.write("" + j);
				csvOutput7.write("" + j);
				
				// employee
				for (int l = 0; l < 4; l++) {
					for (int i = 0; i < 10; i++) {
						char c = chars[random.nextInt(chars.length)];
						sb.append(c);
					}
					String output = sb.toString();
					
					if (l == 0) {
						csvOutput4.write(output);
						OutPut = output;
					}
					if (l == 1)
						csvOutput4.write(OutPut);
					if (l == 2) {
						csvOutput4.write(output);
						firmName = output;
					}
					if (l == 3) {
						sb.delete(0, sb.length());
						for (int i = 0; i < 10; i++) {
							char num = ints[random.nextInt(ints.length)];
							sb.append(num);
						}
						output = sb.toString();
						csvOutput4.write(output);
						IntOutPut = output;
					}
					
					sb.delete(0, sb.length());
				}
				
				// firm
				for (int l = 0; l < 3; l++) {
					for (int i = 0; i < 10; i++) {
						char c = chars[random.nextInt(chars.length)];
						sb.append(c);
					}
					
					if (l == 0)
						csvOutput5.write(firmName);
					
					if (l == 1)
						csvOutput5.write(IntOutPut);
					
					if (l == 2)
						csvOutput5.write(OutPut);
					
					sb.delete(0, sb.length());
				}

				// address
				for (int l = 0; l < 3; l++) {
					for (int i = 0; i < 10; i++) {
						char c = chars[random.nextInt(chars.length)];
						sb.append(c);
					}
					String output = sb.toString();
					
					if (l != 0)
						csvOutput6.write(output);
					
					if (l == 0)
						csvOutput6.write(IntOutPut);
					
					sb.delete(0, sb.length());
				}
				
				// customer
				for (int l = 0; l < 3; l++) {
					for (int i = 0; i < 10; i++) {
						char c = chars[random.nextInt(chars.length)];
						sb.append(c);
					}
					String output = sb.toString();
					
					if (l == 0)
						csvOutput7.write(output);
					
					if (l == 1)
						csvOutput7.write(IntOutPut);
					
					if (l == 2) {
						sb.delete(0, sb.length());
						for (int i = 0; i < 10; i++) {
							char num = ints[random.nextInt(ints.length)];
							sb.append(num);
						}
						output = sb.toString();
						csvOutput7.write(output);
					}
					
					sb.delete(0, sb.length());
				}

				csvOutput4.endRecord();
				csvOutput5.endRecord();
				csvOutput6.endRecord();
				csvOutput7.endRecord();
				sb.delete(0, sb.length());
			}
			
			csvOutput4.close();
			csvOutput5.close();
			csvOutput6.close();
			csvOutput7.close();

			
		} catch (IOException e) {
			e.printStackTrace();
		}

		log.debug("------ CSV file has been created ------");
	}
}
