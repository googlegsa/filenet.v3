package com.google.enterprise.connector.filenet3;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.enterprise.connector.filenet3.filewrap.IBaseObject;
import com.google.enterprise.connector.filenet3.filewrap.IDocument;
import com.google.enterprise.connector.filenet3.filewrap.IObjectStore;
import com.google.enterprise.connector.filenet3.filewrap.IProperties;
import com.google.enterprise.connector.filenet3.filewrap.IProperty;
import com.google.enterprise.connector.filenet3.filewrap.IVersionSeries;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spiimpl.BinaryValue;
import com.google.enterprise.connector.spiimpl.BooleanValue;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.DoubleValue;
import com.google.enterprise.connector.spiimpl.LongValue;
import com.google.enterprise.connector.spiimpl.StringValue;

public class FileDocument implements Document {

    private IObjectStore objectStore;
    private IDocument document = null;
    private String displayUrl;
    private String docId;
    private String versionId;
    private String timeStamp;
    private String vsDocId;
    private HashSet included_meta = null;
    private HashSet excluded_meta = null;
    private static Logger logger = null;
    {
        logger = Logger.getLogger(FileDocument.class.getName());
    }
    private SpiConstants.ActionType action;
    private final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private final static int ONE_SECOND = 1000;

    public FileDocument(String docId, IObjectStore objectStore,
            String displayUrl, HashSet included_meta,
            HashSet excluded_meta, SpiConstants.ActionType action) {
        this.docId = docId;
        this.objectStore = objectStore;
        this.displayUrl = displayUrl;
        this.included_meta = included_meta;
        this.excluded_meta = excluded_meta;
        this.action=action;
    }

    public FileDocument(String docId, String timeStamp, IObjectStore objectStore,
            String displayUrl, HashSet included_meta,
            HashSet excluded_meta, SpiConstants.ActionType action) {
        this.docId = docId;
        this.objectStore = objectStore;
        this.timeStamp = timeStamp;
        this.displayUrl = displayUrl;
        this.included_meta = included_meta;
        this.excluded_meta = excluded_meta;
        this.action=action;
    }

    public FileDocument(String docId, String commonVersionId, String timeStamp, IObjectStore objectStore,
            String displayUrl, HashSet included_meta,
            HashSet excluded_meta, SpiConstants.ActionType action) {
        this.docId = docId;
        this.versionId = commonVersionId;
        this.timeStamp = timeStamp;
        this.objectStore = objectStore;
        this.displayUrl = displayUrl;
        this.included_meta = included_meta;
        this.excluded_meta = excluded_meta;
        this.action=action;
    }

    private void fetch() throws RepositoryDocumentException{
        if (document != null) {
            return;
        }
        IVersionSeries vSeries = null;
        document = (IDocument) objectStore.getObject(IBaseObject.TYPE_DOCUMENT,    docId);

        logger.log(Level.FINE, "Fetch document for docId " + docId);

        vSeries = document.getVersionSeries();
        this.vsDocId = vSeries.getId();
        logger.log(Level.FINE, "VersionSeriesID for document is : " + this.vsDocId);

    }

    private Calendar getDate(String type, IDocument document)
    throws IllegalArgumentException, RepositoryDocumentException {

        Date date = this.document.getPropertyDateValue(type);

        logger.log(Level.FINE, "Property date of the document is: "+date.toString());
        Calendar c = Calendar.getInstance();

        c.setTime(date);
        logger.log(Level.FINE, "Calendar after setTime : "+c);
        return c;
    }

    public Property findProperty(String name) throws RepositoryDocumentException{
        HashSet set = new HashSet();
        logger.log(Level.FINE, "Entering into function findProperty(String name)");
        if (SpiConstants.ActionType.ADD.equals(action)) {
            fetch();
            if (SpiConstants.PROPNAME_CONTENT.equals(name)) {
                if(document.getContent()!= null){
                    set.add(new BinaryValue(document.getContent()));
                }else{
                    logger.log(Level.FINE, "Content is null for docId "+this.docId);
                    set.add(null);
                }
                return new FileDocumentProperty(name, set);
            } else if (SpiConstants.PROPNAME_DISPLAYURL.equals(name)) {
                logger.log(Level.INFO, "Getting property: "+name);
                set.add(new StringValue(this.displayUrl + vsDocId));
                return new FileDocumentProperty(name, set);
            } else if (SpiConstants.PROPNAME_ISPUBLIC.equals(name)) {
                logger.log(Level.INFO, "Getting property: "+name);
                set.add(BooleanValue.makeBooleanValue(false));
                return new FileDocumentProperty(name, set);
            } else if (SpiConstants.PROPNAME_LASTMODIFIED.equals(name)) {
                logger.log(Level.INFO, "Getting property: "+name);

                Calendar tmpCal = Calendar.getInstance();
                try {
                    Date tmpDt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(timeStamp);

                    long timeDateMod=tmpDt.getTime();
//                    logger.log(Level.FINE,"Last Modified Mate before setTime "+tmpDt.getSeconds());
                    tmpDt.setTime(timeDateMod+ONE_SECOND);
//                    logger.fine("Last Modified Date after setTime "+tmpDt.getSeconds());
                    tmpCal.setTime(tmpDt);

//                    logger.fine("Right last modified date : "+tmpDt.toString());
                } catch (ParseException e) {
                    logger.log(Level.WARNING,"Warning: Wrong Last Modified Date");
                    tmpCal.setTime(new Date());
                }

//                logger.info("tmpCal after setTime : " + tmpCal);
                FileDateValue tmpDtVal = new FileDateValue(tmpCal);
                logger.log(Level.FINE,"Last Modified Date value : " + tmpDtVal.toString());
                set.add(tmpDtVal);
                return new FileDocumentProperty(name, set);
            } else if (SpiConstants.PROPNAME_MIMETYPE.equals(name)) {
                logger.log(Level.INFO, "Getting property: "+name);
                try {
                    set.add(new StringValue(document.getPropertyStringValue("MimeType")));
                } catch (RepositoryDocumentException e) {
                    logger.log(Level.WARNING,"Unable to get Property "+name+": "+ e.getLocalizedMessage());
                    set.add(null);
                }

                return new FileDocumentProperty(name, set);
            } else if (SpiConstants.PROPNAME_SEARCHURL.equals(name)) {
                logger.log(Level.INFO, "Getting property: "+name);
                return null;
            } else if(SpiConstants.PROPNAME_TITLE.equals(name)){
                logger.log(Level.INFO, "Getting property: "+name);
//                try{
//                    set.add(new StringValue(document.getPropertyStringValue("Title")));
//                    logger.fine("Property "+name+" : "+document.getPropertyStringValue("Title"));
//                } catch(RepositoryDocumentException e){
//                    logger.warning("RepositoryException thrown : "+ e+" on getting property : "+name);
//                    logger.warning("RepositoryException thrown message : "+ e.getMessage());
//                    set.add(null);
//                }
                return null;

            }else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
                logger.log(Level.INFO, "Getting property: "+name);
                set.add(new StringValue(vsDocId));
                logger.log(Level.FINE, "Property "+name+" : "+vsDocId);
                return new FileDocumentProperty(name, set);
            }else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
                logger.log(Level.INFO, "Getting property: "+name);
                set.add(new StringValue(action.toString()));
                logger.log(Level.FINE, "Property "+name+" : "+action.toString());
                return new FileDocumentProperty(name, set);
            }
            try{
                String prop = null;
                String[] names = { name };
                IProperties props = document.getProperties(names);
                IProperty property = null;

                int ps = props.size();
                for (int i = 0; i < ps; i++) {
                    property = props.get(i);
                    prop = property.getValueType();
                }

                if(prop != null){
                    if (prop.equals("Binary")) {
                        logger.log(Level.INFO, "Getting property: "+name);
                        set.add(new BinaryValue(document.getPropertyBinaryValue(name)));
                    } else if (prop.equals("Boolean")) {
                        logger.log(Level.INFO, "Getting property: "+name);
                        set.add(BooleanValue.makeBooleanValue(document.getPropertyBooleanValue(name)));
                        logger.log(Level.FINE, "Property "+name+" : "+BooleanValue.makeBooleanValue(document.getPropertyBooleanValue(name)));
                    } else if (prop.equals("Date")) {
                        logger.log(Level.INFO, "Getting property: "+name);
                        set.add(new DateValue(getDate(name, document)));
                        logger.log(Level.FINE, "Property "+name+" : "+getDate(name, document));
                    } else if (prop.equals("Double")) {
                        logger.log(Level.INFO, "Getting property: "+name);
                        set.add(new DoubleValue(document.getPropertyDoubleValue(name)));
                    } else if (prop.equals("String")) {
                        logger.log(Level.INFO, "Getting property: "+name);
                        set.add(new StringValue(document.getPropertyStringValue(name)));
                        logger.log(Level.FINE, "Property "+name+" : "+document.getPropertyStringValue(name));
                    } else if (prop.equals("Long")) {
                        logger.log(Level.INFO, "Getting property: "+name);
                        set.add(new LongValue(document.getPropertyLongValue(name)));
                    }
                }

            }catch(RepositoryDocumentException re){
                logger.log(Level.WARNING,"Unable to get Property "+name+": "+ re.getLocalizedMessage());
                set.add(null);
            }
        }else{
            if (SpiConstants.PROPNAME_LASTMODIFIED.equals(name)) {
                Calendar tmpCal = Calendar.getInstance();
                logger.log(Level.FINE, "Calendar del instance : " + tmpCal);


                try {
                    Date tmpDt = new SimpleDateFormat(DATE_FORMAT).parse(timeStamp);

                    ///
                    long timeDateMod=tmpDt.getTime();
//                    logger.fine("last modified date before setTime "+tmpDt.getSeconds());
                    tmpDt.setTime(timeDateMod+ONE_SECOND);
//                    logger.fine("last modified date after setTime "+tmpDt.getSeconds());
                    tmpCal.setTime(tmpDt);


//                    logger.fine("Right last modified date : "+tmpDt.toString());
                } catch (ParseException e) {

                    logger.log(Level.WARNING, "Warning: wrong last modified date");
                    tmpCal.setTime(new Date());
                }
//                logger.info("tmpCal after setTime : " + tmpCal);
                FileDateValue tmpDtVal = new FileDateValue(tmpCal);
                logger.log(Level.FINE, "Last modify date value : " + tmpDtVal.toString());
                set.add(tmpDtVal);
                return new FileDocumentProperty(name, set);
            }else if (SpiConstants.PROPNAME_ACTION.equals(name)) {
                set.add(new StringValue(action.toString()));
                return new FileDocumentProperty(name, set);
            }else if (SpiConstants.PROPNAME_DOCID.equals(name)) {
                set.add(new StringValue(versionId));
                logger.log(Level.FINE, "VersionId : " + versionId);
                return new FileDocumentProperty(name, set);
            }
        }

        return new FileDocumentProperty(name, set);
    }

    public Set getPropertyNames() throws RepositoryDocumentException {
        fetch();
        HashSet properties = new HashSet();
        IProperties documentProperties = this.document.getProperties();
        IProperty property;
        for (int i = 0; i < documentProperties.size(); i++) {
            property = (IProperty) documentProperties.get(i);
            //Added by Pankaj on 06/05/09 to solve the include/exclude metadata policy
            if(property.getValue() != null){
                if(included_meta.size() != 0){
                    //includeMeta - exludeMeta
                    logger.log(Level.FINE, "Metadata set will be (includeMeta - exludeMeta)");
                    if ((!excluded_meta.contains(property.getName())
                            && included_meta.contains(property.getName()))){
                        properties.add(property.getName());
                    }
                }else {
                    //superSet - exludeMeta
                    logger.log(Level.FINE, "Metadata set will be (superSet - exludeMeta)");
                    if ((!excluded_meta.contains(property.getName())
                            || included_meta.contains(property.getName()))){
                        properties.add(property.getName());
                    }
                }
            }
        }
        return properties;
    }
}
