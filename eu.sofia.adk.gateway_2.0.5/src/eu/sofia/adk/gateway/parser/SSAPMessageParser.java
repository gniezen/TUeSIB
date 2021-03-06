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

package eu.sofia.adk.gateway.parser;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eu.sofia.adk.ssapmessage.SSAPMessage;
import eu.sofia.adk.ssapmessage.SSAPMessageRequest;
import eu.sofia.adk.ssapmessage.SSAPMessage.TransactionType;
import eu.sofia.adk.ssapmessage.parameter.SSAPMessageParameter;
import eu.sofia.adk.ssapmessage.parameter.SSAPMessageRDFParameter;
import eu.sofia.adk.ssapmessage.parameter.SSAPMessageParameter.NameAttribute;
import eu.sofia.adk.ssapmessage.parameter.SSAPMessageRDFParameter.TypeAttribute;

/**
 * This class is responsible for parsing 
 * an SSAPMessage
 *  
 * @author Raul Otaolea, Raul.Otaolea@tecnalia.com, Tecnalia
 * @author Fran Ruiz, Fran.Ruiz@tecnalia.com, Tecnalia
 */
public class SSAPMessageParser {
	
	/** The tags that are defined in a SSAPMessage */
	private static final String SSAP_MESSAGE		= "SSAP_message";
	private static final String NODE_ID 			= "node_id";
	private static final String SPACE_ID 			= "space_id";
	private static final String TRANSACTION_ID 		= "transaction_id";
	private static final String TRANSACTION_TYPE 	= "transaction_type";
	private static final String MESSAGE_TYPE 		= "message_type";

	private static final String PARAMETER 			= "parameter";
	
	/**
	 * Parses the message from a byte array to a ssap message
	 * @param rawMsg the raw message
	 * @return a ssap message
	 * @throws SSAPMessageParseException when some error arise
	 */
	public static SSAPMessageRequest parse(byte[] rawMsg) throws SSAPMessageParseException {
		// The response
		SSAPMessageRequest request = null;
		
		try {
			/** The DOM model */
			Document document;
			
			// Creates an instance the factory that gives us the 
			// document builder
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			// Gets the document builder and parses the reference file
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new SSAPMessageErrorHandler());

			// Gets the input source
			InputSource is = new InputSource(new StringReader(new String(rawMsg)));
			
			document = builder.parse(is);
			document.getDocumentElement().normalize();
			
			request = parse(document.getDocumentElement());
		} catch (SAXParseException spe) {
			// Error generated by the parser
			StringBuilder sb = new StringBuilder();
			throw new SSAPMessageParseException(sb.toString(), spe.getException());
		} catch (SAXException sxe) {
			  
			// Error generated during parsing
		    Exception x = sxe;
		    if (sxe.getException() != null) {
		    	x = sxe.getException();
		    }
		    x.printStackTrace();
		    throw new SSAPMessageParseException(sxe.getMessage(), sxe.getException());
		} catch (ParserConfigurationException pce) {
		    // Parser with specified options can't be built
		    pce.printStackTrace();
		    throw new SSAPMessageParseException(pce.getMessage());
		} catch (IOException ioe) {
		    throw new SSAPMessageParseException(ioe.getMessage());
		} catch (Exception ex) {
		    throw new SSAPMessageParseException(ex.getMessage());			
		}			
	
		return request;
	}
	
	/**
	 * Parses a document into a SSAPMessageRequest
	 * @param document a document with a content
	 * @return a SSAPMessageRequest
	 * @throws SSAPMessageParseException 
	 */
	private static SSAPMessageRequest parse(Element rootElement) throws SSAPMessageParseException {
		
		// The response
		SSAPMessageRequest request = null;		
		
		try {
			// Gets the node id content
			String nodeId = getElement(rootElement, NODE_ID);
			
			// Gets the smart space id content
			String smartSpaceId = getElement(rootElement, SPACE_ID);
			
			// Gets the transaction type content
			String transactionType = getElement(rootElement, TRANSACTION_TYPE);
			TransactionType tranType = getTransactionType(transactionType);
	
			// Gets the message type content
			String messageType = getElement(rootElement, MESSAGE_TYPE);
	
			// Gets the message type content
			String transactionId = getElement(rootElement, TRANSACTION_ID);
			
			// Check if the message is a REQUEST message
			if (!messageType.equals(SSAPMessage.MessageType.REQUEST.toString())) {
				throw new SSAPMessageParseException("The SSAPMessage is not of the REQUEST type");
			}
			
			request = new SSAPMessageRequest(nodeId, smartSpaceId, new Long(transactionId), tranType);
			
			if (!tranType.equals(TransactionType.LEAVE)) {
				// Parse the params
				Collection<SSAPMessageParameter> params = listParameters(rootElement);
				
				request.addParameters(params);
			}
		} catch (SSAPMessageParseException ex) {
			throw new SSAPMessageParseException(ex.getMessage());
		}
		
		return request;
	}
	
	/**
	 * Gets the parameters
	 * @param element the element
	 * @return the node list
	 * @throws SSAPMessageParseException 
	 */
	private static Collection<SSAPMessageParameter> listParameters(Element element) throws SSAPMessageParseException {
		
		Vector<SSAPMessageParameter> vParameters = new Vector<SSAPMessageParameter>();
		
		StringBuilder xpathExpression = new StringBuilder();
		xpathExpression.append("//").append(SSAP_MESSAGE).append("/").append(PARAMETER);
		
		try {
			// Gets the params
			NodeList nl = XPathAPI.selectNodeList(element,xpathExpression.toString());
			
			// Explore all the params
			int len = nl.getLength();
			for(int i = 0; i < len; i++) {
				// Get the node
				Node node = nl.item(i);
				
				// Gets the attributes corresponding to the node
				NamedNodeMap attrs = node.getAttributes();
				
				// Gets the name and type attributes
				Node attrName = attrs.getNamedItem("name");
				Node attrType = attrs.getNamedItem("type");
				Node attrEncoding = attrs.getNamedItem("encoding");
				Node attrFormat = attrs.getNamedItem("format");
				
				NameAttribute na = null;
				if(attrName == null) {
					throw new SSAPMessageParseException("Invalid parameter. Attribute 'name' missing");
				} else {
					na = NameAttribute.findValue(attrName.getNodeValue());
					if(na == null) {
						throw new SSAPMessageParseException("Invalid parameter. Attribute '"+attrName.getNodeValue()+"' unknown");
					}
				}
				
				TypeAttribute ta = null;
				if(attrEncoding != null) {
					ta = TypeAttribute.findValue(attrEncoding.getNodeValue());
				} else if (attrType != null) {
					ta = TypeAttribute.findValue(attrType.getNodeValue());
				} else if (attrFormat != null) {
					ta = TypeAttribute.findValue(attrFormat.getNodeValue());
				}
				
				Node contentNode;
				if(na == NameAttribute.CONFIRM || na == NameAttribute.STATUS 
						|| na == NameAttribute.SUBSCRIPTIONID || na == NameAttribute.CREDENTIALS
						|| na == NameAttribute.ENCODING || na == NameAttribute.TYPE || na == NameAttribute.FORMAT) {
					contentNode = node.getFirstChild();
				} else {
					contentNode = node.getFirstChild();
					short nodeType = contentNode.getNodeType();
					while(contentNode != null && (nodeType != Node.ELEMENT_NODE && nodeType != Node.CDATA_SECTION_NODE)) {
						contentNode = contentNode.getNextSibling();
						nodeType = contentNode.getNodeType();
					}
					if(contentNode == null || (nodeType != Node.ELEMENT_NODE && nodeType != Node.CDATA_SECTION_NODE)) {
						throw new SSAPMessageParseException("Invalid parameter '"+attrName.getNodeValue()+"'. Content missing");
					}
				}
				
				String content = SSAPMessageParser.DOM2String(contentNode);
				if(content == null || content.length() == 0) {
					throw new SSAPMessageParseException("Invalid parameter '" + na.getValue() + "' content");
				}
				SSAPMessageParameter param;
				if(ta != null) {
					param = new SSAPMessageRDFParameter(na, ta, content);
				} else {
					param = new SSAPMessageParameter(na, content);
				}
				vParameters.add(param);
			}
		} catch (TransformerException e) {
			throw new SSAPMessageParseException("Error parsing SSAP parameters", e);
		}

		return vParameters;
	}
	
	private static String DOM2String(Node node) {
		
		try {
			// Set up the output transformer
	    	Transformer trans = TransformerFactory.newInstance().newTransformer();
	    	trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    	trans.setOutputProperty(OutputKeys.INDENT, "yes");

	    	// Print the DOM node
	    	StringWriter sw = new StringWriter();
	    	StreamResult result = new StreamResult(sw);
	    	DOMSource source = new DOMSource(node);
	    	trans.transform(source, result);
	    	
	    	return sw.toString();
	    } catch (TransformerException e) {
	    	e.printStackTrace();
	    	return null;
	    }
	}
	
	/**
	 * Gets the transaction type corresponding to a string
	 * @param transactionType a string
	 * @return the corresponding transaction type
	 */
	private static TransactionType getTransactionType(String transactionType) {
		TransactionType returnType = null;
		for (TransactionType tranType : TransactionType.values()) {
			if (tranType.getType().equals(transactionType)) {
				returnType = tranType;
			}
		}
		return returnType;
	}

	/**
	 * Obtains an element for a given node name
	 * @param element the containing element
	 * @param nodeName the node name
	 * @return a String with the node name value
	 * @throws SSAPMessageParseException 
	 */
	private static String getElement(Element element, String nodeName) throws SSAPMessageParseException {

		StringBuilder xpathExpression = new StringBuilder();
		xpathExpression.append("/").append(SSAP_MESSAGE).append("/").append(nodeName);
		
		//Gets the attribute
		Node node;
		try{
			
			node = XPathAPI.selectSingleNode(element, xpathExpression.toString());
						
		}catch(Exception e){
			e.printStackTrace();
			throw new SSAPMessageParseException("Error parsing the message");
		}

		if( node == null || node.getFirstChild().getNodeValue() == null) {
			throw new SSAPMessageParseException("Cannot find the node " + nodeName + " in the SSAP_message");
		} 
		
		return node.getFirstChild().getNodeValue();
	}
}
