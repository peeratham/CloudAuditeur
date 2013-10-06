package uva.cs.auditeur.cloud;


import java.io.IOException;
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
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
@SuppressWarnings("serial")
public class ViewUploadServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService(); 
		BlobstoreService bs = BlobstoreServiceFactory.getBlobstoreService();
		String uploadKeyStr = req.getParameter("key");
		Entity userUpload = null;
		//Get BlobKey for feature file
		BlobKey featureBlobKey = null;
		
		if (uploadKeyStr != null) {
			try {
				userUpload = ds.get(KeyFactory.stringToKey(uploadKeyStr)); 
				if (userUpload.getProperty("user").equals(user)) {
					featureBlobKey = (BlobKey)userUpload.getProperty("featureKey");
					
				}
			} catch (EntityNotFoundException e) { 
				// Leave blobKey null.
				} 
		}
		BlobInfoFactory blobInfoFactory = new BlobInfoFactory();
		BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(featureBlobKey);
		if(featureBlobKey != null) {
//			resp.setContentType("text/plain");
			resp.setHeader("Content-Disposition", "attachment; filename=\"" +"feature_"+blobInfo.getFilename() +"\"");
			
			bs.serve(featureBlobKey, resp);
		}
		else { resp.sendError(404);
		}
		
		
	} 
}