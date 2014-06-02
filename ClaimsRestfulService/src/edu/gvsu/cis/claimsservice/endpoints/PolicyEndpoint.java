package edu.gvsu.cis.claimsservice.endpoints;

import edu.gvsu.cis.claimsservice.PMF;
import edu.gvsu.cis.claimsservice.model.Policy;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

@Api(name = "policyendpoint", namespace = @ApiNamespace(ownerDomain = "gvsu.edu", ownerName = "gvsu.edu", packagePath = "cis.claimsservice.model"))
public class PolicyEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listPolicy")
	public CollectionResponse<Policy> listPolicy(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Policy> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Policy.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Policy>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Policy obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Policy> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getPolicy")
	public Policy getPolicy(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Policy policy = null;
		try {
			policy = mgr.getObjectById(Policy.class, id);
		} finally {
			mgr.close();
		}
		return policy;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param policy the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertPolicy")
	public Policy insertPolicy(Policy policy) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (policy.getId() != null && containsPolicy(policy)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(policy);
		} finally {
			mgr.close();
		}
		return policy;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param policy the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updatePolicy")
	public Policy updatePolicy(Policy policy) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsPolicy(policy)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(policy);
		} finally {
			mgr.close();
		}
		return policy;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removePolicy")
	public void removePolicy(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Policy policy = mgr.getObjectById(Policy.class, id);
			mgr.deletePersistent(policy);
		} finally {
			mgr.close();
		}
	}

	private boolean containsPolicy(Policy policy) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Policy.class, policy.getId());
		} catch (javax.jdo.JDOObjectNotFoundException ex) {
			contains = false;
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static PersistenceManager getPersistenceManager() {
		return PMF.get().getPersistenceManager();
	}

}
