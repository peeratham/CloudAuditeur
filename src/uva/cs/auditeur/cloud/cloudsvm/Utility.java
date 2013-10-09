package uva.cs.auditeur.cloud.cloudsvm;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;


public class Utility {
	
	public static BufferedReader byteArray2Reader(byte[] array){
		// Utility function to convert byte array of data to buffered reader
		InputStream is = null;
        BufferedReader bfReader = null;
        try{
        	is = new ByteArrayInputStream(array);
        	bfReader = new BufferedReader(new InputStreamReader(is));
        } finally {
        	try{
        		if(is != null) is.close();
        	} catch(Exception ex){
        		
        	}
        }
        return bfReader;
	}

}
