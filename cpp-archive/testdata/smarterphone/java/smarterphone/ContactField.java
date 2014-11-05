package smarterphone;
import java.util.Map;

class ContactField
{
    private String name;
    private String type;
    private String displayName;

    static Map fields;
    static ContactField nullField;
    
    //static void initialize();

    // Q_PROPERTY(QString type READ type CONSTANT FINAL)
    String type() { return type; }

    /* Q_PROPERTY(QString displayName READ displayName CONSTANT FINAL) */
    String displayName() { return displayName; }

    /* Q_PROPERTY(bool emptyField READ emptyField CONSTANT FINAL) */
    //boolean emptyField() { return (*this) == ContactField(); }

    /* Q_INVOKABLE */ ContactField fieldForName(String fieldName)
    {
    	ContactField field = (ContactField) fields.get(fieldName);
    	if (field == null)
    		return nullField;
    	else return field;
    }

    public static int comparePhonePriorities(ContactField field1, ContactField field2)
    {
        int pri1 = getPhonePriority(field1);
        int pri2 = getPhonePriority(field2);
        if (pri1 == pri2)
            return 0;
        return pri1 < pri2 ? -1 : 1;
    }

    public static int getPhonePriority(ContactField field)
    {
        if (field != null) {
            if (field == ContactField.MobilePhone)
                return 0;
            if (field == ContactField.HomeMobilePhone)
                return 1;
            if (field == ContactField.BusinessMobilePhone)
                return 2;
            if (field == ContactField.HomePhone)
                return 3;
            if (field == ContactField.BusinessPhone)
                return 4;
        }
        return 5;
    }

    /*
    // Needed to be a key for QHash
    bool operator==(const ContactField& o) const
    {
        return this->equals(&o);
    }

    bool equals(const ContactField* o) const
    {
        return m_name == o->m_name && m_type == o->m_type && m_displayName == o->m_displayName;
    }

    // Needed to be a key for QMap
    bool operator<(const ContactField& o) const
    {
        return m_name < o.m_name;
    }
    */

    // Fields
    static ContactField Type;
    static ContactField LocalID;

    static ContactField Phone;
    static ContactField HomePhone;
    static ContactField BusinessPhone;

    static ContactField MobilePhone;
    static ContactField HomeMobilePhone;
    static ContactField BusinessMobilePhone;

    static ContactField DTMF;
    static ContactField HomeAddress;
    static ContactField BusinessAddress;
    static ContactField BusinessStreet;
    static ContactField EmailAddress;
    static ContactField IMAddress;
    static ContactField WorkEmail;
    static ContactField ImageURL;
    static ContactField Website;
    static ContactField Menu;

    static ContactField DisplayName;
    static ContactField FirstName;
    static ContactField MiddleName;
    static ContactField LastName;
    static ContactField BusinessName;

    static ContactField Distance;
    static ContactField Latitude;
    static ContactField Longitude;

    static ContactField VoiceConferenceField1;
    static ContactField VoiceConferenceField2;

    // Virtual fields
    static ContactField Title;
    static ContactField Subtitle;

    /*
    class Types
    {
        public static String Phone;
        public static String Email;
        public static String Address;
        public static String URL;
        public static String Name;
        public static String Other;
        public static String Empty;
    };
    */
}

/*

class ContactFieldClass : public QObject
{
    Q_OBJECT
public:
    ContactFieldClass(QObject* parent) : QObject(parent) {}
	
    // Fields
    Q_PROPERTY(ContactField* Type READ Type CONSTANT FINAL)
    ContactField* Type() const { return (ContactField*) ContactField::Type; }

    Q_PROPERTY(ContactField* LocalID READ LocalID CONSTANT FINAL)
    ContactField* LocalID() const { return (ContactField*) ContactField::LocalID; }
	

    Q_PROPERTY(ContactField* Phone READ Phone CONSTANT FINAL)
    ContactField* Phone() const { return (ContactField*) ContactField::Phone; }
    
    Q_PROPERTY(ContactField* HomePhone READ HomePhone CONSTANT FINAL)
    ContactField* HomePhone() const { return (ContactField*) ContactField::HomePhone; }
    
    Q_PROPERTY(ContactField* BusinessPhone READ BusinessPhone CONSTANT FINAL)
    ContactField* BusinessPhone() const { return (ContactField*) ContactField::BusinessPhone; }

	
    Q_PROPERTY(ContactField* MobilePhone READ MobilePhone CONSTANT FINAL)
    ContactField* MobilePhone() const { return (ContactField*) ContactField::MobilePhone; }
    
    Q_PROPERTY(ContactField* HomeMobilePhone READ HomeMobilePhone CONSTANT FINAL)
    ContactField* HomeMobilePhone() const { return (ContactField*) ContactField::HomeMobilePhone; }
    
    Q_PROPERTY(ContactField* BusinessMobilePhone READ BusinessMobilePhone CONSTANT FINAL)
    ContactField* BusinessMobilePhone() const { return (ContactField*) ContactField::BusinessMobilePhone; }

    
    Q_PROPERTY(ContactField* DTMF READ DTMF CONSTANT FINAL)
    ContactField* DTMF() const { return (ContactField*) ContactField::DTMF; }
    
    Q_PROPERTY(ContactField* HomeAddress READ HomeAddress CONSTANT FINAL)
    ContactField* HomeAddress() const { return (ContactField*) ContactField::HomeAddress; }
    
    Q_PROPERTY(ContactField* BusinessAddress READ BusinessAddress CONSTANT FINAL)
    ContactField* BusinessAddress() const { return (ContactField*) ContactField::BusinessAddress; }
    
    Q_PROPERTY(ContactField* BusinessStreet READ BusinessStreet CONSTANT FINAL)
    ContactField* BusinessStreet() const { return (ContactField*) ContactField::BusinessStreet; }
    
    Q_PROPERTY(ContactField* EmailAddress READ EmailAddress CONSTANT FINAL)
    ContactField* EmailAddress() const { return (ContactField*) ContactField::EmailAddress; }
    
    Q_PROPERTY(ContactField* IMAddress READ IMAddress CONSTANT FINAL)
    ContactField* IMAddress() const { return (ContactField*) ContactField::IMAddress; }
    
    Q_PROPERTY(ContactField* WorkEmail READ WorkEmail CONSTANT FINAL)
    ContactField* WorkEmail() const { return (ContactField*) ContactField::WorkEmail; }
    

    Q_PROPERTY(ContactField* ImageUrl READ ImageUrl CONSTANT FINAL)
    ContactField* ImageUrl() const { return (ContactField*) ContactField::ImageURL; }

    Q_PROPERTY(ContactField* Website READ Website CONSTANT FINAL)
    ContactField* Website() const { return (ContactField*) ContactField::Website; }

    Q_PROPERTY(ContactField* Menu READ Menu CONSTANT FINAL)
    ContactField* Menu() const { return (ContactField*) ContactField::Menu; }


    Q_PROPERTY(ContactField* DisplayName READ DisplayName CONSTANT FINAL)
    ContactField* DisplayName() const { return (ContactField*) ContactField::DisplayName; }
    
    Q_PROPERTY(ContactField* FirstName READ FirstName CONSTANT FINAL)
    ContactField* FirstName() const { return (ContactField*) ContactField::FirstName; }
    
    Q_PROPERTY(ContactField* MiddleName READ MiddleName CONSTANT FINAL)
    ContactField* MiddleName() const { return (ContactField*) ContactField::MiddleName; }
    
    Q_PROPERTY(ContactField* LastName READ LastName CONSTANT FINAL)
    ContactField* LastName() const { return (ContactField*) ContactField::LastName; }
    
    Q_PROPERTY(ContactField* BusinessName READ BusinessName CONSTANT FINAL)
    ContactField* BusinessName() const { return (ContactField*) ContactField::BusinessName; }

    
    Q_PROPERTY(ContactField* Distance READ Distance CONSTANT FINAL)
    ContactField* Distance() const { return (ContactField*) ContactField::Distance; }
    
    Q_PROPERTY(ContactField* Latitude READ Latitude CONSTANT FINAL)
    ContactField* Latitude() const { return (ContactField*) ContactField::Latitude; }

    Q_PROPERTY(ContactField* Longitude READ Longitude CONSTANT FINAL)
    ContactField* Longitude() const { return (ContactField*) ContactField::Longitude; }


    Q_PROPERTY(ContactField* VoiceConferenceField1 READ VoiceConferenceField1 CONSTANT FINAL)
    ContactField* VoiceConferenceField1() const { return (ContactField*) ContactField::VoiceConferenceField1; }
    
    Q_PROPERTY(ContactField* VoiceConferenceField2 READ VoiceConferenceField2 CONSTANT FINAL)
    ContactField* VoiceConferenceField2() const { return (ContactField*) ContactField::VoiceConferenceField2; }

    // Virtual fields
    Q_PROPERTY(ContactField* Title READ Title CONSTANT FINAL)
    ContactField* Title() const { return (ContactField*) ContactField::Title; }
    
    Q_PROPERTY(ContactField* Subtitle READ Subtitle CONSTANT FINAL)
    ContactField* Subtitle() const { return (ContactField*) ContactField::Subtitle; }

private:
    Q_DISABLE_COPY(ContactFieldClass)
}

*/
