package uk.co.longhome;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.contacts.ContactGroupEntry;
import com.google.gdata.data.contacts.ContactGroupFeed;
import com.google.gdata.data.contacts.SystemGroup;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.moyosoft.connector.com.ComponentObjectModelException;
import com.moyosoft.connector.exception.LibraryNotFoundException;
import com.moyosoft.connector.ms.outlook.Outlook;
import com.moyosoft.connector.ms.outlook.contact.OutlookContact;
import com.moyosoft.connector.ms.outlook.folder.FolderType;
import com.moyosoft.connector.ms.outlook.folder.OutlookFolder;
import com.moyosoft.connector.ms.outlook.item.ItemsCollection;
import com.moyosoft.connector.ms.outlook.item.ItemsIterator;
import com.moyosoft.connector.ms.outlook.item.OutlookItem;

public class ContactSync
{

    private static final String REF_STORE         = "C:/outlook/reference.obj";

    Map<String, GoogleContact>  googleContacts    = new HashMap<String, GoogleContact>();

    Map<String, OutContact>     outlookContacts   = new HashMap<String, OutContact>();

    List<ReferenceContact>      referenceContacts = new ArrayList<ReferenceContact>();

    ContactsService             myService;

    URL                         feedUrl;

    private OutlookFolder       folder;

    String                      googleGroup;

    public ContactSync()
    {
        try
        {
            feedUrl = new URL("http://www.google.com/m8/feeds/contacts/default/full");
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        File dir = new File("C:/outlook/");
        if (!dir.exists())
        {
            dir.mkdir();
        }
        while (true)
        {
            System.out.println("Start.");
            ContactSync me = new ContactSync();
            me.retrieveOutlookContacts();
            me.retrieveGoogleContacts();
            me.retrieveReferenceContacts();
            me.sync();
            me.saveReferenceContacts();
            System.out.println("Complete.");
            try
            {
                Thread.sleep(600000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void saveReferenceContacts()
    {
        try
        {
            FileOutputStream fo = new FileOutputStream(REF_STORE);
            ObjectOutputStream xe = new ObjectOutputStream(fo);
            xe.writeObject(referenceContacts);
            xe.flush();
            xe.close();
            fo.flush();
            fo.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void retrieveReferenceContacts()
    {
        try
        {
            FileInputStream fi = new FileInputStream(REF_STORE);
            ObjectInputStream xd = new ObjectInputStream(fi);
            referenceContacts = (List<ReferenceContact>) xd.readObject();
            xd.close();
            fi.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    private void sync()
    {
        for (ReferenceContact referenceContact : referenceContacts.toArray(new ReferenceContact[referenceContacts.size()]))
        {
            GoogleContact googleContact = googleContacts.remove(referenceContact.getGoogleContactID());
            OutContact outContact = outlookContacts.remove(referenceContact.getOutlookContactID());
            if (googleContact == null || googleContact.getDeleted())
            {
                log("DELETING: "
                        + outContact.getFirstName()
                        + " "
                        + outContact.getLastName());
                outContact.delete();
            }
            if (outContact == null)
            {
                log("DELETING: "
                        + googleContact.getFirstName()
                        + " "
                        + googleContact.getLastName());
                googleContact.delete();
            }
            if (googleContact == null || outContact == null)
            {
                referenceContacts.remove(referenceContact);
            }
            else
            {
                syncContacts(referenceContact, googleContact, outContact);
            }
        }
        for (GoogleContact googleContact : googleContacts.values())
        {
            log("ADDING to OUTLOOK "
                    + googleContact.getFirstName()
                    + " "
                    + googleContact.getLastName());
            OutContact outContact = new OutContact(folder);
            ReferenceContact contact = new ReferenceContact();
            copyContact(googleContact, outContact, contact);
            updateReferenceIDs(googleContact, outContact, contact);
            referenceContacts.add(contact);
        }
        for (OutContact outContact : outlookContacts.values())
        {
            log("ADDING to GOOGLE  "
                    + outContact.getFirstName()
                    + " "
                    + outContact.getLastName());
            GoogleContact googleContact = new GoogleContact(myService,
                    feedUrl,
                    googleGroup);
            ReferenceContact contact = new ReferenceContact();
            copyContact(outContact, googleContact, contact);
            updateReferenceIDs(googleContact, outContact, contact);
            referenceContacts.add(contact);
        }
    }

    private void copyContact(AContact source,
            AContact dest,
            ReferenceContact reference)
    {
        dest.updateWith(source);
        reference.updateWith(source);
        dest.save();
    }

    private void updateReferenceIDs(GoogleContact googleContact,
            OutContact outContact,
            ReferenceContact contact)
    {
        contact.setGoogleContactID(googleContact.getGoogleContactID());
        contact.setOutlookContactID(outContact.getOutlookContactID());
        contact.setGooglePictureHash(googleContact.getPictureHash());
        contact.setOutPictureHash(outContact.getPictureHash());
    }

    private void syncContacts(ReferenceContact contact,
            GoogleContact googleContact,
            OutContact outContact)
    {
        for (Fields field : Fields.values())
        {
            if (!contact.getField(field).equals(outContact.getField(field)))
            {
                log("Updating GOOGLE  "
                        + field
                        + " for "
                        + outContact.getFirstName()
                        + " "
                        + outContact.getLastName()
                        + " from "
                        + googleContact.getField(field)
                        + " to "
                        + outContact.getField(field));
                contact.setField(field, outContact.getField(field));
                googleContact.setField(field, outContact.getField(field));
            }
            else if (!contact.getField(field)
                    .equals(googleContact.getField(field)))
            {
                log("Updating OUTLOOK "
                        + field
                        + " for "
                        + outContact.getFirstName()
                        + " "
                        + outContact.getLastName()
                        + " from "
                        + outContact.getField(field)
                        + " to "
                        + googleContact.getField(field));
                contact.setField(field, googleContact.getField(field));
                outContact.setField(field, googleContact.getField(field));
            }
        }
        if (!contact.getOutPictureHash().equals(outContact.getPictureHash()))
        {
            googleContact.setPictureBytes(outContact.getPictureBytes());
            log("Updating GOOGLE  Picture for "
                    + outContact.getFirstName()
                    + " "
                    + outContact.getLastName()
                    + " from "
                    + googleContact.getPictureHash()
                    + " to "
                    + outContact.getPictureHash());
        }
        else if (!contact.getGooglePictureHash()
                .equals(googleContact.getPictureHash()))
        {
            outContact.setPictureBytes(googleContact.getPictureBytes());
            log("Updating OUTLOOK Picture for "
                    + outContact.getFirstName()
                    + " "
                    + outContact.getLastName()
                    + " from "
                    + outContact.getPictureHash()
                    + " to "
                    + googleContact.getPictureHash());
        }
        googleContact.save();
        outContact.save();
        contact.setOutPictureHash(outContact.getPictureHash());
        contact.setGooglePictureHash(googleContact.getPictureHash());
    }

    private void retrieveGoogleContacts()
    {
        myService = new ContactsService("LongHome-ContactSync-1");
        try
        {
            myService.setUserCredentials("parameterise this", " and this");
            getAllContacts();
            getContactsGroup();
        }
        catch (AuthenticationException e)
        {
            e.printStackTrace();
        }
        catch (ServiceException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void getContactsGroup()
    {
        try
        {
            URL feedUrl = new URL("http://www.google.com/m8/feeds/groups/default/full");
            ContactGroupFeed resultFeed;
            resultFeed = myService.getFeed(feedUrl, ContactGroupFeed.class);
            for (ContactGroupEntry entry : resultFeed.getEntries())
            {
                SystemGroup systemGroup = entry.getSystemGroup();
                if (systemGroup != null
                        && systemGroup.getId().equals("Contacts"))
                {
                    googleGroup = entry.getId();
                }
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (ServiceException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void retrieveOutlookContacts()
    {
        // Outlook application
        Outlook outlookApplication;
        try
        {
            outlookApplication = new Outlook();
            folder = outlookApplication.getDefaultFolder(FolderType.CONTACTS);
            ItemsCollection items = folder.getItems();
            ItemsIterator iterator = items.iterator();
            while (iterator.hasNext())
            {
                OutlookItem item = iterator.nextItem();
                if (item instanceof OutlookContact)
                {
                    OutlookContact contact = (OutlookContact) item;
                    OutContact outContact = new OutContact(contact);
                    outlookContacts.put(outContact.getOutlookContactID(),
                            outContact);
                }
            }

        }
        catch (ComponentObjectModelException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (LibraryNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void getAllContacts() throws ServiceException, IOException
    {
        // Request the feed
        Query myQuery = new Query(feedUrl);
        myQuery.setMaxResults(2000);
        ContactFeed resultFeed = myService.query(myQuery, ContactFeed.class);
        // Print the results
        for (int i = 0; i < resultFeed.getEntries().size(); i++)
        {

            ContactEntry entry = resultFeed.getEntries().get(i);
            GoogleContact googleContact = new GoogleContact(entry,
                    myService,
                    feedUrl);
            this.googleContacts.put(googleContact.getGoogleContactID(),
                    googleContact);
        }
    }

    private static FileWriter logWriter;

    private static DateFormat dateFormat;
    private static DateFormat nameFormat;

    public synchronized static void log(String logString)
    {
        try
        {
            if (logWriter == null)
            {
                dateFormat = DateFormat.getDateTimeInstance();
                nameFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                logWriter = new FileWriter("C:/outlook/sync"
                        + nameFormat.format(new Date())
                        + ".log");
            }

            String line = dateFormat.format(new Date())
                    + ": "
                    + logString
                    + "\r\n";
            System.out.print(line);
            logWriter.write(line);
            logWriter.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
