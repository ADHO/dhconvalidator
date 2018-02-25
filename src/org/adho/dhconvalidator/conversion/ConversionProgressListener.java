/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.conversion;

/**
 * A listener that can be notified of the conversion progress. These messages may be used to notify
 * the user.
 *
 * @author marco.petris@web.de
 */
public interface ConversionProgressListener {
  /** @param msg the message that states about the current progress. */
  public void setProgress(String msg);
}
