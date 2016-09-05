package kdom.processor;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import kdom.core.Message;
import kdom.core.RequestType;
import kdom.core.SiteDetail;
import kdom.core.TreeNode;
import kdom.core.Tuple;
import kdom.kdomfinder.KDomFinder;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;


public class KDomRequester extends ClientSideRequester implements KDomFinder {

	private static Logger logger = Logger.getLogger(KDomRequester.class);

	private Set<Tuple> kDomTuples;
	
	private Set<Tuple> fullSkyLines;
	
	private Map<String, Double> filter;
	
	private TreeNode treeNode;

	private int noOfMessages;
	
	private int k;
	
	private boolean delayFlag;
	
	private Set<String> queryDimensionSet;
	
	public KDomRequester(SiteDetail siteDetail, TreeNode treeNode, Map<String, Double> filter, int k,Set<String> queryDimensionSet, boolean delayFlag) {
		super(siteDetail);
		this.treeNode = treeNode;
		this.kDomTuples = new LinkedHashSet<Tuple>();
		this.fullSkyLines = new LinkedHashSet<Tuple>();
		this.filter = filter;
		this.k = k;
		this.queryDimensionSet = queryDimensionSet;
		this.delayFlag = delayFlag;
	}

	@Override
	protected void getResponse() {
		Socket socket = getSocket();
		
		try {
			logger.debug("socket is alive :: "+ socket.isConnected());
			Scanner scanner = new Scanner(socket.getInputStream());
			int lineCount = 0 ;
			
			//get no of messages
			if(scanner.hasNext()){
				this.noOfMessages = Integer.parseInt(scanner.nextLine());
			}
			//get no of kdom + nonKdom tuples
			if(scanner.hasNext()){
				lineCount = Integer.parseInt(scanner.nextLine());
				this.noOfMessages+=(lineCount-1);
			}
			
			//get 
			logger.debug("Received responce from "+ socket.toString() + ", noOfTuples :: " + (lineCount-1));
			int total = lineCount-1;
			boolean fullSky = false;
			while (lineCount>0 && scanner.hasNext()) {
				lineCount--;
				String responce = scanner.nextLine();				
				//logger.info("Received responce from "+ socket.toString() + ", msg :: " + responce);
				if(responce.equals(Message.MID.toString())){
					logger.debug("Received responce from "+ socket.toString() + ", noOfKDomTuples :: " + (total-lineCount));
					logger.debug("Received responce from "+ socket.toString() + ", noOfNonKDOMTuples :: " + lineCount);
					fullSky = true;
				} else {
					Tuple tuple =Tuple.createTuple(responce);
					if(!fullSky){
						this.kDomTuples.add(tuple);
					} else {
						this.fullSkyLines.add(tuple);
					}
				}
			}
			
			scanner.close();
		} catch (IOException e) {
			logger.debug("Error while getting responce" + e.getLocalizedMessage());
		}
	}

	@Override
	protected RequestType getRequestType() {
		return RequestType.KDOM;
	}

	public Set<Tuple> getKdoms() {
		return this.kDomTuples;
	}

	public Set<Tuple> getFullSkylines() {
		return this.fullSkyLines;
	}

	@Override
	protected String getRequestPayLoad() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(this.k + "\n");
		strBuilder.append(this.delayFlag + "\n");
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String dimSet =objectMapper.writeValueAsString(this.queryDimensionSet);
			strBuilder.append(dimSet);
			strBuilder.append("\n");
			
			String filterString =objectMapper.writeValueAsString(filter);
			if(logger.isDebugEnabled()){
				logger.debug("Filter String :: "+ filterString);
			}
			
			strBuilder.append(objectMapper.writeValueAsString(filter));
			strBuilder.append("\n");
			String stringTreeNode = objectMapper.writeValueAsString(treeNode); 
			strBuilder.append(stringTreeNode);
			if(logger.isDebugEnabled()){
				logger.debug("Sending treenode String :: "+ treeNode.toString());
				logger.debug("Sending JSon NOde String :: "+ stringTreeNode);
			}
			
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			logger.error("JSONGenerationException :: " + e.getCause());
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			logger.error("JSONMappingException :: " + e.getCause());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("IOException :: " + e.getCause());
		}
		return strBuilder.toString();
	}

	public int getNoOfMessage() {
		return this.noOfMessages;
	}

	public int setNoOfMessage() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setNoOfMessage(int noOfMessages) {
		//do nothing
	}

}
