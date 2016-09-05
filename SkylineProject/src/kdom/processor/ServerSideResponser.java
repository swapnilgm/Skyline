package kdom.processor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import kdom.DAO.core.DataBase;
import kdom.DAO.inmemory.InMemoryDB;
import kdom.core.MBR;
import kdom.core.Message;
import kdom.core.RequestType;
import kdom.core.SiteDetail;
import kdom.core.TreeNode;
import kdom.core.Tuple;
import kdom.kdomfinder.KDomFinder;
import kdom.kdomfinder.OneScanKDomFinder;
import kdom.properties.ServerProperties;
import kdom.util.JacksonCustomModule;

import org.apache.log4j.Logger;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.TypeFactory;

/**
 * 
 */

/**
 * @author Swapnil
 *
 */
public class ServerSideResponser implements Runnable {

	private static Logger logger = Logger.getLogger(ServerSideResponser.class);

	private Socket clientSocket;

	/**
	 * 
	 */
	public ServerSideResponser(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}


	public void run() {

		//RequestType requestType = parseRequest();
		InputStream input;
		RequestType requestType = null;
		try {
			input = this.clientSocket.getInputStream();
			//read input stream
			Scanner scanner = new Scanner(input);
			StringBuilder request = new StringBuilder();
			if(scanner.hasNext()) {
				String partialRequest = scanner.nextLine();				
				if(logger.isDebugEnabled())
					logger.debug("Request received of type string ::"+ partialRequest);
				request.append(partialRequest);
			}
			//scanner.close();
			requestType = RequestType.valueOf(request.toString()); 
			if(requestType.equals(RequestType.MBR)){
				ObjectMapper objectMapper = new ObjectMapper();
				String dimSet = scanner.nextLine();				
				TypeFactory typeFactory = objectMapper.getTypeFactory();
				CollectionType setType = typeFactory.constructCollectionType(LinkedHashSet.class, String.class);
				Set<String> dimensionSet = objectMapper.readValue(dimSet, setType);
				logger.info("Dimenension set received ::"+ dimensionSet);
				processMBR(dimensionSet);
			}
			else if(requestType.equals(RequestType.KDOM)){
				int k = scanner.nextInt();
				scanner.nextLine();
				boolean delayFlag = scanner.nextBoolean();
				scanner.nextLine();
				ObjectMapper objectMapper = new ObjectMapper();

				String dimSet = scanner.nextLine();				
				TypeFactory typeFactory = objectMapper.getTypeFactory();
				CollectionType setType = typeFactory.constructCollectionType(LinkedHashSet.class, String.class);
				Set<String> dimensionSet = objectMapper.readValue(dimSet, setType);
				logger.info("Dimenension set received ::"+ dimensionSet);
				String jsonTreeNode = scanner.nextLine();
				if(logger.isDebugEnabled()){
					logger.debug("Filter recievied :: " + jsonTreeNode);
				}
				objectMapper.registerModule( new JacksonCustomModule("siteDetail", new Version(1, 0, 0, null)) );

				MapType mapType = typeFactory.constructMapType(LinkedHashMap.class, String.class, Double.class);
				Map<String, Double> filter = objectMapper.readValue(jsonTreeNode, mapType);
				jsonTreeNode = scanner.nextLine();
				if(logger.isDebugEnabled()){
					logger.debug("JsonTreeNode recievied :: " + jsonTreeNode);
				}
				TreeNode childDetails = objectMapper.readValue(jsonTreeNode, TreeNode.class);
				processKdom(k, childDetails, filter, dimensionSet, delayFlag);
			}else if(requestType.equals(RequestType.DATA) ){
				processDataRequest();
			}
			//scanner.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

		if (logger.isDebugEnabled()) {
			logger.debug("Requsest processed of type  ::" + requestType);
		}

		try {
			if(!clientSocket.isClosed())
				this.clientSocket.close();
		} catch (IOException e) {
			logger.error("Error while closing responser socket ::\n" + e.getLocalizedMessage());
		}

	}


	/**
	 * Find local mbr and send responce to mbr requester
	 */
	private void processMBR(Set<String> queryDimensionSet){
		MBR mbr = findMBR(queryDimensionSet);
		logger.debug("MBR computed,   MBR::" + mbr);
		try {
			OutputStream outputStream = this.clientSocket.getOutputStream();
			PrintWriter printWriter = new PrintWriter(outputStream, true);
			printWriter.println(1);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.writeValue(outputStream, mbr);

			printWriter.flush();
			//printWriter.close();

			logger.debug("MBR response sent MBR::" + mbr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("IO error while sending responce ::\n"+ e.getMessage());
		}
		return;
	}

	/**
	 * Collect local data and send to requester
	 */
	private void processDataRequest(){
		DataBase db =ServerProperties.getDatabase();
		List<Tuple> data = db.getAllTuples();
		logger.debug("Collected local data, dataSize::" + data.size());
		try {
			OutputStream outputStream = this.clientSocket.getOutputStream();
			PrintWriter printWriter = new PrintWriter(outputStream, true);
			printWriter.println(data.size());
			for (Tuple tuple : data) {
				printWriter.println(tuple.toString());
			}			
			printWriter.flush();
			//printWriter.close();

			logger.debug("Data response sent ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("IO error while sending responce ::\n"+ e.getMessage());
		}
		return;
	}

	/**
	 * Find and collect local mbr
	 * @return
	 */
	private MBR findMBR(Set<String> queryDimensionSet){

		MBRFinder mbrFinder = new LocalMBRFinder(queryDimensionSet);
		mbrFinder.run();
		MBR mbr = mbrFinder.getMbr();
		return mbr;
	}


	/**
	 * Process kdom request
	 * @param k
	 * @param treeNode
	 * @param filter
	 */
	private void processKdom(int k, TreeNode treeNode, Map<String, Double> filter, Set<String> queryDimensionSet, boolean delayFlag){
		Map<SiteDetail, TreeNode> forest = treeNode.getChildren();
		collectKdoms(k, forest, filter, queryDimensionSet, delayFlag);		
		
	}

	/**
	 * Collect kdoms from child site
	 * @param k
	 * @param forest
	 * @param filter
	 * @return
	 */
	private void collectKdoms(int k, Map<SiteDetail, TreeNode> forest, Map<String, Double> filter, Set<String> queryDimensionSet, boolean delayFlag){
		//send the requeset to clients and get kdom and full skylines
		logger.debug("Collecting KDOMs");		
		Map<Thread, KDomFinder> childThreads = new LinkedHashMap<Thread, KDomFinder>();

		
		/*
		 * Start thread to copute local mbr
		 */
		Set<Tuple> probableFullSkys = new LinkedHashSet<Tuple>();
		Set<Tuple> probableKDoms = new LinkedHashSet<Tuple>();

		OneScanKDomFinder localKDomFinder= new OneScanKDomFinder(k,probableFullSkys, ServerProperties.getDatabase(), filter, queryDimensionSet);
		if(delayFlag){
			localKDomFinder.run();
			Set<Tuple> localKdoms = localKDomFinder.getKdoms();
			Set<Tuple> fullSky = new LinkedHashSet<Tuple>(localKdoms);
			fullSky.addAll(probableFullSkys);
			
			//update filter
			double vdr = 1;
			for (String dimension : queryDimensionSet) {
				vdr *= 10000 - filter.get(dimension);
			}
		
		
			Tuple maxVDRTuple = Tuple.getMaxVDRTuple(queryDimensionSet, fullSky);
			if(vdr < maxVDRTuple.getVDR(queryDimensionSet)){
				filter = maxVDRTuple.getData();
			}
				
		}else{
			Thread localKDomThread = new Thread(localKDomFinder);
			localKDomThread.start();
			childThreads.put(localKDomThread, localKDomFinder);
		}
		/*
		 * start thread requesting KDOM from each remote site
		 */
		for (Map.Entry<SiteDetail, TreeNode> treeEntry : forest.entrySet()) {
			SiteDetail siteDetail = treeEntry.getKey();
			TreeNode treeNode = treeEntry.getValue();
			treeNode.setRoot(true);
			KDomFinder remoteKDomFinder = new KDomRequester(siteDetail, treeNode, filter, k, queryDimensionSet, delayFlag);
			Thread clientThread = new Thread(remoteKDomFinder);
			clientThread.start();
			childThreads.put(clientThread, remoteKDomFinder);
		}

		/*
		 * Wait for all thread to complete there execution 
		 * and collect all mbrs
		 */
		int noOfMessages = 0;
		for (Map.Entry<Thread, KDomFinder> threadEntry : childThreads.entrySet()) {
			SiteDetail siteDetail = null;
			try {

				//wait for other threads to complete
				threadEntry.getKey().join();

				KDomFinder kDomFinder = threadEntry.getValue();
				probableKDoms.addAll(kDomFinder.getKdoms());
				probableFullSkys.addAll(kDomFinder.getFullSkylines());
				if(kDomFinder instanceof KDomRequester){
					noOfMessages+= kDomFinder.getNoOfMessage();
					KDomRequester remoteKDomFinder = (KDomRequester)kDomFinder;
					siteDetail = remoteKDomFinder.getSiteDetails();
				}
				else{
					siteDetail = ServerProperties.getLocalSiteDetails();					
				}

				logger.debug("KDOM collected from site :: " +  siteDetail);

			} catch (InterruptedException e) {
				logger.error("Thread interrupted, " + siteDetail + "\n" + e.getMessage());				
			}
		}		

		Set<Tuple> finalKdoms = filterKdoms(probableKDoms, probableFullSkys, k, queryDimensionSet);
		sendKDomResponse(finalKdoms, probableFullSkys, noOfMessages);
		

	}

	/**
	 * Filter rather merge kdoms collected from child sites
	 * @param probableKdoms
	 * @param probableFullSky
	 * @param k
	 * @param filter
	 * @return
	 */
	private Set<Tuple> filterKdoms(Set<Tuple> probableKdoms, Set<Tuple> probableFullSky, int k, Set<String> queryDimensionSet){
		//compare local and remote kdoms
		DataBase db = new InMemoryDB(probableKdoms);
		KDomFinder kDomFinder = new OneScanKDomFinder(k, probableFullSky, db,null,queryDimensionSet);
		kDomFinder.run();
		return kDomFinder.getKdoms();
		//return kDomFinder;
	}

	/**
	 * Send kdoms ans full yline on network connection stream to requester
	 * @param kDoms
	 * @param fullSky
	 */
	private void  sendKDomResponse(Set<Tuple> kDoms, Set<Tuple> fullSky, int noOfMessages){
		logger.debug("Sending KDom computed");
		try {
			OutputStream outputStream = this.clientSocket.getOutputStream();
			PrintWriter printWriter = new PrintWriter(outputStream, true);
			int size = 1 + kDoms.size() + fullSky.size();
			printWriter.println(noOfMessages);
			logger.info("Sending KDoms , size ::" + kDoms.size());
			printWriter.println(size);
			for (Tuple tuple : kDoms) {
				printWriter.println(tuple.toString());
			}
			printWriter.println(Message.MID.toString());
			logger.info("Sending nonKDom full skys , size ::" + fullSky.size());
			for (Tuple tuple : fullSky) {
				printWriter.println(tuple.toString());
			}

			printWriter.flush();
			//printWriter.close();

			logger.debug("KDOM response sent, total size::" + size);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("IO error while sending responce ::\n"+ e.getMessage());
		}
		return;

	}


}
