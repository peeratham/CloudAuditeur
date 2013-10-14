package uva.cs.auditeur.cloud;

import java.io.IOException;
import java.net.URLEncoder;

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
public class DownloadModelServlet  extends HttpServlet {
		public void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws IOException {
//			UserService userService = UserServiceFactory.getUserService();
//			User user = userService.getCurrentUser();
			DatastoreService ds = DatastoreServiceFactory.getDatastoreService(); 
			BlobstoreService bs = BlobstoreServiceFactory.getBlobstoreService();
			String entityKeyStr = req.getParameter("key");
			String kind = req.getParameter("kind");
			Entity userModel = null;
			//Get BlobKey for the requested file
			BlobKey blobKey = null;
			String prefix = "";
			
			if (entityKeyStr != null) {
				try {
					userModel = ds.get(KeyFactory.stringToKey(entityKeyStr)); 
					if(kind.equals("model")){
						blobKey = (BlobKey)userModel.getProperty("modelKey");
						prefix = "model";
					}
					if(kind.equals("range")){
						blobKey = (BlobKey)userModel.getProperty("rangeKey");
						prefix = "range";
					}
					if(kind.equals("feature")){
						blobKey = (BlobKey)userModel.getProperty("featureKey");
						prefix = "feature";
					}
				} catch (EntityNotFoundException e) { 
					// Leave blobKey null.
					} 
			}
			BlobInfoFactory blobInfoFactory = new BlobInfoFactory();
			BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);
			//encode time_creation property
			System.out.println(blobInfo.getCreation().toString());
			String time_creation = URLEncoder.encode(blobInfo.getCreation().toString(), "UTF-8");
			
			if(blobKey != null) {
				resp.setContentType("application/octet-stream");
				resp.setHeader("Content-Disposition", "attachment; filename=\"" +prefix+"_"+time_creation+"\"");
//				
				bs.serve(blobKey, resp);
			}
			else { 
				resp.sendError(404);
			}
		}

}
