package org.adho.dhconvalidator.conversion.input.docx.paragraphparser;

enum State {
	SEEKSUBTITLE(new SeekSubtitleHandler()),
	FIRSTSUBTITLEFOUND(new SeekSecondSubtitleHandler()),
	SECONDSUBTITLEFOUND(new SecondSubtitleFoundHandler()),
	DONE(null)
	;
	private StateHandler stateHandler;

	private State(StateHandler stateHandler) {
		this.stateHandler = stateHandler;
	}
	
	public StateHandler getStateHandler() {
		return stateHandler;
	}
	
}
