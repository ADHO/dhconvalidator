/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.adho.dhconvalidator.Messages;
import org.apache.commons.io.IOUtils;

/**
 * The UI that displays the expample files.
 *
 * @author marco.petris@web.de
 */
@Theme("dhconvalidator")
@PreserveOnRefresh
// @Push(value=PushMode.MANUAL, transport=Transport.LONG_POLLING)
public class DHConvalidatorExample extends UI {

  @Override
  protected void init(VaadinRequest request) {
    try {
      Messages.setLocaleProvider(VaadinSessionLocaleProvider.INSTANCE);

      VerticalLayout content = new VerticalLayout();
      content.setMargin(true);
      content.setSpacing(true);

      setContent(content);

      HeaderPanel headerPanel = new HeaderPanel(null);
      headerPanel.getBackLink().setVisible(false);

      content.addComponent(headerPanel);

      // prepare downloader for input file
      Button btGetInputfile =
          new Button(Messages.getString("DHConvalidatorExample.btInputCaption"));
      content.addComponent(btGetInputfile);

      FileDownloader inputFileDownloader =
          new FileDownloader(
              new StreamResource(
                  new StreamSource() {

                    @Override
                    public InputStream getStream() {
                      return Thread.currentThread()
                          .getContextClassLoader()
                          .getResourceAsStream(
                              "/org/adho/dhconvalidator/conversion/example/1_Digital_Humanities.odt");
                    }
                  },
                  "1_Digital_Humanities.odt"));
      inputFileDownloader.extend(btGetInputfile);

      // prepare downloader for output file
      Button btGetOutputfile =
          new Button(Messages.getString("DHConvalidatorExample.btConversionResultCaption"));
      content.addComponent(btGetOutputfile);

      FileDownloader outputFileDownloader =
          new FileDownloader(
              new StreamResource(
                  new StreamSource() {

                    @Override
                    public InputStream getStream() {
                      return Thread.currentThread()
                          .getContextClassLoader()
                          .getResourceAsStream(
                              "/org/adho/dhconvalidator/conversion/example/1_Digital_Humanities.dhc");
                    }
                  },
                  "1_Digital_Humanities.dhc"));
      outputFileDownloader.extend(btGetOutputfile);

      // setup visual feedback
      Label preview = new Label("", ContentMode.HTML);
      preview.addStyleName("tei-preview");
      preview.setWidth("800px");

      content.addComponent(preview);
      content.setComponentAlignment(preview, Alignment.MIDDLE_CENTER);

      ByteArrayOutputStream buffer = new ByteArrayOutputStream();

      IOUtils.copy(
          Thread.currentThread()
              .getContextClassLoader()
              .getResourceAsStream(
                  "/org/adho/dhconvalidator/conversion/example/1_Digital_Humanities.html"),
          buffer);

      preview.setValue(buffer.toString("UTF-8"));

      Page.getCurrent().setTitle(Messages.getString("DHConvalidatorExample.title"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
