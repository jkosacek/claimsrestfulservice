package edu.gvsu.cis.claimsservice.endpoints;

import static com.google.api.server.spi.config.ApiMethod.HttpMethod.GET;
import edu.gvsu.cis.claimsservice.PMF;
import edu.gvsu.cis.claimsservice.model.Note;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

@Api(name = "noteendpoint", namespace = @ApiNamespace(ownerDomain = "gvsu.edu", ownerName = "gvsu.edu", packagePath = "cis.claimsservice.model"))
public class NoteEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listNote")
	public CollectionResponse<Note> listNote(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Note> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Note.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Note>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Note obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Note> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@SuppressWarnings("unchecked")
	@ApiMethod(name = "getNotesForClaim")
	public List<Note> getNotesForClaim(@Named("claimId") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Query q = mgr.newQuery(Note.class);
		q.setFilter("claimId == claimIdParam");
		q.setOrdering("createDate desc");
		q.declareParameters("String claimIdParam");
		List<Note> notes = new ArrayList<Note>();
		try {
			notes = (List<Note>) q.execute(id);
		} finally {
			mgr.close();
		}
		return notes;
	}
	
	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getNote")
	public Note getNote(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Note note = null;
		try {
			note = mgr.getObjectById(Note.class, id);
		} finally {
			mgr.close();
		}
		return note;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param note the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertNote")
	public Note insertNote(Note note) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (note.getId() != null && containsNote(note)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.makePersistent(note);
		} finally {
			mgr.close();
		}
		return note;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param note the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateNote")
	public Note updateNote(Note note) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsNote(note)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(note);
		} finally {
			mgr.close();
		}
		return note;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeNote")
	public void removeNote(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Note note = mgr.getObjectById(Note.class, id);
			mgr.deletePersistent(note);
		} finally {
			mgr.close();
		}
	}

	private boolean containsNote(Note note) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Note.class, note.getId());
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
