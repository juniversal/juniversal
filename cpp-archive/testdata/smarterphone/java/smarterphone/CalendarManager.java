package smarterphone;

public final class CalendarManager {
	public AppointmentInfo getAppointmentInfo(String location, String body) {
		AppointmentInfo appointmentInfo = new AppointmentInfo();

		AppointmentToken token = new AppointmentToken(location);

		while (token.type() != AppointmentToken.EOF) {
			int type = token.type();

			if (type == AppointmentToken.DIGIT_SEQUENCE)
				matchVoiceConferenceInfo(token, appointmentInfo);
		}

		return appointmentInfo;
	}

	private void matchVoiceConferenceInfo(AppointmentToken token, AppointmentInfo appointmentInfo) {
		if (! token.hasLengthInRange(5, 6))
			return;
		
		String id = token.text();
		token.advance();

		if (! token.hasLengthInRange(4, 6))
			return;

		String pin = token.text();
		token.advance();

		if (appointmentInfo.id() == null) {
			appointmentInfo.setID(id);
			appointmentInfo.setPIN(pin);
		}
	}
}
