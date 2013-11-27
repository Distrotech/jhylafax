package net.sf.jhylafax;

import static net.sf.jhylafax.JHylaFAX.i18n;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ArgsHandler {

	private LinkedList<String> args;
	private List<String> filenames = new ArrayList<String>();
	private String logConfigFilename = "logging-default.properties";
	private List<String> numbers = new ArrayList<String>();
	private boolean readStdin = false;
	private boolean quitAfterSending;
	
	public ArgsHandler()
	{
	}
	
	private void error(String error)
	{
		System.err.println(error);
		System.exit(1);
	}

	public void evaluate(String[] args)
	{
		this.args = new LinkedList<String>(Arrays.asList(args));
		
		while (hasNext()) {
			String arg = pop();
			if (!arg.startsWith("-")) {
				filenames.add(arg);
			}
			else {
				if ("-n".equals(arg)) {
					numbers.add(popArg("-n"));
				}
				else if ("-d".equals(arg)) {
					logConfigFilename = "logging-info.properties";
				}
				else if ("-dd".equals(arg)) {
					logConfigFilename = "logging-debug.properties";
				}
				else if ("--stdin".equals(arg)) {
					readStdin = true;
				}
				else if ("-q".equals(arg)) {
					quitAfterSending = true;
				}
				else if ("-h".equals(arg) | "-?".equals(arg) | "-help".equals(arg) | "--help".equals(arg)) {
					System.out.println("Usage: java -jar jhylafax.jar [options] [files]");
					System.out.println();
					System.out.println(" -d	            Output debug information to stderr");
					System.out.println(" -dd            Output more debug information to stderr");
					System.out.println(" -h --help      Print this help message");
					System.out.println(" -n number      Send fax to number");
					System.out.println(" -q	            Quit after sending fax");
					System.out.println(" --stdin        Read PostScript from stdin");
					System.exit(0);
				}
				else {
					error(i18n.tr("Invalid parameter. Use -h for help."));
				}
			}
		}
		
	}
	
	public String[] getFilenames()
	{
		return filenames.toArray(new String[0]);
	}
	
	public String getLogConfigFilename()
	{
		return logConfigFilename;
	}
	
	public String[] getNumbers()
	{
		return numbers.toArray(new String[0]);
	}
	
	public boolean getReadStdin()
	{
		return readStdin;
	}
	
	public boolean getQuitAfterSending() 
	{
		return quitAfterSending;
	}
	
	private boolean hasNext()
	{
		return !args.isEmpty();
	}
	
	private String pop()
	{
		return args.remove();
	}

	private String popArg(String parameter)
	{
		if (!hasNext()) {
			error(i18n.tr("{0} parameter requires argument", parameter));
		}
		String arg = pop();
		if (arg.startsWith("-")) {
			error(i18n.tr("{0} parameter requires argument", parameter));
		}
		return arg;
	}
}
