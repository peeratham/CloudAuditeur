package uva.cs.auditeur.cloud;


import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uva.cs.auditeur.cloud.cloudsvm.*;

import com.google.appengine.api.blobstore.BlobKey;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;


@SuppressWarnings("serial")
public class DataPrepServlet extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		//parsing positive/negative dataset
		BufferedReader buf = req.getReader();
		String strLine;
		List<BlobKey> positiveSampleKeys = new ArrayList<BlobKey>();
		List<BlobKey> negativeSampleKeys = new ArrayList<BlobKey>();
		List<BlobKey> pointer = null;
		Key entityKey = KeyFactory.stringToKey(buf.readLine());

		
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
		
		//now scaling
		String[] args = new String[]{"-l","-1","-u","1"};	//default scaling parameter range (-1,1)
		svm_scale s = new svm_scale();
		s.setData(merge.toString().getBytes());
		s.cloud_run(args);
		
//		need to save the range to blobstore
		
		//now gridsearching for best C and gamma
		// grid search range for C and gamma
		double c_begin = -5;
    	double c_end = 15;
    	double c_step = 3;
    	
    	double g_begin = 3;
    	double g_end = -15;
    	double g_step = -3;
    	
    	//default gridsearch parameter, cross-validation 5 folds and quiet mode(no print)
    	args = new String[]{"-v","5","-q"};		
    	svm_train t = new svm_train();
    	t.setData(s.get_scaled_features());
		t.initialize(args);
		
		GridSearch grid = new GridSearch(); 
		grid.gridRange(c_begin,c_end,c_step,g_begin,g_end,g_step);
		grid.search(t);
		
		// now train the model
		t.setParam(grid.getBestC(), grid.getBestGamma());
		byte[] model_bytes = t.cloud_run();
		
		//save the model
		//session key, range, model, cross validation accuracy
		 //save file to BlobStore
	      //Get a file service
	      FileService fileService = FileServiceFactory.getFileService();

	     
	      AppEngineFile file = fileService.createNewBlobFile("application/octet-stream","model");
	      // Open a channel to write to it
	      boolean lock = true;
	      FileWriteChannel writeChannel = fileService.openWriteChannel(file, lock);
	     
	      //This time we write to the channel directly
	      writeChannel.write(ByteBuffer.wrap(model_bytes));
	      //Now finalize
	      writeChannel.closeFinally();
	      
	      // save blob key of feature file to datastore
	      BlobKey modelKey  = fileService.getBlobKey(file);
	      
	      DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
	      //create new entity
	      
	      //save
	      
	      Entity userModel = null;
			try {
				userModel = ds.get(entityKey);
			} catch (EntityNotFoundException e) {
				e.printStackTrace();
			}
	      userModel.setProperty("modelKey", modelKey);
	      ds.put(userModel);
	      
	      //bring user to view model page
	      RequestDispatcher jsp = req.getRequestDispatcher("WEB-INF/view-models.jsp");
//	      req.setAttribute("userEmail", userModel.getProperty("userEmail")); 
	      jsp.forward(req, resp);
	      
	}
}
