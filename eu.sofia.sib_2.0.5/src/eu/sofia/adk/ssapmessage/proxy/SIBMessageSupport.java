/*******************************************************************************
 * Copyright (c) 2009,2011 Tecnalia Research and Innovation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Raul Otaolea (Tecnalia Research and Innovation - Software Systems Engineering) - initial API, implementation and documentation
 *    Fran Ruiz (Tecnalia Research and Innovation - Software Systems Engineering) - initial API, implementation and documentation
 *******************************************************************************/ 

package eu.sofia.adk.ssapmessage.proxy;

import java.util.ArrayList;
import java.util.List;

import eu.sofia.adk.ssapmessage.SSAPMessage;

/**
 * Gets the list of the connected listeners
 * 
 * @author Raul Otaolea, raul.otaolea@tecnalia.com, ESI
 *
 */
public class SIBMessageSupport {
	
	/** The list of listeners that are connected to the SIB **/
	protected List<SIBMessageListener> listeners = null;
	
	/**
	 * The constructor.
	 * Inits the list of listeners 
	 */
	public SIBMessageSupport() {
		//super();
		listeners = new ArrayList<SIBMessageListener>( );
	}
  
	/**
	 * Adds a SIB SSAPMessage listener to the list of listeners
	 * @param listener the listener that implements the SIBMessageListener interface
	 */
    public void addListener(SIBMessageListener listener) {
        if(listener == null) {
            throw new IllegalArgumentException("The listener cannot be null");
        }
        synchronized(listeners) {
        	listeners.add(listener);
        }
    }

    /**
     * Removes a listener from the list of listeners
     * @param listener an object implementing the SIBMessageListener interface
     */
    public void removeListener(SIBMessageListener listener) {
    	synchronized(listeners) {
    		listeners.remove(listener);
    	}
    }

    /**
     * Sends the message to all connected listeners
     * @param message the message generated by the SIB
     */
	public void fireMessageReceived(SSAPMessage message) {
		synchronized(listeners) {
			for(SIBMessageListener listener : listeners) {
				listener.messageReceived(message);
			}
		}
	}
	
}
