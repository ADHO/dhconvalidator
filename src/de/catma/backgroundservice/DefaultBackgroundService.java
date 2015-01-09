package de.catma.backgroundservice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultBackgroundService implements BackgroundService {
	private ExecutorService backgroundThread;
	private boolean background = true;
	private Object lock;

	/**
	 * setup the worker thread
	 */
	public DefaultBackgroundService(Object lock) {
		this(lock, true);
	}

	public DefaultBackgroundService(Object lock, boolean background) {
		this.lock = lock;
		this.background = background;
		if (background) {
			backgroundThread = Executors.newSingleThreadExecutor();
		}
	}
	
	/**
	 * @param <T> the type of the {@link ExecutionListener} and 
	 * 		the {@link ProgressCallable}.
	 *  
	 * @param callable the task which should be done in the background 
	 * @param listener this one will be notified when the task is 
	 * 		done (within the initiating thread)
	 * @param progressListener the listener for progress. The implementer 
	 *		of the {@link ProgressCallable} which defines the task can
	 *		call this listener to notify progress to the initiating thread.
	 */
	public <T> void submit( 
			final ProgressCallable<T> callable, 
			final ExecutionListener<T> listener,
			final ProgressListener progressListener) {
		
        if (background) {
            backgroundThread.submit( new Runnable() {
                public void run() {
                    try {
                        callable.setProgressListener( progressListener );
                        final T result = callable.call();
                        
                        synchronized(lock) {
                        	listener.done(result);
                        }
                    } catch (Throwable t) {
                        try {
                        	Logger.getLogger(
                        			getClass().getName()).log(
                        					Level.SEVERE, "error", t);
                            synchronized(lock) {
                            	listener.error(t);
                            }
                        }
                        catch(Throwable t2) {
                        	t2.printStackTrace();
                        }
                    }
                }
            } );
        }
        else {
            try {
                callable.setProgressListener( progressListener );
                final T result = callable.call();
                listener.done( result );

            } catch (Throwable t) {
            	listener.error(t);
            	Logger.getLogger(getClass().getName()).log(
            		Level.SEVERE, "error", t);  
            }
        }
		
	}
	

}
