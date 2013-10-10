package uva.cs.auditeur.cloud;
import java.util.logging.Logger;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
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
public class FeatureExtractServlet extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		final Logger log = Logger.getLogger(FeatureExtractServlet.class.getName());
		BlobstoreService bs = BlobstoreServiceFactory.getBlobstoreService();
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		BlobInfoFactory blobInfoFactory = new BlobInfoFactory();
		
		
		
		
		Key entityKey = KeyFactory.stringToKey(req.getParameter("entity_key"));
        Entity userUpload = null;
		try {
			userUpload = ds.get(entityKey);
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		}
		BlobKey blobKey = (BlobKey) userUpload.getProperty(new String("upload"));

		//Extract wav feature
        WavFeature wf = new WavFeature(blobKey);
        
        
      //save file to BlobStore
      //Get a file service
      FileService fileService = FileServiceFactory.getFileService();
      //Create a new Blob file with mime-type "text/plain"
      BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);
      AppEngineFile file = fileService.createNewBlobFile("text/plain", blobInfo.getFilename());
      // Open a channel to write to it
      boolean lock = true;
      FileWriteChannel writeChannel = fileService.openWriteChannel(file, lock);
       
      //This time we write to the channel directly
      writeChannel.write(ByteBuffer.wrap(wf.getAudioFeature().toString().getBytes()));
      //Now finalize
      writeChannel.closeFinally();
      
      // save blob key of feature file to datastore
      BlobKey featureKey  = fileService.getBlobKey(file);
      userUpload.setProperty("featureKey", featureKey);
      ds.put(userUpload);
      
      

	}

}
