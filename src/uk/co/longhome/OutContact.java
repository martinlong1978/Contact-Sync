package uk.co.longhome;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.moyosoft.connector.ms.outlook.attachment.AttachmentsCollection;
import com.moyosoft.connector.ms.outlook.contact.OutlookContact;
import com.moyosoft.connector.ms.outlook.folder.OutlookFolder;

public class OutContact extends AContact {

	OutlookContact outlookContact;

	public OutContact(OutlookFolder folder) {
		registerCalls();
		outlookContact = (OutlookContact) folder.createItem();
	}

	public OutContact(OutlookContact outlookContact) {
		registerCalls();
		this.outlookContact = outlookContact;
		String picture = getPictureHash();
	}

	private void registerCalls() {
		fields.put(Fields.FIRST, new FieldInterface() {

			public String getValue() {
				return getFirstName();
			}

			public void setValue(String value) {
				setFirstName(value);
			}
		});

		fields.put(Fields.MIDDLE, new FieldInterface() {

			public String getValue() {
				return outlookContact.getMiddleName();
			}

			public void setValue(String value) {
				outlookContact.setMiddleName(value);
			}
		});

		fields.put(Fields.LAST, new FieldInterface() {

			public String getValue() {
				return getLastName();
			}

			public void setValue(String value) {
				setLastName(value);
			}
		});

		fields.put(Fields.TITLE, new FieldInterface() {

			public String getValue() {
				return getTitleName();
			}

			public void setValue(String value) {
				setTitleName(value);
			}
		});

		fields.put(Fields.NICKNAME, new FieldInterface() {

			public String getValue() {
				return outlookContact.getNickName();
			}

			public void setValue(String value) {
				outlookContact.setNickName(value);
			}
		});

		fields.put(Fields.COMPANY, new FieldInterface() {

			public String getValue() {
				return outlookContact.getCompanyName();
			}

			public void setValue(String value) {
				outlookContact.setCompanyName(value);
			}
		});

		fields.put(Fields.OTHER_PHONE, new FieldInterface() {

			public String getValue() {
				return outlookContact.getOtherTelephoneNumber();
			}

			public void setValue(String value) {
				outlookContact.setOtherTelephoneNumber(value);
			}
		});

		fields.put(Fields.BUSINESS_PHONE, new FieldInterface() {

			public String getValue() {
				return outlookContact.getBusinessTelephoneNumber();
			}

			public void setValue(String value) {
				outlookContact.setBusinessTelephoneNumber(value);
			}
		});

		fields.put(Fields.BUSINESS_PHONE2, new FieldInterface() {

			public String getValue() {
				return outlookContact.getBusiness2TelephoneNumber();
			}

			public void setValue(String value) {
				outlookContact.setBusiness2TelephoneNumber(value);
			}
		});

		fields.put(Fields.MOBILE_PHONE, new FieldInterface() {

			public String getValue() {
				return outlookContact.getMobileTelephoneNumber();
			}

			public void setValue(String value) {
				outlookContact.setMobileTelephoneNumber(value);
			}
		});

		fields.put(Fields.MOBILE_PHONE2, new FieldInterface() {

			public String getValue() {
				return outlookContact.getPagerNumber();
			}

			public void setValue(String value) {
				outlookContact.setPagerNumber(value);
			}
		});

		fields.put(Fields.HOME_PHONE, new FieldInterface() {

			public String getValue() {
				return outlookContact.getHomeTelephoneNumber();
			}

			public void setValue(String value) {
				outlookContact.setHomeTelephoneNumber(value);
			}
		});

		fields.put(Fields.HOME_PHONE2, new FieldInterface() {

			public String getValue() {
				return outlookContact.getHome2TelephoneNumber();
			}

			public void setValue(String value) {
				outlookContact.setHome2TelephoneNumber(value);
			}
		});

		fields.put(Fields.EMAIL1, new FieldInterface() {

			public String getValue() {
				return outlookContact.getEmail1Address();
			}

			public void setValue(String value) {
				outlookContact.setEmail1Address(value);
			}
		});

		fields.put(Fields.EMAIL2, new FieldInterface() {

			public String getValue() {
				return outlookContact.getEmail2Address();
			}

			public void setValue(String value) {
				outlookContact.setEmail2Address(value);
			}
		});

		fields.put(Fields.EMAIL3, new FieldInterface() {

			public String getValue() {
				return outlookContact.getEmail3Address();
			}

			public void setValue(String value) {
				outlookContact.setEmail3Address(value);
			}
		});

		fields.put(Fields.BUSINESS_ADDR, new FieldInterface() {

			public String getValue() {
				return translateCrLf(outlookContact.getBusinessAddress());
			}

			public void setValue(String value) {
				outlookContact.setBusinessAddress(translateLf(value));
			}
		});

		fields.put(Fields.HOME_ADDR, new FieldInterface() {

			public String getValue() {
				return translateCrLf(outlookContact.getHomeAddress());
			}

			public void setValue(String value) {
				outlookContact.setHomeAddress(translateLf(value));
			}
		});
		
		fields.put(Fields.OUTID, new FieldInterface(){

			public String getValue() {
				return getOutlookContactID();
			}

			public void setValue(String value) {
				
			}});

	}

	private static String translateCrLf(String trans) {
		// int i;
		// while ((i = trans.indexOf("\r\n")) > -1) {
		// trans = trans.substring(0, i) + trans.substring(i+1);
		// }
		// return trans;
		return trans.replace("\r\n", "\n");
	}

	private static String translateLf(String trans) {
		return trans.replace("\n", "\r\n");
	}

	public String getOutlookContactID() {
		return outlookContact.getEntryId();
	}

	public String getFirstName() {
		return outlookContact.getFirstName();
	}

	public void setFirstName(String name) {
		outlookContact.setFirstName(name);
	}

	public String getLastName() {
		return outlookContact.getLastName();
	}

	public String getTitleName() {
		return outlookContact.getTitle();
	}

	public void setLastName(String name) {
		outlookContact.setLastName(name);
	}

	public void setTitleName(String name) {
		outlookContact.setTitle(name);
	}

	public void save() {
		if (changed) {
			outlookContact.save();
			changed = false;
		}
	}

	private byte[] pictureBytes = null;

	private String pictureHash = null;

	public byte[] getPictureBytes() {
		getPictureHash();
		return pictureBytes;
	}

	public String getPictureHash() {
		if (pictureHash != null)
			return pictureHash;
		pictureHash = "";
		AttachmentsCollection attachments = outlookContact.getAttachments();
		int count = attachments.getCount();
		try {
			for (int i = 0; i < count; i++) {
				String contentId = attachments.getItem(i).getDisplayName();
				if (contentId.startsWith("ContactP")) {
					File tempFile = new File("C:\\olsync\\ContactPicture" + i + ".jpg");
					if (tempFile.exists())
						tempFile.delete();
					attachments.getItem(i).saveAsFile(tempFile);
					FileInputStream in = new FileInputStream(tempFile);
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte[] buffer = new byte[4096];
					for (int read = 0; (read = in.read(buffer)) != -1; out
							.write(buffer, 0, read))
						;
					pictureBytes = out.toByteArray();
					in.close();
					pictureHash = getHashFromBytes(pictureBytes);
					File file = new File("C:\\olsync\\" + outlookContact.getItemId() + ".jpg");
					if (file.exists())
						file.delete();
					tempFile.renameTo(file);
					tempFile.delete();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return pictureHash;
	}

	@Override
	public void delete() {
		outlookContact.delete();
	}

	@Override
	public void setPictureBytes(byte[] bytes) {
		if (bytes == null)
			return;
		this.pictureBytes = null;
		this.pictureHash = null;
		try {
			outlookContact.removePicture();
			while (outlookContact.getAttachmentsCount() > 0) {
				outlookContact.getAttachments().remove(0);
			}
			outlookContact.save();
			File tempFile = new File("C:/outlook/outtemp.jpg");
			FileOutputStream os = new FileOutputStream(tempFile);
			os.write(bytes);
			os.flush();
			os.close();
			outlookContact.addPicture(tempFile);
			tempFile.delete();
			outlookContact.save();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
