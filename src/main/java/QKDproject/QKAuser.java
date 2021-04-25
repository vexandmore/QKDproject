package QKDproject;

import QKDproject.exception.DecryptionException;
import QKDproject.exception.EncryptionException;
import QKDproject.exception.KeyExchangeFailure;
import java.io.File;
import java.io.IOException;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
/**
 *
 * @author Raphael
 * is a user, connected to one specific chat instance
 */
public class QKAuser implements Protocol{
    private static String SCRIPT_LOCATION;
    static {
            try {
                    SCRIPT_LOCATION = new File(".").getCanonicalPath() + 
                                    File.separatorChar + "src" + File.separatorChar + "main" + 
                                    File.separatorChar + "qkaImplementationFINAL.py";
            } catch (IOException e) {
                    SCRIPT_LOCATION = "error getting script location";
            }
    } 
    private static PyScript python;
    private String key = "";
    private StandardPBEByteEncryptor textEncryptor = new StandardPBEByteEncryptor();
    /*
    The idea is that each chat instance has it's own protocol
    so, each chat instance has its own contributor
    
    */
    protected QKAuser other;
        
    protected String[] getData(double securityProperty) throws IOException{
        return getPython().getResults(securityProperty); 
    }
    public void connect(QKAuser other) {
            this.other = other;
            other.other = this;
    }
    
    protected void securityCheck(String[] data) throws IOException, KeyExchangeFailure {
        //python returns whether they are equal or not
        String[] sC = getPython().getResults(data[0], data[1], data[2], data[3]);
        //System.out.println("dS and bi_dS equal: " + sC[0]);
        //System.out.println("dC and bi_dC equal: " + sC[1]);
        //python boolean has capital F, so I don't believe we can transfer it into a java boolean, so I keep it in string form.
        if (sC[0].equals("False")) {
            System.out.println("There has been an eavesdropping. Aborting protocol.");
            throw new KeyExchangeFailure();
        }
        if (sC[1].equals("False")) {
            System.out.println("There has been an eavesdropping. Aborting protocol.");
            throw new KeyExchangeFailure();
        }
    }
    
    protected void makeKey() throws IOException {
        System.out.println("Error. Make a key before encrypting.");
    }
    
    
    protected void makeKey(String[] data) throws IOException {

        key = getPython().getResults(data[0], data[1], data[2], data[3], data[4], data[5], "placeholder");
        System.out.println(key);
        textEncryptor.setPassword(key);
    }
    
    protected boolean keyMade() {
        if (key.equals(""))
            return false;
        else
            return true; 
    }
    
    @Override
    public byte[] encryptMessage(byte[] message) throws EncryptionException {
            return textEncryptor.encrypt(message);     
    }
    @Override
    public byte[] decryptMessage(byte[] encryptedMessage) throws DecryptionException {
            return textEncryptor.decrypt(encryptedMessage);
    }
    
    
    
    
    private static PyScript getPython() throws IOException {
		if (python == null)
			python = new PyScript(SCRIPT_LOCATION, "QiskitEngine");
		return python;
    }
 
}
