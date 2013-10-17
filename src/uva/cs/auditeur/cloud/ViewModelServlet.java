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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@SuppressWarnings("serial")
public class ViewModelServlet extends HttpServlet {
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		//give the model to the user
	DatastoreService ds = DatastoreServiceFactory.getDatastoreService(); 
	BlobInfoFactory blobInfoFactory = new BlobInfoFactory();
	System.out.println(req.getServerName());
	System.out.println("hello");
	
	UserService userService = UserServiceFactory.getUserService(); 
	User user = userService.getCurrentUser();
	String userEmail = user.getEmail();
	List<Map<String, Object>> modelList = new ArrayList<Map<String, Object>>();
	Key UserModelGroupKey = KeyFactory.createKey("UserModelGroup", userEmail);
	Query q = new Query("UserModel").setAncestor(UserModelGroupKey); 
	PreparedQuery pq = ds.prepare(q);
	Iterable<Entity> results = pq.asIterable();
	//entry key, range key, model key, cross validation accuracy
	//output 
	List<AuditeurModel> auditeurModelList = new ArrayList<AuditeurModel>();
	AuditeurModel model;
	String format = req.getParameter("format");
	StringBuilder jsonOutput = new StringBuilder();
	Gson gson = new GsonBuilder().setPrettyPrinting().create();

	
	for (Entity result : results) {
		if(format != null){
			if(format.equals("json")){
			model = new AuditeurModel();
			BlobKey mKey = (BlobKey)result.getProperty("modelKey");
			BlobKey rKey = (BlobKey)result.getProperty("rangeKey");
			model.setModelKey(mKey.getKeyString());
			model.setRangeKey(rKey.getKeyString());
			model.setLookFors((ArrayList<String>) result.getProperty("lookfor"));
			model.setWithins((ArrayList<String>) result.getProperty("within"));
			model.setAccuracy((Double)result.getProperty("accuracy"));
			jsonOutput.append(gson.toJson(model));
			jsonOutput.append("\n");
			}
		}
			
		else{
		Map<String, Object> modelInfo = new HashMap<String, Object>(); 
		modelInfo.put("modelKey", KeyFactory.keyToString(result.getKey()));//not blobkey but entity key that keep model & range blob key
		modelInfo.put("accuracy", String.format("%.2f percent", (Double)result.getProperty("accuracy"))); 
		//put tag info of each model here
		modelInfo.put("lookfor", (ArrayList<String>) result.getProperty(("lookfor")));
		modelInfo.put("within", (ArrayList<String>) result.getProperty(("within")));
		modelList.add(modelInfo);
		}
		
	}

	if(format != null){
		if(format.equals("json")){
		resp.setContentType("application/json");
		resp.getWriter().print(jsonOutput);
		}
	}
	else{
	req.setAttribute("models", modelList); 
	req.setAttribute("hasModels", !modelList.isEmpty());
	req.setAttribute("userEmail", userEmail);


	
	resp.setContentType("text/html");
	RequestDispatcher jsp = req.getRequestDispatcher("WEB-INF/view-models.jsp");
	jsp.forward(req, resp);
	}
	
	}

}
