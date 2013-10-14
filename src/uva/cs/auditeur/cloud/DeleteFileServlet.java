package uva.cs.auditeur.cloud;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

@SuppressWarnings("serial")
public class DeleteFileServlet  extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService(); 
		BlobstoreService bs = BlobstoreServiceFactory.getBlobstoreService();
		String[] entityKeys = req.getParameterValues("delete");
		String kind = req.getParameter("kind");
		List<Key> keysToDelete = new ArrayList<Key>();
		BlobKey blobKey = null;
		Key blobInfoKey = null;
		if (entityKeys != null) {
			if(kind.equals("soundlet")){
				for (String entityKey : entityKeys) { 
					try {
						Entity entity = ds.get(KeyFactory.stringToKey(entityKey));
						
							//delete featureKey
							blobKey = (BlobKey)entity.getProperty("featureKey");
							blobInfoKey = KeyFactory.createKey(BlobInfoFactory.KIND, blobKey.getKeyString());
							keysToDelete.add(blobInfoKey);
							keysToDelete.add(entity.getKey());
							//also delete sound file
							blobKey = (BlobKey)entity.getProperty("upload");
							blobInfoKey = KeyFactory.createKey(BlobInfoFactory.KIND, blobKey.getKeyString());
							keysToDelete.add(blobInfoKey);
							keysToDelete.add(entity.getKey());	//and finally delete entity

						} catch (EntityNotFoundException e) { 
						// Do nothing.
						}
				}
			}
			//need to make it more concise
			if(kind.equals("model")){
				for (String entityKey : entityKeys) { 
					try {
						Entity entity = ds.get(KeyFactory.stringToKey(entityKey));
						
							//delete model key
							blobKey = (BlobKey)entity.getProperty("modelKey");
							blobInfoKey = KeyFactory.createKey(BlobInfoFactory.KIND, blobKey.getKeyString());
							keysToDelete.add(blobInfoKey);
							keysToDelete.add(entity.getKey()); //and finally delete the entity itself
							//also delete range key
							blobKey = (BlobKey)entity.getProperty("rangeKey");
							blobInfoKey = KeyFactory.createKey(BlobInfoFactory.KIND, blobKey.getKeyString());
							keysToDelete.add(blobInfoKey);
							keysToDelete.add(entity.getKey());	//and finally delete the entity itself

						} catch (EntityNotFoundException e) { 
						// Do nothing.
						}
				}
			}

			ds.delete(keysToDelete.toArray(new Key[0]));
			if(kind.equals("soundlet"))
				resp.sendRedirect("/");
			else if(kind.equals("model"))
				resp.sendRedirect("/view-models");
			else
				resp.sendRedirect("/");
			}
		}
}