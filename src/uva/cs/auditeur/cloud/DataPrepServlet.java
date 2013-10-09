package uva.cs.auditeur.cloud;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

public class DataPrepServlet extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		//parsing positive/negative dataset
		BufferedReader buf = req.getReader();
		String strLine;
		List<BlobKey> positiveSampleKeys = new ArrayList<BlobKey>();
		List<BlobKey> negativeSampleKeys = new ArrayList<BlobKey>();
		List<BlobKey> pointer = null;
		while((strLine = buf.readLine()) != null){
			if(strLine.equals("-positive")){
				pointer = positiveSampleKeys;
			}
			else if(strLine.equals("-negative")){
				pointer = negativeSampleKeys;
			}
			else{
				pointer.add(new BlobKey(strLine));
			}
		}
		System.out.println(Arrays.toString(positiveSampleKeys.toArray()));
		System.out.println(Arrays.toString(negativeSampleKeys.toArray()));
		
		
		//add label and merge samples together
		StringBuilder merge = new StringBuilder();
		for(BlobKey key : positiveSampleKeys){
			byte[] featureData = Utility.readBlob(key);
			BufferedReader br = Utility.byteArray2Reader(featureData);
			String line;
			while((line = br.readLine()) != null){
				merge.append("+1 " + line +"\n");
			}
		}
		for(BlobKey key : negativeSampleKeys){
			byte[] featureData = Utility.readBlob(key);
			BufferedReader br = Utility.byteArray2Reader(featureData);
			String line;
			while((line = br.readLine()) != null){
				merge.append("-1 " + line +"\n");
			}
		}
//		merge.toString().getBytes()
		
		
		//now scaling
		
	}
}
