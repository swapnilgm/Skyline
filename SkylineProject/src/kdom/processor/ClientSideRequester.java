package kdom.processor;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import kdom.core.RequestType;
import kdom.core.SiteDetail;

import org.apache.log4j.Logger;

/**
 * 
 */

/**
 * @author Swapnil
 *
 */
public abstract class ClientSideRequester implements Runnable {

	private static Logger logger = Logger.getLogger(ClientSideRequester.class);

	private Socket socket ;

	private SiteDetail siteDetails;
	/**
	 * 
	 */
	public ClientSideRequester(SiteDetail siteDetail) {
		this.siteDetails = siteDetail;
		try {
			this.socket = new Socket(siteDetail.getIpAddress(), siteDetail.getPort());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * @return the socket
	 */
	protected Socket getSocket() {
		return socket;
	}


	public SiteDetail getSiteDetails(){
		return this.siteDetails;
	}


	public void run() {
		sendRequest();		
		getResponse();
		try {
			this.socket.close();
		} catch (IOException e) {
			logger.error("Error while closing requester socket ::\n" + e.getMessage());
		}
	}	

	/**
	 * Send request with specified request type over network
	 */
	protected void sendRequest(){
		RequestType requestType = getRequestType();
		try {
			logger.debug("Sending request to "+this.siteDetails+" ::"+ requestType);
			OutputStream outputStream = this.socket.getOutputStream();
			PrintWriter printWriter = new PrintWriter(outputStream, true);
			printWriter.println(requestType.toString());
			String reuqestPayload = getRequestPayLoad();
			if(reuqestPayload != null)
				printWriter.println(reuqestPayload);
			printWriter.flush();
			logger.debug("Request sent "+this.siteDetails+" ::"+ requestType);
		} catch (IOException e) {
			logger.error("Error while sending request ::\n"+ e.getMessage());
		}
	}

	protected abstract void getResponse();

	protected abstract RequestType getRequestType();

	protected abstract String getRequestPayLoad();

}

