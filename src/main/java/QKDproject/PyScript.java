package QKDproject;

import java.io.*;

/**
 * Class that encapsulates a running python script.
 * @authors Marc and Raphael
 */
public class PyScript {
	private BufferedReader pyIn;
	private PrintWriter pyOut;
	private PrintWriter debugger;
	private static Environment env = null;
	
	private enum Environment {
		Windows_Conda,
		Windows_Builtin
	};
	
	/**
	 * Runs a python script with the given args in the given anaconda env.
	 * Should be able to detect whether the builtin python has qiskit installed
	 * or not and work accordingly.
	 * Windows only for now. For Anaconda: relies on cmd.exe having been 
	 * initialized with conda (ie conda initialize cmd.exe has been run).
	 * @param scriptLocation Path to the python script
	 * @param condaEnvName Name of conda environment to run in, if necessary.
	 * @param args Arguments to give to python script
	 * @throws IOException 
	 */
	public PyScript(String scriptLocation, 
			String condaEnvName, String... args) throws IOException {
		if (env == null)
			detectEnvironment();
		if (env == Environment.Windows_Conda) {
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

			debugger = new PrintWriter(new FileOutputStream(new File("test.txt"), true));
		} else {
			String[] initialArgs = {"py", scriptLocation};
			String[] pbArgs = new String[initialArgs.length + args.length];
			System.arraycopy(initialArgs, 0, pbArgs, 0, initialArgs.length);
			System.arraycopy(args, 0, pbArgs, initialArgs.length, args.length);
			ProcessBuilder pb = new ProcessBuilder(pbArgs);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			pyIn = new BufferedReader(new InputStreamReader(p.getInputStream()));
			pyOut = new PrintWriter(new BufferedOutputStream(p.getOutputStream()));

			debugger = new PrintWriter(new FileOutputStream(new File("test.txt"), true));
		}
		
	}
	
	private static synchronized void detectEnvironment() throws IOException {
		if (env != null)
			return;
		String testScript = new File(".").getCanonicalPath() + 
                                    File.separatorChar + "src" + File.separatorChar + "main" + 
                                    File.separatorChar + "pythonTest.py";
		//see if builtin Python in windows will work
		try {
			ProcessBuilder pb = new ProcessBuilder("py", testScript);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			BufferedReader tempIn = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String l = tempIn.readLine();
			//System.out.println("returned line: " + l);
			if (l != null && l.equals("import successful")) {
				env = Environment.Windows_Builtin;
			} else {
				env = Environment.Windows_Conda;
			}
		} catch (IOException e) {
			env = Environment.Windows_Conda;
		}
	}
	
	/**
	 * Gives the given String to the python process and returns the resulting
	 * line.
	 * @param input String passed to the process's stdin.
	 * @return Resulting output line.
	 * @throws IOException 
	 */
        
        //QKD
	public synchronized String getResults(String input) throws IOException {
		pyOut.println(input);
		pyOut.flush();
                return pyIn.readLine();
	}
        
        //QKA
        //GiveData ,returns data necessary to find key
        public synchronized String[] getResults(double securityProperty) throws IOException {
                pyOut.println(securityProperty);
                pyOut.flush();
                String[] ls = new String[12];
                for (int i = 0; i < ls.length; i++) {
                    ls[i] = pyIn.readLine();
                    //System.out.println(ls[i]+" ");
                }
                return ls;
        }

        //ReceiveData1, outputs decoy bits, and lists without decoys
        public synchronized String[] getResults(String strS__, String strC_, String pos_dS, String pos_dC, String ba_dS, String ba_dC) throws IOException {
            String temp = strS__ + "#" + strC_ + "#" + pos_dS + "#" + pos_dC + "#" + ba_dS + "#" + ba_dC;
            pyOut.println(temp);
            pyOut.flush();
            String[] ls = new String[4];
            for (int i = 0; i < ls.length; i++) {
                    ls[i] = pyIn.readLine();
            }
            return ls;
        }
        //securityCheck,
        public synchronized String[] getResults(String dS, String dC, String bi_dS, String bi_dC) throws IOException {
            String temp = dS + "#" + dC + "#" + bi_dS + "#" + bi_dC;
            pyOut.println(temp);
            pyOut.flush();
            String[] ls = new String[2];
            ls[0] = pyIn.readLine();
            ls[1] = pyIn.readLine();
            return ls;
        }
        
        //makeKey, outputs shared key
        public synchronized String getResults(String Kkey, String S_, String C, String seed, String bS, String bH, String placeholder) throws IOException {
            String temp = Kkey + "#" + S_ + "#" + C + "#" + seed + "#" + bS + "#" + bH + "#" + "placeholder";
            pyOut.println(temp);
            pyOut.flush();
            return pyIn.readLine();
        }
        
        
        //intercept
        //intercept 2 strings, each is an array of strings with each element (string) seperated by "@"
        public synchronized String[] getResults(String strS__, String strC_) throws IOException {
            // each argument is seperated by "#", element by @
            String temp = strS__ + "#" + strC_;
            
            pyOut.println(temp);
            pyOut.flush();
            String[] ls = new String[2];
            ls[0] = pyIn.readLine();
            ls[1] = pyIn.readLine();

            return ls;
        }
        
        
        
                
}
