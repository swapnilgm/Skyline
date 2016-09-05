package kdom.processor;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;

import kdom.core.MBR;
import kdom.core.RequestType;
import kdom.core.SiteDetail;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;


public class MBRRequester extends ClientSideRequester implements MBRFinder {

	private static Logger logger = Logger.getLogger(MBRRequester.class);

	private MBR mbr;
	
	private Set<String> queryDimensionSet;

	public MBRRequester(SiteDetail siteDetail, Set<String> queryDimensionSet) {
		super(siteDetail);
		this.mbr = null;
		this.queryDimensionSet = queryDimensionSet;
	}

	public MBR getMbr(){
		return mbr; 
	}

	@Override
	protected void getResponse() {
		Socket socket = getSocket();

		try {
			logger.debug("socket is alive :: "+ socket.isConnected());
			Scanner scanner = new Scanner(socket.getInputStream());
			StringBuilder mbrStringBuilder = new StringBuilder();
			int lineCount = 0 ;
			if(scanner.hasNext()){
				lineCount = Integer.parseInt(scanner.nextLine());
			}

			while (lineCount>0 && scanner.hasNext()) {
				lineCount--;
				String responce = scanner.nextLine();
				logger.debug("Received responce from "+ socket.toString() + ", msg :: " + responce);
				mbrStringBuilder.append(responce);
			}

			//parse input mbr json string and get object
			ObjectMapper objectMapper = new ObjectMapper();			
			this.mbr = objectMapper.readValue(mbrStringBuilder.toString(), MBR.class);

			scanner.close();
		} catch (IOException e) {
			logger.debug("Error while getting responce" + e.getLocalizedMessage());
		}

	}

	@Override
	protected RequestType getRequestType() {
		return RequestType.MBR;
	}

	@Override
	protected String getRequestPayLoad() {

		ObjectMapper objectMapper = new ObjectMapper();

		String dimSet = null;
		try {
			dimSet = objectMapper.writeValueAsString(this.queryDimensionSet);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return dimSet;
	}

}
