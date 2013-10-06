
package uva.cs.auditeur.cloud;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;



@SuppressWarnings("serial")
public class UploadServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		
				//when the user hit /upload, we print out the upload url that can be used to upload file with post request
		//creating an upload url that can be used to direct the post request to this servlet '/upload'
		//after the post request get submitted, in the background, the content will get saved to the blobstore.
		//the blobstore rewrite the post request with blobkey and send back to this servlet
		//which post method below can handle
	

		resp.setContentType("text/html");
		RequestDispatcher jsp = req.getRequestDispatcher("/service.jsp");
	
		jsp.forward(req, resp);
		
		
	}
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService(); 
		BlobstoreService bs = BlobstoreServiceFactory.getBlobstoreService();
		
		Map<String, List<BlobKey>> blobFields = bs.getUploads(req);
		List<BlobKey> blobKeys = blobFields.get("upload");
		Key userGroupKey = KeyFactory.createKey("UserUploadGroup", user.getEmail());
		Entity userUpload = new Entity("UserUpload", userGroupKey);
		userUpload.setProperty("user", user);
		userUpload.setProperty("upload", blobKeys.get(0));
		//Add Tags
		ArrayList<String> tags = Utility.makeTagList(req.getParameter("tags"));
		userUpload.setProperty("tags", tags);
		ds.put(userUpload);
		
		//extract feature in the background
		Queue defaultQueue = QueueFactory.getDefaultQueue();
		defaultQueue.add(TaskOptions.Builder.withUrl("/extract")
				.param("entity_key", KeyFactory.keyToString(userUpload.getKey()))
				.param("blob_key", blobKeys.get(0).getKeyString()));
		
		
		resp.sendRedirect("/");
		
	}
}
