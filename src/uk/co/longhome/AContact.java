package uk.co.longhome;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public abstract class AContact {

	Map<Fields, FieldInterface> fields = new HashMap<Fields, FieldInterface>();
	
	protected boolean pictureChanged = false;

	public boolean changed = false;

	public boolean compareTo(AContact contact) {
		for (Fields field : Fields.values()) {
			if (!this.getField(field).equals(contact.getField(field)))
				return false;
		}
		return true;
	}

	public String getField(Fields field) {
		return fields.get(field).getValue();
	}

	public void setField(Fields field, String value) {
		changed = true;
		fields.get(field).setValue(value);
	}

	public void updateWith(AContact contact) {
		for (Fields field : Fields.values()) {
			this.setField(field, contact.getField(field));
		}
		this.setPictureBytes(contact.getPictureBytes());
	}

	public abstract void save();

	public abstract void delete();

	public abstract String getPictureHash();

	public abstract byte[] getPictureBytes();

	public abstract void setPictureBytes(byte[] pictureBytes);

	public String getHashFromBytes(byte[] pictureBytes) {
		if(pictureBytes == null)return "";
		int index;
		int fileLength = pictureBytes.length;
		int[] hash = new int[10];
		for (index = 0; index < fileLength; index++) {
			hash[index % 10] += pictureBytes[index];
		}
		char[] hashChars = new char[10];
		for (index = 0; index < 10; index++) {
			hashChars[index] = (char) ('a' + (Math.abs(hash[index]) % 25));
		}
		return new String(hashChars);

	}
	
	public boolean getDeleted(){
		return false;
	}

}
