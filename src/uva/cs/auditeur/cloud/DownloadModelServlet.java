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
			String userModelKeyStr = req.getParameter("key");
			Entity userModel = null;
			//Get BlobKey for feature file
			BlobKey modelBlobKey = null;
//			
			if (userModelKeyStr != null) {
				try {
					userModel = ds.get(KeyFactory.stringToKey(userModelKeyStr)); 
//					if (userModel.getProperty("user").equals(user)) {	//check if the same user
						modelBlobKey = (BlobKey)userModel.getProperty("modelKey");
//					}
				} catch (EntityNotFoundException e) { 
					// Leave blobKey null.
					} 
			}
			BlobInfoFactory blobInfoFactory = new BlobInfoFactory();
			BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(modelBlobKey);
			//encode time_creation property
			System.out.println(blobInfo.getCreation().toString());
			String time_creation = URLEncoder.encode(blobInfo.getCreation().toString(), "UTF-8");
			
			if(modelBlobKey != null) {
				resp.setContentType("application/octet-stream");
				resp.setHeader("Content-Disposition", "attachment; filename=\"" +"model_"+time_creation+"\"");
//				
				bs.serve(modelBlobKey, resp);
			}
			else { 
				resp.sendError(404);
			}

			
			
		}

}
