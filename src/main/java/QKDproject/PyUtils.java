package QKDproject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Class that helps run python code.
 * @author Marc
 */
public class PyUtils {
	/**
	 * Runs a python script with the given args in the given anaconda env.
	 * Relies on cmd.exe having been initialized with conda (ie conda initialize
	 * cmd.exe has been run). As such, only works on windows.
	 * @param scriptLocation Path to the python script
	 * @param condaEnvName Name of conda environment to run in.
	 * @param args Arguments to give to python script
	 * @return A BufferedReader. It will read what the python script outputs.
	 * It will also read stderr.
	 * @throws IOException 
	 */
	public static BufferedReader runPythonConda(String scriptLocation, 
			String condaEnvName, String... args) throws IOException {
		String[] initialArgs = {"cmd.exe", "/c", "conda", "activate", condaEnvName, 
			"&&", "python", scriptLocation};
		String[] pbArgs = new String[initialArgs.length + args.length];
		System.arraycopy(initialArgs, 0, pbArgs, 0, initialArgs.length);
		System.arraycopy(args, 0, pbArgs, initialArgs.length, args.length);
		
		ProcessBuilder pb = new ProcessBuilder(pbArgs);
		pb.redirectErrorStream(true);
		Process p = pb.start();
		return new BufferedReader(new InputStreamReader(p.getInputStream()));
	}
}
