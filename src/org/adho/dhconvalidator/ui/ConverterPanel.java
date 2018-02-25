/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import de.catma.backgroundservice.DefaultProgressCallable;
import de.catma.backgroundservice.ExecutionListener;
import de.catma.backgroundservice.ProgressListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.adho.dhconvalidator.Messages;
import org.adho.dhconvalidator.conversion.ConversionPath;
import org.adho.dhconvalidator.conversion.ConversionProgressListener;
import org.adho.dhconvalidator.conversion.Converter;
import org.adho.dhconvalidator.conversion.oxgarage.ZipResult;
import org.adho.dhconvalidator.properties.PropertyKey;
import org.adho.dhconvalidator.user.User;
import org.adho.dhconvalidator.util.Pair;

/**
 * The Panel that lets the user perform the conversion.
 *
 * @author marco.petris@web.de
 */
public class ConverterPanel extends VerticalLayout implements View {

  private static final Logger LOGGER = Logger.getLogger(ConverterPanel.class.getName());

  private Upload upload;
  private ByteArrayOutputStream uploadContent;
  private String filename;
  private ProgressBar progressBar;
  private Label logArea;
  private Label preview;
  private HorizontalSplitPanel resultPanel;
  private Label resultCaption;
  private Button btDownloadResult;
  private Label downloadInfo;

  private FileDownloader currentFileDownloader;

  private Label confToolLabel;

  public ConverterPanel() {
    initComponents();
    initActions();
  }

  /** Setup behaviour. */
  private void initActions() {
    upload.addSucceededListener(
        new SucceededListener() {

          @Override
          public void uploadSucceeded(SucceededEvent event) {

            // the upload has successfully been finished

            try {
              // uploadedContent has been filled by the Receiver of this upload component
              // see initComponents below
              final byte[] uploadData = uploadContent.toByteArray();
              // do we have data?
              if (uploadData.length == 0) {
                Notification.show(
                    Messages.getString("ConverterPanel.fileSelectionTitle"), // $NON-NLS-1$
                    Messages.getString("ConverterPanel.fileSelectionMsg"), // $NON-NLS-1$
                    Type.TRAY_NOTIFICATION);
              } else {
                appendLogMessage(Messages.getString("ConverterPanel.progress1")); // $NON-NLS-1$

                // ok, let's do the conversion in the background
                ((DHConvalidatorServices) UI.getCurrent())
                    .getBackgroundService()
                    .submit(
                        new DefaultProgressCallable<Pair<ZipResult, String>>() {
                          @Override
                          public Pair<ZipResult, String> call() throws Exception {
                            // background execution!

                            // do the actual conversion
                            Converter converter =
                                new Converter(PropertyKey.oxgarage_url.getValue());

                            ZipResult zipResult =
                                converter.convert(
                                    uploadData,
                                    ConversionPath.getConversionPathByFilename(filename),
                                    (User)
                                        VaadinSession.getCurrent()
                                            .getAttribute(SessionStorageKey.USER.name()),
                                    filename,
                                    new ConversionProgressListener() {

                                      @Override
                                      public void setProgress(String msg) {
                                        getProgressListener().setProgress(msg);
                                      }
                                    });

                            return new Pair<ZipResult, String>(
                                zipResult, converter.getContentAsXhtml());
                          }
                        },
                        new ExecutionListener<Pair<ZipResult, String>>() {
                          @Override
                          public void done(Pair<ZipResult, String> result) {
                            // back to GUI foreground at this point
                            String originalExtension =
                                filename.substring(filename.lastIndexOf('.') + 1);

                            filename =
                                result
                                        .getFirst()
                                        .getDocumentName()
                                        .substring(
                                            0, result.getFirst().getDocumentName().lastIndexOf('.'))
                                    + "."
                                    + originalExtension;

                            // we store the result in the session for the
                            // ExternalResourceRequestHandler
                            VaadinSession.getCurrent()
                                .setAttribute(
                                    SessionStorageKey.ZIPRESULT.name(), result.getFirst());

                            appendLogMessage(
                                Messages.getString("ConverterPanel.progress2")); // $NON-NLS-1$

                            preview.setValue(result.getSecond());
                            resultCaption.setValue(
                                Messages.getString(
                                    "ConverterPanel.previewTitle", filename)); // $NON-NLS-1$
                            prepareForResultDownload();
                            progressBar.setVisible(false);
                          }

                          @Override
                          public void error(Throwable t) {
                            // an error during background conversion
                            LOGGER.log(
                                Level.SEVERE,
                                Messages.getString("ConverterPanel.conversionErrorMsg"),
                                t); // $NON-NLS-1$
                            String message = t.getLocalizedMessage();
                            if (message == null) {
                              message =
                                  Messages.getString(
                                      "ConverterPanel.conversionErrorNullReplacement"); // $NON-NLS-1$
                            }
                            appendLogMessage(
                                Messages.getString(
                                    "ConverterPanel.errorLogMsg", message)); // $NON-NLS-1$
                            progressBar.setVisible(false);
                          }
                        },
                        new ProgressListener() {

                          @Override
                          public void setProgress(String value, Object... args) {
                            appendLogMessage(value);
                          }
                        });
              }
            } catch (Exception e) {
              // we don't expect many problems at this point since the main work takes place in the
              // background
              // but just in case
              LOGGER.log(
                  Level.SEVERE,
                  Messages.getString("ConverterPanel.syncErrorMsg"),
                  e); // $NON-NLS-1$
              String message = e.getLocalizedMessage();
              if (message == null) {
                message =
                    Messages.getString(
                        "ConverterPanel.conversionErrorNullReplacement"); // $NON-NLS-1$
              }
              appendLogMessage(
                  Messages.getString("ConverterPanel.errorLogMsg", message)); // $NON-NLS-1$
            }
          }
        });
    upload.addStartedListener(
        new StartedListener() {

          @Override
          public void uploadStarted(StartedEvent event) {
            if (!event.getFilename().isEmpty()) {
              // clean everything for the new conversion
              cleanUp();
              progressBar.setVisible(true);
              //					UI.getCurrent().push();
            }
          }
        });

    upload.addFailedListener(
        new FailedListener() {

          @Override
          public void uploadFailed(FailedEvent event) {
            resultCaption.setValue(
                Messages.getString("ConverterPanel.previewTitle", filename)); // $NON-NLS-1$
            progressBar.setVisible(false);
          }
        });
  }

  /** Provides a new FileDownloader with the conversion result. */
  private void prepareForResultDownload() {
    downloadInfo.setVisible(true);

    // detach the old file downloader
    if (currentFileDownloader != null) {
      currentFileDownloader.remove();
    }

    StreamResource resultStreamResource =
        new StreamResource(
            new StreamSource() {

              @Override
              public InputStream getStream() {
                return createResultStream();
              }
            },
            filename.substring(0, filename.lastIndexOf('.')) + ".dhc"); // $NON-NLS-1$

    resultStreamResource.setCacheTime(0);

    currentFileDownloader = new FileDownloader(resultStreamResource);
    currentFileDownloader.extend(btDownloadResult);

    btDownloadResult.setVisible(true);
  }

  private void appendLogMessage(String logmesssage) {
    StringBuilder logBuilder =
        new StringBuilder((logArea.getValue() == null) ? "" : logArea.getValue()); // $NON-NLS-1$

    logBuilder.append("<br>"); // $NON-NLS-1$
    logBuilder.append(logmesssage);
    logArea.setReadOnly(false);
    logArea.setValue(logBuilder.toString());
    logArea.setReadOnly(true);
  }

  /** Setup GUI. */
  private void initComponents() {
    setMargin(true);
    setSizeFull();
    setSpacing(true);
    HeaderPanel headerPanel = new HeaderPanel(null);
    addComponent(headerPanel);

    Label title = new Label(Messages.getString("ConverterPanel.title")); // $NON-NLS-1$
    title.addStyleName("title-caption"); // $NON-NLS-1$
    addComponent(title);
    setComponentAlignment(title, Alignment.TOP_LEFT);

    Label info =
        new Label(
            Messages.getString("ConverterPanel.info"), // $NON-NLS-1$
            ContentMode.HTML);
    addComponent(info);

    HorizontalLayout inputPanel = new HorizontalLayout();
    inputPanel.setSpacing(true);
    addComponent(inputPanel);

    upload =
        new Upload(
            Messages.getString("ConverterPanel.uploadCaption"), // $NON-NLS-1$
            new Receiver() {
              @Override
              public OutputStream receiveUpload(String filename, String mimeType) {
                // we store the uploaded content in the panel instance

                ConverterPanel.this.filename = filename;
                ConverterPanel.this.uploadContent = new ByteArrayOutputStream();

                return ConverterPanel.this.uploadContent;
              }
            });

    inputPanel.addComponent(upload);

    progressBar = new ProgressBar();
    progressBar.setIndeterminate(true);
    progressBar.setVisible(false);
    inputPanel.addComponent(progressBar);
    inputPanel.setComponentAlignment(progressBar, Alignment.MIDDLE_CENTER);
    progressBar.addStyleName("converterpanel-progressbar"); // $NON-NLS-1$

    resultCaption = new Label(Messages.getString("ConverterPanel.previewTitle2")); // $NON-NLS-1$
    resultCaption.setWidth("100%"); // $NON-NLS-1$
    resultCaption.addStyleName("converterpanel-resultcaption"); // $NON-NLS-1$
    addComponent(resultCaption);
    setComponentAlignment(resultCaption, Alignment.MIDDLE_CENTER);

    resultPanel = new HorizontalSplitPanel();
    addComponent(resultPanel);
    resultPanel.setSizeFull();
    setExpandRatio(resultPanel, 1.0f);

    preview = new Label("", ContentMode.HTML); // $NON-NLS-1$
    preview.addStyleName("tei-preview"); // $NON-NLS-1$
    resultPanel.addComponent(preview);
    VerticalLayout rightPanel = new VerticalLayout();
    rightPanel.setMargin(new MarginInfo(false, false, true, true));
    rightPanel.setSpacing(true);
    resultPanel.addComponent(rightPanel);

    logArea = new Label("", ContentMode.HTML); // $NON-NLS-1$
    logArea.setSizeFull();
    logArea.setReadOnly(true);
    rightPanel.addComponent(logArea);

    downloadInfo = new Label(Messages.getString("ConverterPanel.downloadMsg")); // $NON-NLS-1$
    rightPanel.addComponent(downloadInfo);
    downloadInfo.setVisible(false);

    btDownloadResult =
        new Button(Messages.getString("ConverterPanel.downloadBtCaption")); // $NON-NLS-1$
    btDownloadResult.setVisible(false);
    rightPanel.addComponent(btDownloadResult);
    rightPanel.setComponentAlignment(btDownloadResult, Alignment.BOTTOM_CENTER);
    btDownloadResult.setHeight("50px"); // $NON-NLS-1$

    rightPanel.addComponent(
        new Label(Messages.getString("ConverterPanel.exampleMsg"))); // $NON-NLS-1$
    Button btExample =
        new Button(Messages.getString("ConverterPanel.exampleButtonCaption")); // $NON-NLS-1$
    btExample.setStyleName(BaseTheme.BUTTON_LINK);
    btExample.addStyleName("plain-link"); // $NON-NLS-1$
    rightPanel.addComponent(btExample);

    confToolLabel =
        new Label(
            Messages.getString(
                "ConverterPanel.gotoConfToolMsg", // $NON-NLS-1$
                PropertyKey.conftool_login_url.getValue()),
            ContentMode.HTML);
    confToolLabel.setVisible(false);
    confToolLabel.addStyleName("postDownloadInfoRedAndBold");

    rightPanel.addComponent(confToolLabel);

    new BrowserWindowOpener(DHConvalidatorExample.class).extend(btExample);
  }

  /** @return the download stream of the {@link ZipResult}-data. */
  private InputStream createResultStream() {
    try {
      ZipResult result =
          (ZipResult) VaadinSession.getCurrent().getAttribute(SessionStorageKey.ZIPRESULT.name());
      logArea.setValue("");
      confToolLabel.setVisible(true);
      //			UI.getCurrent().push();

      return new ByteArrayInputStream(result.toZipData());
    } catch (IOException e) {
      e.printStackTrace();
      Notification.show(
          Messages.getString("ConverterPanel.resultCreationErrorTitle"), // $NON-NLS-1$
          Messages.getString("ConverterPanel.resultCreationErrorMsg"), // $NON-NLS-1$
          Type.ERROR_MESSAGE);
      return null;
    }
  }

  /* (non-Javadoc)
   * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
   */
  @Override
  public void enter(ViewChangeEvent event) {
    VaadinSession.getCurrent().setAttribute(SessionStorageKey.ZIPRESULT.name(), null);
    cleanUp();
    resultCaption.setValue(Messages.getString("ConverterPanel.previewTitle2")); // $NON-NLS-1$
    Page.getCurrent().setTitle(Messages.getString("ConverterPanel.pageTitle")); // $NON-NLS-1$
  }

  private void cleanUp() {
    btDownloadResult.setVisible(false);
    downloadInfo.setVisible(false);
    preview.setValue(""); // $NON-NLS-1$
    logArea.setValue(""); // $NON-NLS-1$
    confToolLabel.setVisible(false);
  }
}
