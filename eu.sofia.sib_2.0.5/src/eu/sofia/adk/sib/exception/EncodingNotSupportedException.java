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

package eu.sofia.adk.sib.exception;

import eu.sofia.adk.ssapmessage.parameter.SSAPMessageRDFParameter.TypeAttribute;

/**
 * The TransformerNotSupportedException captures all the exceptions related with the 
 * messages sent to the SIB.
 * 
 * @author Fran Ruiz, fran.ruiz@tecnalia.com, Tecnalia
 * @author Raul Otaolea, raul.otaolea@tecnalia.com, Tecnalia
 */
@SuppressWarnings("serial")
public class EncodingNotSupportedException extends SIBException {
	
	/**
	 * The public constructor with a message param 
	 * @param message the message
	 */
	public EncodingNotSupportedException(TypeAttribute encoding) {
		super(encoding.getValue() + " is not supported for current SIB");
		this.status = SIBStatus.SSStatus_SIB_Failure_NotImplemented;
	}	
}
