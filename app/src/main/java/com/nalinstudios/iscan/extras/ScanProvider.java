package com.nalinstudios.iscan.extras;

import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Point;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.DocumentsProvider;
import android.webkit.MimeTypeMap;

import com.nalinstudios.iscan.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Manages documents and exposes them to the Android system for sharing.
 *
 * @author Nalin Angrish.
 */
public class ScanProvider extends DocumentsProvider {

    /** Use these as the default columns to return information about a root if no specific columns are requested in a query. */
    private static final String[] DEFAULT_ROOT_PROJECTION = new String[]{
            Root.COLUMN_ROOT_ID,
            Root.COLUMN_MIME_TYPES,
            Root.COLUMN_FLAGS,
            Root.COLUMN_ICON,
            Root.COLUMN_TITLE,
            Root.COLUMN_DOCUMENT_ID
    };

    /** Use these as the default columns to return information about a document if no specific columns are requested in a query. */
    private static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE
    };


    /** Maximum results to show on searching */
    private static final int MAX_SEARCH_RESULTS = 20;
    /** ROOT_ID for the Root directory */
    private static final String ROOT = "root";
    /** The base Directory */
    private File mBaseDir;


    /**
     * Initial function to set the base directory for the provider.
     * @return true
     */
    @Override
    public boolean onCreate() {
        mBaseDir = getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        return true;
    }


    /**
     * A function to get all root directories defined for the app (Only one as of now.)
     * @param projection The base projection to be used by the Cursor
     * @return A Cursor containing the required data
     */
    @Override
    public Cursor queryRoots(String[] projection) {
        final MatrixCursor result = new MatrixCursor(resolveRootProjection(projection));
        final MatrixCursor.RowBuilder row = result.newRow();

        row.add(Root.COLUMN_ROOT_ID, ROOT);
        row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_SEARCH);
        row.add(Root.COLUMN_TITLE, getContext().getString(R.string.app_name));
        row.add(Root.COLUMN_DOCUMENT_ID, getDocIdForFile(mBaseDir));
        row.add(Root.COLUMN_MIME_TYPES, getAppMimes());
        row.add(Root.COLUMN_ICON, R.mipmap.icon);

        return result;
    }


    /**
     * A function to implement search functionality in the provider
     * @param rootId the ID of the root directory
     * @param query the search query
     * @param projection The base projection to be used by the Cursor
     * @return A Cursor that contains the required data
     * @throws FileNotFoundException -
     */
    @Override
    public Cursor querySearchDocuments(String rootId, String query, String[] projection) throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));
        final File parent = getFileForDocId(rootId);

        final LinkedList<File> pending = new LinkedList<>();
        pending.add(parent);

        while (!pending.isEmpty() && result.getCount() < MAX_SEARCH_RESULTS) {
            final File file = pending.removeFirst();
            if (file.isDirectory()) {
                Collections.addAll(pending, file.listFiles());
            } else {
                if (file.getName().toLowerCase().contains(query)) {
                    includeFile(result, null, file);
                }
            }
        }
        return result;
    }


    /**
     * A function to load the thumbnail for the PDF
     * @param documentId the ID of the file
     * @param sizeHint The Approximation of the size required for the image
     * @param signal a signal to listen for cancellation by the user
     * @return An AssetFileDescriptor describing the thumbnail asset
     * @throws FileNotFoundException -
     */
    @Override
    public AssetFileDescriptor openDocumentThumbnail(String documentId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException {
        final File file = getFileForDocId(documentId);
        String filename = file.getName();
        if(filename.endsWith(".pdf")){
            filename = filename.replace(".pdf", ".jpg");  // If name consists extension, replace it's extension to jpg
        }else{
            filename += ".jpg";  // If name does not contains extension, add jpg extension.
        }
        File imageFile = new File(new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), ".data-internal"), filename);
        final ParcelFileDescriptor pfd = ParcelFileDescriptor.open(imageFile, ParcelFileDescriptor.MODE_READ_ONLY);
        return new AssetFileDescriptor(pfd, 0, AssetFileDescriptor.UNKNOWN_LENGTH);
    }


    /**
     * A function to get the details of the given document
     * @param documentId the ID of the document
     * @param projection The base projection to be used by the Cursor
     * @return A Cursor containing the required data
     * @throws FileNotFoundException -
     */
    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));
        includeFile(result, documentId, null);
        return result;
    }


    /**
     * A function to get the child Documents from the parent folder
     * @param parentDocumentId the ID of the parent folder
     * @param projection The base projection to be used by the Cursor
     * @param sortOrder The order in which to sort (Not used)
     * @return A cursor containing the required data
     * @throws FileNotFoundException -
     */
    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));
        final File parent = getFileForDocId(parentDocumentId);
        for (File file : parent.listFiles()) {
            includeFile(result, null, file);
        }
        return result;
    }


    /**
     * A function to get the file contents of the selected document
     * @param documentId the ID of the document
     * @param mode the Mode in which to open the file
     * @param signal A signal to listen for the cancellation by the user
     * @return A ParcelFileDescriptor of the selected document
     * @throws FileNotFoundException -
     */
    @Override
    public ParcelFileDescriptor openDocument(final String documentId, final String mode, CancellationSignal signal) throws FileNotFoundException {
        final File file = getFileForDocId(documentId);
        final int accessMode = ParcelFileDescriptor.parseMode(mode);
        return ParcelFileDescriptor.open(file, accessMode);
    }


    /**
     * A function to get the type of Document (MIME Type)
     * @param documentId The ID of the document
     * @return The MIME Type of the document
     * @throws FileNotFoundException -
     */
    @Override
    public String getDocumentType(String documentId) throws FileNotFoundException {
        File file = getFileForDocId(documentId);
        return getTypeForFile(file);
    }

    /**
     * A function to choose which projection to use from the OS's given projection and our default one for a Root
     * @param projection The projection given by Android
     * @return either the requested root column projection, or the default projection if the requested projection is null.
     */
    private static String[] resolveRootProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_ROOT_PROJECTION;
    }

    /**
     * A function to choose which projection to use from the OS's given projection and our default one for a document
     * @param projection The projection given by Android
     * @return either the requested root column projection, or the default projection if the requested projection is null.
     */
    private static String[] resolveDocumentProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION;
    }

    /**
     * Get a file's MIME type
     *
     * @param file the File object whose type we want
     * @return the MIME type of the file
     */
    private static String getTypeForFile(File file) {
        if (file.isDirectory()) {
            return Document.MIME_TYPE_DIR;
        } else {
            return getTypeForName(file.getName());
        }
    }

    /**
     * Get the MIME data type of a document, given its filename.
     *
     * @param name the filename of the document
     * @return the MIME data type of a document
     */
    private static String getTypeForName(String name) {
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = name.substring(lastDot + 1);
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }
        return "application/octet-stream";
    }

    /**
     * Gets a string of unique MIME data types a directory supports, separated by newlines.  This
     * should not change.
     *
     * @return a string of the unique MIME data types the application shares
     */
    private String getAppMimes() {
        Set<String> mimeTypes = new HashSet<>();
        mimeTypes.add("application/pdf");

        StringBuilder mimeTypesString = new StringBuilder();
        for (String mimeType : mimeTypes) {
            mimeTypesString.append(mimeType).append("\n");
        }

        return mimeTypesString.toString();
    }

    /**
     * Get the document ID given a File.  The document id must be consistent across time.  Other
     * applications may save the ID and use it to reference documents later.
     *
     * This implementation is specific to this demo.  It assumes only one root and is built
     * directly from the file structure.  However, it is possible for a document to be a child of
     * multiple directories (for example "android" and "images"), in which case the file must have
     * the same consistent, unique document ID in both cases.
     *
     * @param file the File whose document ID you want
     * @return the corresponding document ID
     */
    private String getDocIdForFile(File file) {
        String path = file.getAbsolutePath();

        // Start at first char of path under root
        final String rootPath = mBaseDir.getPath();
        if (rootPath.equals(path)) {
            path = "";
        } else if (rootPath.endsWith("/")) {
            path = path.substring(rootPath.length());
        } else {
            path = path.substring(rootPath.length() + 1);
        }

        return "root" + ':' + path;
    }

    /**
     * Add a representation of a file to a cursor.
     *
     * @param result the cursor to modify
     * @param docId  the document ID representing the desired file (may be null if given file)
     * @param file   the File object representing the desired file (may be null if given docID)
     * @throws java.io.FileNotFoundException -
     */
    private void includeFile(MatrixCursor result, String docId, File file) throws FileNotFoundException {
        if (docId == null) {
            docId = getDocIdForFile(file);
        } else {
            file = getFileForDocId(docId);
        }

        int flags = 0;

        final String displayName = file.getName();
        final String mimeType = getTypeForFile(file);

        flags |= Document.FLAG_SUPPORTS_THUMBNAIL;


        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_DISPLAY_NAME, displayName);
        row.add(Document.COLUMN_SIZE, file.length());
        row.add(Document.COLUMN_MIME_TYPE, mimeType);
        row.add(Document.COLUMN_LAST_MODIFIED, file.lastModified());
        row.add(Document.COLUMN_FLAGS, flags);

        // Add a custom icon
        row.add(Document.COLUMN_ICON, R.mipmap.icon);
    }

    /**
     * Translate your custom URI scheme into a File object.
     *
     * @param docId the document ID representing the desired file
     * @return a File represented by the given document ID
     * @throws java.io.FileNotFoundException -
     */
    private File getFileForDocId(String docId) throws FileNotFoundException {
        File target = mBaseDir;
        if (docId.equals(ROOT)) {
            return target;
        }
        final int splitIndex = docId.indexOf(':', 1);
        if (splitIndex < 0) {
            throw new FileNotFoundException("Missing root for " + docId);
        } else {
            final String path = docId.substring(splitIndex + 1);
            target = new File(target, path);
            if (!target.exists()) {
                throw new FileNotFoundException("Missing file for " + docId + " at " + target);
            }
            return target;
        }
    }
}