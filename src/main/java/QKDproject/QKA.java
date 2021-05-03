package QKDproject;
import QKDproject.exception.KeyExchangeFailure;
import java.io.File;
import java.io.IOException;


/**
 *
 * @author Raphael
 * simulates the CommunicationChannel
 */
public class QKA {
    private static String SCRIPT_LOCATION;
    static {
            try {
                    SCRIPT_LOCATION = new File(".").getCanonicalPath() + File.separatorChar + "qkaImplementationFINAL.py";
            } catch (IOException e) {
                    SCRIPT_LOCATION = "error getting script location";
            }
    }
    
    private static PyScript python;

    //private QKAuser Eve;
    private boolean isEve;
    double securityProperty;
    public QKA (boolean isEve, double securityProperty) {
        this.isEve = isEve;
        this.securityProperty = securityProperty;
    }
    
    public void makeKey(QKAuser Alice, QKAuser Bob) throws IOException, KeyExchangeFailure {
        /*
        in Alice's POV
        GiveData: Gives her own key, decoy bits, and everything else
        ReceiveData1: takes Bob's GiveData to form dS, dC, S_, C. Gives dS, dC to Bob for security check
        ReceiveData2: takes Bob's dS, dC for security check with own bi_, then forms key if passed
        Same for Bob
        */
        String[] AliceGetData = Alice.getData(securityProperty);
        String[] BobGetData = Bob.getData(securityProperty);
        
        /*
        this is GiveData;
        print(user.Kkey)
        print(user.bi_dS)
        print(user.bi_dC)
    R1    print(json_strS__)
    R1    print(json_strC_)
    R1    print(user.pos_dS)
    R1    print(user.pos_dC)
    R1    print(user.ba_dS)
    R1    print(user.ba_dC)
        print(user.seed)
        print(user.bS)
        print(user.bH)
        
        these are ReceiveData 1&2:
        ReceiveData1(S__, C_, pos_dS, pos_dC, ba_dS, ba_dC, backend) denoted R1
        securityCheck(d, bi_d) x2
        makeKey(ownKkey, S_, C, seed, bS, bH, backend) denoted mK
        */
        //GetData is split into two, one is to keep, the other is for R1
        //(Alice/Bob Data for ReceiveData 1)
        String[][] ADR1 = splitGetData(BobGetData);
        String[] BobKeptData = ADR1[0];
        String[] AR1Data = ADR1[1];
        //maybe some mistake above or below
        String[][] BDR1 = splitGetData(AliceGetData);
        String[] AliceKeptData = BDR1[0];
        String[] BR1Data = BDR1[1];
        
        //so now Alice and Bob each have data necessary for R1
        //oh no, interception! Here, Eve intercepts S__ and C_ as it is the only thing she can recognize

        if (isEve) {
            String[] AliceData = {AR1Data[0], AR1Data[1]};
            String[] eveAliceData = intercept(AliceData);
            String[] BobData = {BR1Data[0], BR1Data[1]};
            String[] eveBobData = intercept(BobData);
            
            for (int i = 0; i < 2; i++) {
                AR1Data[i] = eveAliceData[i];
                BR1Data[i] = eveBobData[i];
            }
        }
        
        //Alice.makeKey(BobData); 
        //Bob.makeKey(AliceData);
        //do R1. Alice takes Bob's data. Bob takes Alice's data
        String[] AR1 = getPython().getResults(AR1Data[0], AR1Data[1], AR1Data[2], AR1Data[3], AR1Data[4], AR1Data[5]);
        String[] BR1 = getPython().getResults(BR1Data[0], BR1Data[1], BR1Data[2], BR1Data[3], BR1Data[4], BR1Data[5]);
            
        
        /*
        so Alice now has Bob's decoy measurements, and Bob has Alice's decoy measurements (plus lists without decoys)
        Alice keeps Bob's lists without decoys, but gives Bob his decoy measurements so he can check if eavesdropping. Vice versa in opposite POV.
        format of A/BR1: 
        dS
        dC
        S_
        C
        */
            
        String[] AsC = new String[4];
        String[] BsC = new String[4];
        //Format of securityCheck
        //dS, dC, bi_dS, bi_dC so x2
        AsC[0] = BR1[0];
        AsC[1] = BR1[1];
        AsC[2] = AliceKeptData[1];
        AsC[3] = AliceKeptData[2];
        
        BsC[0] = AR1[0];
        BsC[1] = AR1[1];
        BsC[2] = BobKeptData[1];
        BsC[3] = BobKeptData[2];
        
        Alice.securityCheck(AsC);
        Bob.securityCheck(BsC);
        
        //Format of makeKey
        //kkey, S_, C, seed, bS, bH
        String[] AmK = new String[6];
        String[] BmK = new String[6];
        
        AmK[0] = AliceKeptData[0];
        AmK[1] = AR1[2];
        AmK[2] = AR1[3];
        AmK[3] = BobGetData[9];
        AmK[4] = BobGetData[10];
        AmK[5] = BobGetData[11];
        
        BmK[0] = BobKeptData[0];
        BmK[1] = BR1[2];
        BmK[2] = BR1[3];
        BmK[3] = AliceGetData[9];
        BmK[4] = AliceGetData[10];
        BmK[5] = AliceGetData[11];
                
        Alice.makeKey(AmK);
        Bob.makeKey(BmK);
  
    }
    

    private String[][] splitGetData(String[] getData) {
        String[][] s = new String[2][];
        String[] keptData = new String[3];
        for (int i = 0; i < keptData.length; i++) {
            keptData[i] = getData[i];
        }
        
        String[] R1Data = new String[6];
        for (int i = 0; i < R1Data.length; i++) {
            R1Data[i] = getData[i+3];
        }
        s[0] = keptData;
        s[1] = R1Data;
        return s;
    }
        
    //Oh no! Eve intercepts the messages that have been shared of QKA, the simulated QKA communication channel
    protected String[] intercept(String[] data) throws IOException {
        String[] out = getPython().getResults(data[0], data[1]); 
        return out;
    }
    
    
    private static PyScript getPython() throws IOException {
		if (python == null)
			python = new PyScript(SCRIPT_LOCATION, "QiskitEngine");
		return python;
	}
    
}
