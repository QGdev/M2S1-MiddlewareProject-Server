package fr.univnantes.document;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class used to manage documents
 * <p>
 *     This class is used to create, get and remove documents
 *     It is a singleton
 * </p>
 */
public class DocumentManager {

    private static final AtomicReference<DocumentManager> instance = new AtomicReference<>(null);
    private final ConcurrentHashMap<UUID, Document> documents;


    /**
     * Creates a new document manager
     */
    private DocumentManager() {
        documents = new ConcurrentHashMap<java.util.UUID, Document>();
    }

    /**
     * Returns the instance of the document manager
     * Creates it if it does not exist
     *
     * @return  The instance of the document manager
     */
    public static DocumentManager getInstance() {
        if (instance.get() == null) {
            synchronized (DocumentManager.class) {
                instance.compareAndSet(null, new DocumentManager());
            }
        }
        return instance.get();
    }

    /**
     * Returns the document with the given UUID
     *
     * @param documentId    The UUID of the document
     * @return              The document with the given UUID
     */
    public Document getDocument(UUID documentId) {
        return documents.get(documentId);
    }

    /**
     * Creates a new document with the given name
     *
     * @param name  The name of the document
     * @return      The created document
     */
    public Document createDocument(String name) {
        Document document = new Document(name);
        documents.put(document.getUUID(), document);
        return document;
    }

    /**
     * Removes the document with the given UUID
     *
     * @param documentId    The UUID of the document
     * @return              True if the document was removed, false otherwise
     */
    public boolean removeDocument(UUID documentId) {
        return documents.remove(documentId) != null;
    }

}
