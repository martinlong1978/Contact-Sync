package uk.co.longhome;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.Link;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ExternalId;
import com.google.gdata.data.contacts.GroupMembershipInfo;
import com.google.gdata.data.contacts.Nickname;
import com.google.gdata.data.extensions.AdditionalName;
import com.google.gdata.data.extensions.City;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.FamilyName;
import com.google.gdata.data.extensions.FormattedAddress;
import com.google.gdata.data.extensions.GivenName;
import com.google.gdata.data.extensions.HouseName;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.NamePrefix;
import com.google.gdata.data.extensions.Neighborhood;
import com.google.gdata.data.extensions.OrgName;
import com.google.gdata.data.extensions.Organization;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.data.extensions.PostCode;
import com.google.gdata.data.extensions.Street;
import com.google.gdata.data.extensions.StructuredPostalAddress;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.ResourceNotFoundException;
import com.google.gdata.util.ServiceException;

public class GoogleContact extends AContact {

	ContactEntry entry;

	String first;

	String middle;

	String last;

	String title;

	ContactsService myService;

	URL feedUrl;

	boolean newContact = false;

	public GoogleContact(ContactsService myService, URL feedUrl,
			String googlegroup) {
		registerCalls();
		this.myService = myService;
		this.feedUrl = feedUrl;
		newContact = true;
		entry = new ContactEntry();
		entry
				.addGroupMembershipInfo(new GroupMembershipInfo(false,
						googlegroup));
	}

	@Override
	public boolean getDeleted() {
		return entry.getDeleted() != null;
	}

	public GoogleContact(ContactEntry entry, ContactsService myService,
			URL feedUrl) {
		registerCalls();
		this.entry = entry;
		this.myService = myService;
		this.feedUrl = feedUrl;
		// System.out.println(entry.getTitle().getPlainText() + ": " +
		// entry.getDeleted());
		loadPicture();
	}

	private void loadPicture() {
		Link photoLink = entry.getContactPhotoLink();
		try {
			if (photoLink != null) {
				GDataRequest request = myService
						.createLinkQueryRequest(photoLink);
				request.execute();
				InputStream in = request.getResponseStream();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buffer = new byte[4096];
				for (int read = 0; (read = in.read(buffer)) != -1; out.write(
						buffer, 0, read))
					;
				pictureBytes = out.toByteArray();
			}
		} catch (ResourceNotFoundException ex) {

		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
				return getMiddleName();
			}

			public void setValue(String value) {
				setMiddleName(value);
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
				Nickname nickname = entry.getNickname();
				return nickname == null ? "" : nickname.getValue();
			}

			public void setValue(String value) {
				if (value == null || value.length() < 1) {
					entry.setNickname(new Nickname());
				} else {
					entry.setNickname(new Nickname(value));
				}
			}
		});

		fields.put(Fields.COMPANY, new FieldInterface() {

			public String getValue() {
				List<Organization> organizations = entry.getOrganizations();
				if (organizations.size() < 1) {
					return "";
				} else {
					OrgName orgName = organizations.get(0).getOrgName();
					return orgName == null ? "" : orgName.getValue();
				}
			}

			public void setValue(String value) {
				List<Organization> organizations = entry.getOrganizations();
				if (organizations.size() < 1) {
					Organization organization = new Organization();
					organization.setRel(Organization.Rel.WORK);
					organization.setOrgName(new OrgName(value));
					entry.addOrganization(organization);
				} else {
					organizations.get(0).getOrgName().setValue(value);
				}
			}
		});

		fields.put(Fields.OTHER_PHONE, new PhoneField(PhoneNumber.Rel.OTHER));
		fields.put(Fields.BUSINESS_PHONE, new PhoneField(PhoneNumber.Rel.WORK));
		fields.put(Fields.BUSINESS_PHONE2, new PhoneField(PhoneNumber.Rel.WORK,
				2));
		fields.put(Fields.MOBILE_PHONE, new PhoneField(PhoneNumber.Rel.MOBILE));
		fields.put(Fields.MOBILE_PHONE2, new PhoneField(PhoneNumber.Rel.MOBILE,
				2));
		fields.put(Fields.HOME_PHONE, new PhoneField(PhoneNumber.Rel.HOME));
		fields.put(Fields.HOME_PHONE2, new PhoneField(PhoneNumber.Rel.HOME, 2));

		fields.put(Fields.EMAIL1, new EmailField(Email.Rel.OTHER, 1));
		fields.put(Fields.EMAIL2, new EmailField(Email.Rel.OTHER, 2));
		fields.put(Fields.EMAIL3, new EmailField(Email.Rel.OTHER, 3));

		fields.put(Fields.HOME_ADDR, new PostalField(Email.Rel.HOME));
		fields.put(Fields.BUSINESS_ADDR, new PostalField(Email.Rel.WORK));

		fields.put(Fields.OUTID, new FieldInterface() {

			public String getValue() {
				if (entry.getExternalIds().size() < 1)
					return "";
				return entry.getExternalIds().get(0).getValue();
			}

			public void setValue(String value) {
				if (entry.getExternalIds().size() < 1) {
					entry.addExternalId(new ExternalId("Outlook", null, value));
				} else {
					entry.getExternalIds().get(0).setValue(value);
				}
			}
		});

	}

	public String getFirstName() {
		fetchName();
		return first;
	}

	public void setFirstName(String name) {
		this.first = name;
		saveName();
	}

	public String getMiddleName() {
		fetchName();
		return middle;
	}

	public void setMiddleName(String name) {
		this.middle = name;
		saveName();
	}

	public ContactEntry getUnderlyingEntry() {
		return entry;
	}

	public long getUpdateTime() {
		return this.entry.getUpdated().getValue();
	}

	public String getLastName() {
		fetchName();
		return last;
	}

	public String getTitleName() {
		fetchName();
		return title;
	}

	public void setLastName(String name) {
		this.last = name;
		saveName();
	}

	public void setTitleName(String name) {
		this.title = name;
		saveName();
	}

	private void saveName() {
		if (title == null || title.length() < 1)
			title = "";
		if (first == null || first.length() < 1)
			first = "";
		if (middle == null || middle.length() < 1)
			middle = "";
		if (last == null || last.length() < 1)
			last = "";
		String mid = (middle.length() < 1) ? "" : middle + " ";
		this.entry.setTitle(new PlainTextConstruct(first + " " + mid + last));
		Name name = new Name();
		if (first != "")
			name.setGivenName(new GivenName(first, null));
		if (middle != "")
			name.setAdditionalName(new AdditionalName(middle, null));
		if (last != "")
			name.setFamilyName(new FamilyName(last, null));
		if (title != "")
			name.setNamePrefix(new NamePrefix(title));
		this.entry.setName(name);
	}

	private void fetchName() {
		this.first = "";
		this.middle = "";
		this.last = "";
		this.title = "";
		Name name = entry.getName();
		String tit = entry.getTitle().getPlainText();
		if (name != null) {
			first = name.getGivenName() == null ? "" : name.getGivenName()
					.getValue();
			middle = name.getAdditionalName() == null ? "" : name
					.getAdditionalName().getValue();
			last = name.getFamilyName() == null ? "" : name.getFamilyName()
					.getValue();
			title = name.getNamePrefix() == null ? "" : name.getNamePrefix()
					.getValue();
		}
		if ((first + last + title).length() < 1) {
			int index = tit.lastIndexOf(' ');
			if (index < 0) {
				first = tit;
			} else {
				last = tit.substring(index + 1);
				first = tit.substring(0, index);
				index = first.indexOf(' ');
				if (index > 0) {
					first = first.substring(0, index);
					middle = first.substring(index + 1);
				}
			}
		}
	}

	public String getGoogleContactID() {
		return entry.getId();
	}

	@Override
	public void save() {
		if (changed) {
			if (newContact) {
				try {
					entry = myService.insert(feedUrl, entry);
					updatePicture();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ServiceException e) {
					e.printStackTrace();
				}
			} else {
				try {
					entry = entry.update();
					updatePicture();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ServiceException e) {
					e.printStackTrace();
				}
			}
			changed = false;
		}

	}

	private void updatePicture() {
		if (pictureBytes != null) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				Link photoLink = entry.getContactPhotoLink();
				URL photoUrl = new URL(photoLink.getHref());
				GDataRequest request = myService.createRequest(
						GDataRequest.RequestType.UPDATE, photoUrl,
						new ContentType("image/jpeg"));

				request.setEtag(photoLink.getEtag());

				OutputStream requestStream = request.getRequestStream();
				requestStream.write(pictureBytes);

				request.execute();
				request.end();
				pictureChanged = false;
				loadPicture();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	@Override
	public void delete() {
		try {
			entry.delete();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		}
	}

	class PhoneField implements FieldInterface {

		final String relType;

		final int count;

		public PhoneField(String relType, int count) {
			this.relType = relType;
			this.count = count;
		}

		public PhoneField(String relType) {
			this(relType, 1);
		}

		public String getValue() {
			List<PhoneNumber> phoneNumbers = entry.getPhoneNumbers();
			int c = 0;
			for (PhoneNumber number : phoneNumbers) {
				if (number.getRel().equals(relType)) {
					if (++c == count) {
						return number.getPhoneNumber();
					}
				}
			}
			return "";
		}

		public void setValue(String value) {
			List<PhoneNumber> phoneNumbers = entry.getPhoneNumbers();
			int c = 0;
			for (PhoneNumber number : phoneNumbers) {
				if (number.getRel().equals(relType)) {
					c++;
					if (c == count) {
						if (value.length() < 1) {
							phoneNumbers.remove(number);
							return;
						} else {
							number.setPhoneNumber(value);
							return;
						}
					} else {
					}
				}
			}
			if (value == null || value.length() < 1) {
				return;
			}
			PhoneNumber phoneNumber = new PhoneNumber();
			phoneNumber.setRel(relType);
			phoneNumber.setPhoneNumber(value);
			entry.addPhoneNumber(phoneNumber);
		}
	}

	class EmailField implements FieldInterface {

		final String relType;

		final int count;

		public EmailField(String relType, int count) {
			this.relType = relType;
			this.count = count;
		}

		public EmailField(String relType) {
			this(relType, 1);
		}

		public String getValue() {
			List<Email> emailAddrs = entry.getEmailAddresses();
			int c = 0;
			for (Email email : emailAddrs) {
				if (email.getRel().equals(relType)) {
					if (++c == count) {
						return email.getAddress();
					}
				}
			}
			return "";
		}

		public void setValue(String value) {
			List<Email> emailAddrs = entry.getEmailAddresses();
			int c = 0;
			for (Email email : emailAddrs) {
				if (email.getRel().equals(relType)) {
					c++;
					if (c == count) {
						if (value.length() < 1) {
							emailAddrs.remove(email);
							return;
						} else {
							email.setAddress(value);
							return;
						}
					} else {
					}
				}
			}
			if (value == null || value.length() < 1) {
				return;
			}
			Email email = new Email();
			email.setRel(relType);
			email.setAddress(value);
			entry.addEmailAddress(email);
		}
	}

	class PostalField implements FieldInterface {

		final String relType;

		final int count;

		public PostalField(String relType, int count) {
			this.relType = relType;
			this.count = count;
		}

		public PostalField(String relType) {
			this(relType, 1);
		}

		public String getValue() {
			List<StructuredPostalAddress> postalAddress = entry
					.getStructuredPostalAddresses();
			int c = 0;
			for (StructuredPostalAddress postal : postalAddress) {
				if (postal.getRel().equals(relType)) {
					if (++c == count) {
						FormattedAddress formattedAddress = postal
								.getFormattedAddress();
						return formattedAddress == null ? "" : formattedAddress
								.getValue();
					}
				}
			}
			return "";
		}

		public void setValue(String value) {
			List<StructuredPostalAddress> postalAddrs = entry
					.getStructuredPostalAddresses();
			int c = 0;
			FormattedAddress formattedAddress = new FormattedAddress(value);
			for (StructuredPostalAddress postal : postalAddrs) {
				if (postal.getRel().equals(relType)) {
					c++;
					if (c == count) {
						if (value.length() < 1) {
							postalAddrs.remove(postal);
							return;
						} else {
							postal.setFormattedAddress(formattedAddress);
							return;
						}
					}
				}
			}
			if (value == null || value.length() < 1) {
				return;
			}
			StructuredPostalAddress postal = new StructuredPostalAddress();
			postal.setRel(relType);
			postal.setFormattedAddress(formattedAddress);
			entry.addStructuredPostalAddress(postal);
		}
	}

	private void setAddress(StructuredPostalAddress postal, String value) {
		try {
			Properties props = new Properties();
			props.load(new StringReader(value));
			postal.setStreet(new Street(props.getProperty("Street", "")));
			postal.setPostcode(new PostCode(props.getProperty("Postcode", "")));
			postal.setCity(new City(props.getProperty("City", "")));
			postal.setHousename(new HouseName(props
					.getProperty("Housename", "")));
			postal.setNeighborhood(new Neighborhood(props.getProperty(
					"Neighborhood", "")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private byte[] pictureBytes = null;

	@Override
	public byte[] getPictureBytes() {
		return pictureBytes;
	}

	@Override
	public String getPictureHash() {
		return getHashFromBytes(getPictureBytes());
	}

	@Override
	public void setPictureBytes(byte[] pictureBytes) {
		this.changed = true;
		this.pictureChanged = true;
		this.pictureBytes = pictureBytes;
	}

}
