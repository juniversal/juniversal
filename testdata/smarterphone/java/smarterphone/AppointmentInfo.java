package smarterphone;
public final class AppointmentInfo {
	private String m_id;
	private String m_pin;
	private String m_phoneNumber;
	
	public void setID(String id) {
		this.m_id = id;
	}

	public String id() {
		return m_id;
	}

	public void setPIN(String pin) {
		this.m_pin = pin;
	}

	public String pin() {
		return m_pin;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.m_phoneNumber = phoneNumber;
	}
	
	public String phoneNumber() {
		return this.m_phoneNumber;
	}
}
