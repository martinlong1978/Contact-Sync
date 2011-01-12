package uk.co.longhome;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ReferenceContact extends AContact implements Serializable{
	
	String outlookContactID;
	String googleID;
	String outPictureHash = "";
	String googlePictureHash = "";
	
	Map<Fields, String> values = new HashMap<Fields, String>();

	public String getOutlookContactID() {
		return outlookContactID;
	}
	
	public String getGoogleContactID(){
		return googleID;
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub

	}

	public void setOutlookContactID(String id) {
		outlookContactID = id;
	}
	
	public void setGoogleContactID(String id){
		googleID = id;
	}

	@Override
	public String getField(Fields field) {
		String string = values.get(field);
		return string == null ? "" : string;
	}

	@Override
	public void setField(Fields field, String value) {
		values.put(field,value);
	}

	@Override
	public void delete() {
	}

	@Override
	public byte[] getPictureBytes() {
		return null;
	}

	public void setGooglePictureHash(String hash)
	{
		this.googlePictureHash = hash;
	}
	
	public String getGooglePictureHash() {
		return googlePictureHash;
	}
	
	public void setOutPictureHash(String hash)
	{
		this.outPictureHash = hash;
	}
	
	public String getOutPictureHash() {
		return outPictureHash;
	}

	@Override
	public void setPictureBytes(byte[] pictureBytes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getPictureHash() {
		// TODO Auto-generated method stub
		return null;
	}

}
