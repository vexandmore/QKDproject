package QKDproject;

import java.io.*;

/**
 * Class that encapsulates a running python script
 * @author Marc
 */
public class PyScript {
	private BufferedReader pyIn;
	private PrintWriter pyOut;
	/**
	 * Runs a python script with the given args in the given anaconda env.
	 * Relies on cmd.exe having been initialized with conda (ie conda initialize
	 * cmd.exe has been run). As such, only works on windows.
	 * @param scriptLocation Path to the python script
	 * @param condaEnvName Name of conda environment to run in.
	 * @param args Arguments to give to python script
	 * @throws IOException 
	 */
	public PyScript(String scriptLocation, 
			String condaEnvName, String... args) throws IOException {
		String[] initialArgs = {"cmd.exe", "/c", "conda", "activate", condaEnvName, 
			"&&", "python", scriptLocation};
		String[] pbArgs = new String[initialArgs.length + args.length];
		System.arraycopy(initialArgs, 0, pbArgs, 0, initialArgs.length);
		System.arraycopy(args, 0, pbArgs, initialArgs.length, args.length);
		
		ProcessBuilder pb = new ProcessBuilder(pbArgs);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		pyIn = new BufferedReader(new InputStreamReader(p.getInputStream()));
		pyOut = new PrintWriter(new BufferedOutputStream(p.getOutputStream()));
	}
	
	/**
	 * Gives the given String to the python process and returns the resulting
	 * line.
	 * @param input String passed to the process's stdin.
	 * @return Resulting output line.
	 * @throws IOException 
	 */
	public synchronized String getResults(String input) throws IOException {
		pyOut.println(input);
		pyOut.flush();
		return pyIn.readLine();
	}
}
