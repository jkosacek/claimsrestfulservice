package edu.gvsu.cis.claimsservice.model;

import edu.gvsu.cis.claimsservice.PMF;

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

@Api(name = "claimendpoint", namespace = @ApiNamespace(ownerDomain = "gvsu.edu", ownerName = "gvsu.edu", packagePath = "cis.claimsservice.model"))
public class ClaimEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listClaim")
	public CollectionResponse<Claim> listClaim(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Claim> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Claim.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Claim>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Claim obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Claim> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getClaim")
	public Claim getClaim(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Claim claim = null;
		try {
			claim = mgr.getObjectById(Claim.class, id);
		} finally {
			mgr.close();
		}
		return claim;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param claim the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertClaim")
	public Claim insertClaim(Claim claim) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (claim.getId() != null) {
				if (containsClaim(claim)) {
					throw new EntityExistsException("Object already exists");
				}
			}
			mgr.makePersistent(claim);
		} finally {
			mgr.close();
		}
		return claim;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param claim the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateClaim")
	public Claim updateClaim(Claim claim) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsClaim(claim)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(claim);
		} finally {
			mgr.close();
		}
		return claim;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeClaim")
	public void removeClaim(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Claim claim = mgr.getObjectById(Claim.class, id);
			mgr.deletePersistent(claim);
		} finally {
			mgr.close();
		}
	}

	private boolean containsClaim(Claim claim) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Claim.class, claim.getId());
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
