package kdom.processor;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import kdom.core.RequestType;
import kdom.core.SiteDetail;
import kdom.core.Tuple;

import org.apache.log4j.Logger;

public class RemoteDataRequester extends ClientSideRequester implements DataFinder
{
	
	private final static Logger logger = Logger.getLogger(RemoteDataRequester.class);
	
	private List<Tuple> data;
	public RemoteDataRequester(SiteDetail siteDetail)
	{
		super(siteDetail);
		this.data = new ArrayList<Tuple>();
	}


	public List<Tuple> getData()
	{
		return this.data;
	}
	
	@Override
	protected void getResponse()
	{
		Socket socket = getSocket();
		
		try {
			logger.debug("socket is alive :: "+ socket.isConnected() + socket.isInputShutdown() + socket.isOutputShutdown());
			Scanner scanner = new Scanner(socket.getInputStream());
			int lineCount = 0 ;
			if(scanner.hasNext()){
				lineCount = Integer.parseInt(scanner.nextLine());
			}
			
			logger.debug("Receiving data from "+ socket.toString() + ", noOfTuples :: " + lineCount);
			while (lineCount>0 && scanner.hasNext()) {
				lineCount--;
				String responce = scanner.nextLine();
				Tuple tuple = Tuple.createTuple(responce);
				this.data.add(tuple);
			}
			
			scanner.close();
		} catch (IOException e) {
			logger.debug("Error while getting responce" + e.getLocalizedMessage());
		}


	}
	
	@Override
	protected RequestType getRequestType() 
	{
		return RequestType.DATA;
	}

	@Override
	protected String getRequestPayLoad() {
		return null;
	}

}
