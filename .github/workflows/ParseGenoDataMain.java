/*_____________________________________________________________________
*
* Copyright (c) 2020 ANU College of Medicine Biology and Environment
* _____________________________________________________________________
*/

import java.io.*;
import java.lang.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This program reads and parses a genomic data file in Variant Call Format and combine the fields
 * into the format chr<CHROM>:<POS><REF>><ALT>, for example, chr16:11178640G>A. The program ignores
 * the metadata marked by ## and the column headings marked by # in the file that.
 *
 * @author Rodger Luo
 * @version 1.0
 * @since March 2020
 */
public class ParseGenoDataMain extends Thread  
{
	//output constant string
	static final String CHROMOSOME = "chr";
	static final String COLON = ":";
	static final String ANGLE_BRACKET = ">";

	//define pattern for column split
	private static final Pattern tabSplitter = Pattern.compile("\t");	
	//store the line number for error or exception check
	private int lineNum = 0;
	//the name of the file that store data for parsing
	static String fileName;
	
	public void run()
    	{
		try  
		{  
			/*
			 * creates a file instance, reads the file, creates a buffering input stream and 
			 * constructs a string buffer to read a data record in the file.
			 */
			BufferedReader buffReader = new BufferedReader(new FileReader(new File(fileName)));   
			StringBuffer strBuffer = new StringBuffer();
			
			String dataRecord;
			while((dataRecord = buffReader.readLine())!= null)  
			{  
				++lineNum;
				if (dataRecord.startsWith("##") || dataRecord.startsWith("#"))
				{
					continue;
				}
				//appends line to string buffer 
				strBuffer.append(parseLine(dataRecord));
				//appends line feed 
				strBuffer.append("\n");  
			}
			
			/*
			 * creates a buffered writer and writes contents of StringBuffer
			 * to an output file, use BufferedWriter class.
			 */
			BufferedWriter buffWriter = new BufferedWriter(new FileWriter(new File("output.txt")));
			buffWriter.write(strBuffer.toString());
			buffWriter.flush();
		
			//closes the input and output streams and release the resources			
			buffReader.close();
			buffWriter.close();
			
			System.out.println("The parsed data have been writtern to the file output.txt.");  
			//System.out.println(strBuffer.toString());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
    	}

	public static void main(String args[]) throws Exception
	{  
		ParseGenoDataMain mainThread = new ParseGenoDataMain();
				
		try  
		{
			if (args != null && args.length > 0)
			{
				fileName = args[0];
				mainThread.start();
			} else{
				System.out.print("No file name provided. Please type a full data file name including suffix.");
			}
		} 
		catch(Exception ex)  
		{  
			ex.printStackTrace();  
		}  
	}  

	/**
	 * Parse data record.
	 *
	 * @param line a data record from the file
	 * @return the parsed string in the format: chr<CHROM>:<POS><REF>><ALT>
	 * @throws IllegalArgumentException
    	 */
	public String parseLine(String line) throws IOException
	{
		try
		{
			List<String> dataLine = toList(tabSplitter, line);

			// Chromosome (CHROM)
			String chr = dataLine.get(0);
			// Position (POS)
			long pos;
			try
			{
				pos = Long.parseLong(dataLine.get(1));
			}
			catch (NumberFormatException e)
			{
				throw new IllegalArgumentException("Error at the line #" + lineNum + " where Position: " +
					dataLine.get(1) + " is not a number");
			}
			// Reference base (REF)
			String ref = dataLine.get(3);
			// Alternate base (ALT)
			String alt = dataLine.get(4);
			
			//concatenate and return the new record line
			String str1 = CHROMOSOME.concat(chr).concat(COLON).concat(Long.toString(pos));
			return str1.concat(ref).concat(ANGLE_BRACKET).concat(alt);
		}

		catch (IllegalArgumentException ex)
		{
			throw ex;
		}
	}
	
	/**
	 * This method splits tab seperated record into a data list.
	 *
	 * @param pattern the tab pattern
	 * @param string the data record from the file
	 * @return a list containing the tab seperated data in a record
   	 */
	private List<String> toList(Pattern pattern, String string)
	{
		String[] array = pattern.split(string);
		List<String> list = new ArrayList<>(array.length);
		Collections.addAll(list, array);
		return list;
	}
}  
