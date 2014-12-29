package org.adho.dhconvalidator.conversion.input.docx.paragraphparser;

enum State {
	SEEKPERMSTART(new SeekPermStartHandler()),
	INPERM(new InPermHandler()),
	;
	private StateHandler stateHandler;

	private State(StateHandler stateHandler) {
		this.stateHandler = stateHandler;
	}
	
	public StateHandler getStateHandler() {
		return stateHandler;
	}
	
}
