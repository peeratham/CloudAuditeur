package uva.cs.auditeur.cloud;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class DataQueryServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		
		
		req.setAttribute("user", user); 
		resp.setContentType("text/html");
		RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/prepare.jsp");
		jsp.forward(req, resp);
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		BlobInfoFactory blobInfoFactory = new BlobInfoFactory();
		//get tags from the request
		String lookfor = req.getParameter("lookfor");
		String within = req.getParameter("within");
		String userEmail = req.getParameter("userEmail");
		
		//prepare query
		ArrayList<String> lookforTags = Utility.makeTagList(lookfor);
		ArrayList<String> withinTags = Utility.makeTagList(within);
		
		//initial save model to the datastore
		 //save
	      Key UserModelGroupKey = KeyFactory.createKey("UserModelGroup", userEmail);
	      Entity UserModel = new Entity("UserModel", UserModelGroupKey);
	      UserModel.setProperty("userEmail", userEmail);	//uniquely id user by email
	      UserModel.setProperty("lookfor",lookforTags);
	      UserModel.setProperty("within",withinTags);
	      ds.put(UserModel);
		
		
		//doing the query based on tags
		List<Map<String, Object>> soundletList = new ArrayList<Map<String, Object>>();
		Key userGroupKey = KeyFactory.createKey("UserUploadGroup", userEmail);
		

		
		/////////BUILD WITHIN QUERY FIRST
		List<Filter> withinFilterPredList = new ArrayList<Filter>();
		for (String tag : withinTags) {
			withinFilterPredList.add(new FilterPredicate("tags", FilterOperator.EQUAL, tag));
		}
		Filter withinFilter = null;
		try{
			withinFilter = CompositeFilterOperator.or(withinFilterPredList);
		} catch (Exception e) {
			withinFilter = withinFilterPredList.get(0);
		}
		//For negative dataset
		Query withinQuery = new Query("UserUpload").setAncestor(userGroupKey)
						.addProjection(new PropertyProjection("featureKey", BlobKey.class));
		withinQuery.setFilter(withinFilter);
		PreparedQuery pq = ds.prepare(withinQuery);
		Iterable<Entity> results = pq.asIterable();
			
		Set<BlobKey> negativeDataSet = new HashSet<BlobKey>();
		for (Entity result : results) {
			BlobKey feature_blobKey = (BlobKey) result.getProperty("featureKey");
			negativeDataSet.add(feature_blobKey);
		}
		

		//////BUILD LOOKFOR FILTER

		

		List<Filter> lookforFilterPredList = new ArrayList<Filter>();
		for (String tag : lookforTags) {
			lookforFilterPredList.add(new FilterPredicate("tags", FilterOperator.EQUAL, tag));
		}
		Filter lookforFilter = null;
		try{
			lookforFilter = CompositeFilterOperator.and(lookforFilterPredList);
		} catch (Exception e) {
			lookforFilter = lookforFilterPredList.get(0);
		}
		
		// positiveSet = (lookfor1 AND lookfor2) AND (within1 OR within2 OR ...)
		lookforFilter = new CompositeFilter(CompositeFilterOperator.AND, Arrays.asList(lookforFilter,withinFilter));
		Query lookforQuery = new Query("UserUpload").setAncestor(userGroupKey)
				.addProjection(new PropertyProjection("featureKey", BlobKey.class));
		lookforQuery.setFilter(lookforFilter);
		pq = ds.prepare(lookforQuery);
		
		results = pq.asIterable();
		//For positive dataset
		Set<BlobKey> positiveDataSet = new HashSet<BlobKey>();
		for (Entity result : results) {
			BlobKey feature_blobKey = (BlobKey) result.getProperty("featureKey");
			positiveDataSet.add(feature_blobKey);
		}
		
//		System.out.println("positive");
//		for(BlobKey bk: positiveDataSet){
//			System.out.println(bk.toString());
//		}
//		
//		System.out.println("negative");
//		for(BlobKey bk: negativeDataSet){
//			System.out.println(bk.toString());
//		}
		
		
	
		negativeDataSet.removeAll(positiveDataSet);	//set difference
	
		//format the key to be parse
		StringBuilder buf = new StringBuilder();

		buf.append(KeyFactory.keyToString(UserModel.getKey())+"\n");	//pass pass entity key of the model training
		
		buf.append("-positive\n");	//mark the beginning of positive samples
		for (BlobKey feature_key : positiveDataSet){
			System.out.println(feature_key);
			buf.append(feature_key.getKeyString());
			buf.append("\n");
		}
		buf.append("-negative\n");	//mark the beginning of negative samples
		for (BlobKey feature_key : negativeDataSet){
			System.out.println(feature_key);
			buf.append(feature_key.getKeyString());
			buf.append("\n");
		}
		System.out.println(buf.toString());
		
		Queue queue = QueueFactory.getQueue("data-preperation-queue");
		TaskOptions taskOptions = TaskOptions.Builder.withUrl("/prepare").method(Method.POST);
		taskOptions.payload(buf.toString());
		queue.add(taskOptions);
		
		resp.sendRedirect("/view-models");
		
	}
}