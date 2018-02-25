/*
 * Copyright (c) 2015 http://www.adho.org/
 * License: see LICENSE file
 */
package org.adho.dhconvalidator.ui;

import com.vaadin.ui.UI;
import de.catma.backgroundservice.BackgroundService;
import de.catma.backgroundservice.ExecutionListener;
import de.catma.backgroundservice.ProgressCallable;
import de.catma.backgroundservice.ProgressListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A background service that works with Vaadin {@link UI}s.
 *
 * @author marco.petris@web.de
 */
public class UIBackgroundService implements BackgroundService {

  /** a listener that delegates progress updates in a thread safe way. */
  private static class UIProgressListener implements ProgressListener {
    private ProgressListener delegate;

    public UIProgressListener(ProgressListener delegate) {
      super();
      this.delegate = delegate;
    }

    @Override
    public void setProgress(final String value, final Object... args) {
      if (UI.getCurrent() != null && UI.getCurrent().isAttached()) {
        UI.getCurrent()
            .access(
                new Runnable() {
                  public void run() {
                    delegate.setProgress(value, args);
                    UI.getCurrent().push();
                  }
                });
      }
    }
  }

  private ExecutorService backgroundThread;
  private boolean background;

  public UIBackgroundService(boolean background) {
    this.background = background;
    if (background) {
      backgroundThread = Executors.newFixedThreadPool(1);
    }
  }

  /* (non-Javadoc)
   * @see de.catma.backgroundservice.BackgroundService#submit(de.catma.backgroundservice.ProgressCallable, de.catma.backgroundservice.ExecutionListener, de.catma.backgroundservice.ProgressListener)
   */
  @Override
  public <T> void submit(
      final ProgressCallable<T> callable,
      final ExecutionListener<T> listener,
      final ProgressListener progressListener) {
    if (background) {
      backgroundThread.submit(
          new Runnable() {
            public void run() {
              try {
                callable.setProgressListener(new UIProgressListener(progressListener));
                final T result = callable.call();
                if (UI.getCurrent() != null && UI.getCurrent().isAttached()) {
                  UI.getCurrent()
                      .access(
                          new Runnable() {
                            public void run() {
                              listener.done(result);
                              UI.getCurrent().push();
                            }
                          });
                }
              } catch (final Throwable t) {
                try {
                  Logger.getLogger(getClass().getName()).log(Level.SEVERE, "error", t);
                  if (UI.getCurrent() != null && UI.getCurrent().isAttached()) {
                    UI.getCurrent()
                        .access(
                            new Runnable() {
                              public void run() {
                                listener.error(t);
                                UI.getCurrent().push();
                              }
                            });
                  }
                } catch (Throwable t2) {
                  t2.printStackTrace();
                }
              }
            }
          });
    } else {
      try {
        callable.setProgressListener(progressListener);
        final T result = callable.call();
        listener.done(result);

      } catch (Throwable t) {
        listener.error(t);
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "error", t);
      }
    }
  }
}
