package uva.cs.auditeur.cloud;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;



public final class Utility {
		//should be handled by the client in the future
		 static public ArrayList<String> makeTagList(String tags){
			String delim = ",";
			String[] splitTags = tags.split(delim);
			ArrayList<String> tagList = new ArrayList<String>();
			for(int i =0; i < splitTags.length; i++){
				tagList.add(splitTags[i].trim());
			}
			return tagList;
		}
		 
		 
		 public static byte[] readBlob(BlobKey blobKey){
			  
			 BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
			 BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
			  
			 if( blobInfo == null )
			  return null;
			  
			 if( blobInfo.getSize() > Integer.MAX_VALUE )
			  throw new RuntimeException("This method can only process blobs up to " + Integer.MAX_VALUE + " bytes");
			  
			 int blobSize = (int)blobInfo.getSize();
			 int chunks = (int)Math.ceil(((double)blobSize / BlobstoreService.MAX_BLOB_FETCH_SIZE));
			 int totalBytesRead = 0;
			 int startPointer = 0;
			 int endPointer;
			 byte[] blobBytes = new byte[blobSize];
			  
			 for( int i = 0; i < chunks; i++ ){

				 endPointer = Math.min(blobSize - 1, startPointer + BlobstoreService.MAX_BLOB_FETCH_SIZE - 1);

				 byte[] bytes = blobstoreService.fetchData(blobKey, startPointer, endPointer);

				 for( int j = 0; j < bytes.length; j++ )
					 blobBytes[j + totalBytesRead] = bytes[j];

				 startPointer = endPointer + 1;
				 totalBytesRead += bytes.length;
			 }
//			 System.out.println("byte length: "+blobBytes.length);
			 return blobBytes;
			}
		 
		 
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
		 
	

	private static void main(String[] args) throws IOException {
		String str = "hello i'm kearn\nI'm living in Charlottesville\nim computer science major";
		System.out.println(str);
		StringBuilder sb = new StringBuilder();
		BufferedReader br = byteArray2Reader(str.getBytes());
		String strLine;
		while((strLine = br.readLine()) != null){
			sb.append("+1 " + strLine +"\n");
		}
		
		System.out.println(sb.toString());
		

	}

}
