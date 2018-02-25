/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion.input.docx.paragraphparser;

/**
 * States of the ParagraphParser.
 *
 * @author marco.petris@web.de
 */
enum State {
  /** Seeking the start of an editable section. */
  SEEKPERMSTART(new SeekPermStartHandler()),
  /** Within an editable section. */
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
