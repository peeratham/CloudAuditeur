package uva.cs.auditeur.cloud;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public class ViewModelServlet extends HttpServlet {
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		//give the model to the user
	DatastoreService ds = DatastoreServiceFactory.getDatastoreService(); 
	BlobInfoFactory blobInfoFactory = new BlobInfoFactory();
	
	
	UserService userService = UserServiceFactory.getUserService(); 
	User user = userService.getCurrentUser();
	String userEmail = user.getEmail();
	List<Map<String, Object>> modelList = new ArrayList<Map<String, Object>>();
	Key UserModelGroupKey = KeyFactory.createKey("UserModelGroup", userEmail);
	Query q = new Query("UserModel").setAncestor(UserModelGroupKey); 
	PreparedQuery pq = ds.prepare(q);
	Iterable<Entity> results = pq.asIterable();
	//entry key, range key, model key, cross validation accuracy
	for (Entity result : results) {
		Map<String, Object> modelInfo = new HashMap<String, Object>(); 
		modelInfo.put("rangeKey", "rangeKey");
		modelInfo.put("modelKey", KeyFactory.keyToString(result.getKey()));
		System.out.println(KeyFactory.keyToString(result.getKey()));
		modelInfo.put("accuracy", 99.99); 
		//put tag info of each model here
		modelInfo.put("lookfor", (ArrayList<String>) result.getProperty(("lookfor")));
		modelInfo.put("within", (ArrayList<String>) result.getProperty(("within")));
		modelList.add(modelInfo);
	}
	req.setAttribute("models", modelList); 
	req.setAttribute("hasModels", !modelList.isEmpty());
	req.setAttribute("userEmail", userEmail);


	
	resp.setContentType("text/html");
	RequestDispatcher jsp = req.getRequestDispatcher("WEB-INF/view-models.jsp");
	jsp.forward(req, resp);
	
	}

}
