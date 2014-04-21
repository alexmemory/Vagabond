package org.vagabond.commandline.errgen;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.HelpFormatter;

import java.util.Random;
import java.io.*;

import org.vagabond.commandline.explgen.CommandLineExplGen;

public class CommandLineErrGen {

	
	public static void main(String[] args) {
		Options options = new Options();
		//options.addOption(String opt, String longOpt, boolean hasArg, String description)
		options.addOption("t", "tables", true, "relation names");
		options.addOption("a", "attrs", true, "attribute names");
		options.addOption("s", "sizeoftable", true, "size of table");
		options.addOption("e", "sizeoferrset", true, "size of error set");
		options.addOption("o", "outfile", true, "output error file");

        try {
            CommandLineParser parser = new GnuParser();
            CommandLine cmdline = parser.parse(options, args);

            if (!cmdline.hasOption("t") || !cmdline.hasOption("a") || !cmdline.hasOption("s") 
            		|| !cmdline.hasOption("e")  || !cmdline.hasOption("o") )
            {
                HelpFormatter help = new HelpFormatter();
                help.printHelp("cmdname", options);
                System.exit(-1);
            }

            String TableList    = cmdline.getOptionValue("t");
            String AttrList     = cmdline.getOptionValue("a");
            int SizeOfTable     = Integer.parseInt(cmdline.getOptionValue("s"));
            int RequiredErrSize = Integer.parseInt(cmdline.getOptionValue("e"));
            String OutFile      = cmdline.getOptionValue("o");
            
            String[] Tables = TableList.split(",");
            int AmountOfTables = Tables.length;
            
            String[] Attrs = AttrList.split(";");
            
            Random randomForTID = new Random();
            Random randomForTable = new Random();
            Random randForAttr = new Random();
            Writer writer = null;

            try {
                writer = new BufferedWriter(new OutputStreamWriter(
                      new FileOutputStream(OutFile), "utf-8"));
                
                for (int i=0; i<RequiredErrSize; i++)
                {
                	int randTable = randomForTable.nextInt(AmountOfTables);
                	int randomTID = randomForTID.nextInt(SizeOfTable);
                	String tableAttrs = Attrs[randTable];
                	String[] cols = tableAttrs.split(",");
                	int rantAttrIdx = randForAttr.nextInt(cols.length);
                	
                	String TName = Tables[randTable];
                	String AName = cols[rantAttrIdx];
                	
                	String outline = "A("+TName+","+randomTID+","+ AName+")\n";
                	writer.write(outline);
                }
                
            } catch (IOException ex) {
              // report
            } finally {
               try {writer.close();} catch (Exception ex) {}
            }

            
            
            System.out.println("Error file generated...");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
	}


}
