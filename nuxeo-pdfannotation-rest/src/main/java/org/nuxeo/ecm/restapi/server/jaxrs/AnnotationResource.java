package org.nuxeo.ecm.restapi.server.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.event.DocumentEventCategories;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;
import org.nuxeo.runtime.api.Framework;

import com.google.common.base.Charsets;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@WebObject(type = "annotation")
@Produces(MediaType.APPLICATION_JSON)
public class AnnotationResource extends DefaultObject {

    static final String FORM_XML = "data.xml";

    protected static final Log log = LogFactory.getLog(AnnotationResource.class);

    //
    protected static final String FACET_ANNOTATIONEER = "Annotationeer";

    protected static final String PERMISSION_ANNOTATION = "Annotations";

    protected static final String DEFAULT_XPATH = "file:content";

    //
    protected static final String EVENT_SAVE = "annotationSaved";

    protected static final String EVENT_LOAD = "annotationLoaded";

    @GET
    @Path("{docId}")
    public Response getAnnotations(@PathParam("docId") String docId, @QueryParam("xpath") String xpath) {
        CoreSession session = getContext().getCoreSession();
        DocumentModel doc = session.getDocument(new IdRef(docId));

        if (!doc.hasFacet(FACET_ANNOTATIONEER)) {
            return Response.status(Status.OK).entity(StringUtils.EMPTY).build();
        }

        if (StringUtils.isBlank(xpath)) {
            xpath = DEFAULT_XPATH;
        }

        String annotations = null;
        try {
            annotations = load(doc, session, xpath);
        } catch (Exception e) {
            log.error("Error loading annotations", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        return Response.status(Status.OK).entity(annotations).build();
    }

    @POST
    @Path("{docId}")
    public Response saveAnnotations(@PathParam("docId") String docId, @QueryParam("xpath") String xpath,
            InputStream input) throws Exception {
        CoreSession session = getContext().getCoreSession();
        DocumentModel doc = session.getDocument(new IdRef(docId));

        boolean writable = session.hasPermission(doc.getRef(), SecurityConstants.WRITE);
        if (!writable) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        if (StringUtils.isBlank(xpath)) {
            xpath = DEFAULT_XPATH;
        }

        String annotations = null;
        try {
            String changes = IOUtils.toString(input, Charsets.UTF_8);
            annotations = save(doc, session, xpath, changes);
        } catch (IOException e) {
            log.error("Error saving annotations", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        return Response.status(Status.OK).entity(annotations).build();
    }

    @DELETE
    @Path("{docId}")
    public Response deleteAnnotations(@PathParam("docId") String docId, @QueryParam("xpath") String xpath)
            throws Exception {
        CoreSession session = getContext().getCoreSession();
        DocumentModel doc = session.getDocument(new IdRef(docId));

        boolean writable = session.hasPermission(doc.getRef(), SecurityConstants.WRITE);
        if (!writable) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        if (StringUtils.isBlank(xpath)) {
            xpath = DEFAULT_XPATH;
        }

        String annotations = null;
        try {
            save(doc, session, xpath, null);
        } catch (IOException e) {
            log.error("Error saving annotations", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        return Response.status(Status.OK).entity(annotations).build();
    }

    /**
     * Load the annotations from Document
     *
     * @param doc
     * @param session
     * @param currentUser
     * @return
     * @throws Exception
     */
    protected String load(DocumentModel doc, CoreSession session, String xpath) throws Exception {
        Property target = getTargetProperty(doc, xpath);

        if (target == null) {
            return StringUtils.EMPTY;
        }

        Serializable annotations = target.getValue("content");
        if (annotations == null) {
            return StringUtils.EMPTY;
        }

        boolean writable = session.hasPermission(doc.getRef(), SecurityConstants.WRITE);
        if (!writable) {
            if (!session.hasPermission(doc.getRef(), PERMISSION_ANNOTATION)) {
                annotations = "[]";
            }
        }

        // Configure the response to say that annotation can't be changed by current user
        String json = "{\"settings\":[{\"key\":\"ANNOTATIONS_READ_ONLY\",\"value\":\"" + !writable
                + "\"}],\"annotations\":" + "" + annotations + "}";

        fireEvent(session, doc, EVENT_LOAD);
        return (String) json;
    }

    /**
     * Save annotations method - They will add the Facet Annotation if the document hasn't
     *
     * @param doc
     * @param session
     * @param currentUser
     * @param changedAnnotations
     * @return
     * @throws Exception
     */
    protected String save(DocumentModel doc, CoreSession session, String xpath, String changedAnnotations)
            throws Exception {

        if (!doc.hasFacet(FACET_ANNOTATIONEER)) {
            doc.addFacet(FACET_ANNOTATIONEER);
        }

        Property target = getTargetProperty(doc, xpath);
        if (target == null) {
            Property p = doc.getProperty("annotationeer:items");
            target = p.addEmpty();
        }

        // Check if something exists...
        if (changedAnnotations != null) {
            Serializable savedAnnotations = target.getValue("content");
            if (savedAnnotations != null) {
                changedAnnotations = updateAnnotations(savedAnnotations.toString(), changedAnnotations);
            }
        }

        target.setValue("content", changedAnnotations);
        target.setValue("xpath", xpath);
        doc = session.saveDocument(doc);
        session.save();

        fireEvent(session, doc, EVENT_SAVE);

        return changedAnnotations;
    }

    /**
     * Locate the appropriate property in the list of items;
     * 
     * @param doc
     * @param xpath
     * @return
     */
    protected Property getTargetProperty(DocumentModel doc, String xpath) {
        Property p = doc.getProperty("annotationeer:items");

        Property target = null;
        for (Property child : p.getChildren()) {
            Serializable xp = child.getValue("xpath");
            if (xpath.equals(xp)) {
                target = child;
                break;
            }
        }
        return target;
    }

    /**
     * Resolve the update/remove/insert Annotation onto the saved's JSON;
     *
     * @param existentAnnotationJson
     * @param changedAnnotationJSON
     * @return
     */
    protected String updateAnnotations(String existentAnnotationJson, String changedAnnotationJSON) {
        String out = existentAnnotationJson;

        JSONArray arraySaved = JSONArray.fromObject(existentAnnotationJson);
        JSONArray arrayChanged = JSONArray.fromObject(changedAnnotationJSON);
        JSONArray arrayNew = new JSONArray();

        arrayNew.addAll(arraySaved);

        for (int i = 0; i < arrayChanged.size(); i++) {
            JSONObject changed = arrayChanged.getJSONObject(i);

            if ("insert".equalsIgnoreCase(changed.getString("modified"))) {

                insertAnnotation(arrayNew, changed);

            } else if ("delete".equalsIgnoreCase(changed.getString("modified"))) {

                removeAnnotation(arrayNew, changed);

            } else if ("update".equalsIgnoreCase(changed.getString("modified"))) {

                updateAnnotation(arrayNew, changed);

            }
        }

        if (!arrayNew.isEmpty()) {
            out = arrayNew.toString();
        } else {
            out = null;
        }

        return out;

    }

    /**
     * Insert new Annotations to JSONAray
     *
     * @param array
     * @param toInsert
     */
    private void insertAnnotation(JSONArray array, JSONObject toInsert) {
        boolean found = false;
        for (int i = 0; i < array.size(); i++) {
            JSONObject saved = array.getJSONObject(i);
            if (saved.getString("id").equalsIgnoreCase(toInsert.getString("id"))) {
                found = true;
                array.remove(saved);
                array.add(i, toInsert);
                break;
            }
        }

        if (!found) {
            array.add(toInsert);
        }

    }

    /**
     * Remove annotations from JSONArray
     *
     * @param array
     * @param toRemove
     * @return
     */
    private boolean removeAnnotation(JSONArray array, JSONObject toRemove) {
        boolean res = false;
        for (int i = 0; i < array.size(); i++) {
            JSONObject saved = array.getJSONObject(i);
            if (saved.getString("id").equalsIgnoreCase(toRemove.getString("id"))) {
                res = array.remove(saved);
                break;
            }
        }
        return res;
    }

    /**
     * Update the JSONArray add/remove/modify received Annotations
     *
     * @param array
     * @param toUpdate
     * @return
     */
    private boolean updateAnnotation(JSONArray array, JSONObject toUpdate) {
        boolean res = false;
        for (int i = 0; i < array.size(); i++) {
            JSONObject saved = array.getJSONObject(i);
            if (saved.getString("id").equalsIgnoreCase(toUpdate.getString("id"))) {
                res = array.remove(saved);
                array.add(i, toUpdate);
                break;
            }
        }
        return res;
    }

    /**
     * Fire some events for audit/history of document about annotations
     *
     * @param session
     * @param doc
     * @param currentUser
     * @param eventType
     * @throws Exception
     */
    protected void fireEvent(CoreSession session, DocumentModel doc, String eventType) throws Exception {
        try {
            DocumentEventContext ctx = new DocumentEventContext(session, getContext().getPrincipal(), doc);
            ctx.setCategory(DocumentEventCategories.EVENT_DOCUMENT_CATEGORY);
            ctx.setProperty(CoreEventConstants.DOC_LIFE_CYCLE, doc.getCurrentLifeCycleState());
            ctx.setProperty(CoreEventConstants.SESSION_ID, doc.getSessionId());
            Event event = ctx.newEvent(eventType);

            EventService evtService = Framework.getService(EventService.class);

            log.debug("Sending scheduled event id=" + event + ", category="
                    + DocumentEventCategories.EVENT_DOCUMENT_CATEGORY);

            evtService.fireEvent(event);
        } catch (Exception ex) {
            log.error("Erro ao lancar evento: " + EVENT_SAVE, ex);
        }

    }

}
