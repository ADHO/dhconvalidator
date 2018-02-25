/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;
import de.catma.backgroundservice.BackgroundService;
import java.io.IOException;
import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.conversion.input.docx.DocxInputConverter;
import org.adho.dhconvalidator.conversion.input.odt.OdtInputConverter;
import org.adho.dhconvalidator.properties.PropertyKey;

/**
 * The UI that offers the available services.
 *
 * @author marco.petris@web.de
 */
@Theme("dhconvalidator")
@PreserveOnRefresh
// @Push(value=PushMode.MANUAL, transport=Transport.LONG_POLLING)
public class DHConvalidatorServices extends UI {

  private BackgroundService backgroundService;

  @Override
  protected void init(VaadinRequest request) {

    Messages.setLocaleProvider(VaadinSessionLocaleProvider.INSTANCE);

    backgroundService = new UIBackgroundService(false);

    // are we logged in?
    if ((VaadinSession.getCurrent().getAttribute(SessionStorageKey.USER.name())) == null) {
      // no, show login box then
      setContent(new LoginPanel());
      Page.getCurrent().setTitle(Messages.getString("DHConvalidatorServices.loginTitle"));
    } else {
      // add available services and navigation
      Navigator navigator = new Navigator(this, this);
      navigator.addView("", new ServiceSelectionPanel());
      navigator.addView(
          ServicesViewName.odt.name(), new PaperSelectionPanel(new OdtInputConverter()));
      navigator.addView(
          ServicesViewName.docx.name(), new PaperSelectionPanel(new DocxInputConverter()));
      navigator.addView(ServicesViewName.converter.name(), new ConverterPanel());
      navigator.addView(ServicesViewName.templates.name(), new TemplateGeneratorPanel());
      navigator.addView(ServicesViewName.conftoolupload.name(), new ConfToolUploadPanel());

      // the visual feedback may reference external resources like images
      // the ExternalResourceRequestHandler serves those external resources from
      // the ZipResult of the conversion process
      try {
        VaadinSession.getCurrent()
            .addRequestHandler(
                new ExternalResourceRequestHandler(PropertyKey.tei_image_location.getValue()));
      } catch (IOException e) {
        throw new IllegalStateException(
            Messages.getString("DHConvalidatorServices.errorExampleFiles"), e);
      }
      Page.getCurrent().setTitle(Messages.getString("DHConvalidatorServices.servicesTitle"));
    }
  }

  /** @return a sevice that allows background execution */
  public BackgroundService getBackgroundService() {
    return backgroundService;
  }
}
