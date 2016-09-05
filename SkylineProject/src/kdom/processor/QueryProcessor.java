package kdom.processor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kdom.DAO.core.DataBase;
import kdom.DAO.inmemory.InMemoryDB;
import kdom.core.MBR;
import kdom.core.SiteDetail;
import kdom.core.TreeNode;
import kdom.core.Tuple;
import kdom.kdomfinder.KDomFinder;
import kdom.kdomfinder.OneScanKDomFinder;
import kdom.processor.TreeCreator.Forest;
import kdom.processor.TreeCreator.ParallelTrees;
import kdom.processor.TreeCreator.TreeGenerator;
import kdom.properties.ClusterProerties;
import kdom.properties.ServerProperties;

import org.apache.log4j.Logger;


public class QueryProcessor {

	private static Logger logger = Logger.getLogger(QueryProcessor.class);

	/**
	 * Collection of all site where further data is residing
	 */
	private static ArrayList<SiteDetail> siteDetail;

	private int k;
	
	private Set<String> queryDimensionSet;

	private int parallaismDegree;
	
	public QueryProcessor(int k,Set<String> queryDimensionSet, int parallelismDegree) {
		this.k = k;
		this.queryDimensionSet = queryDimensionSet;
		this.parallaismDegree = parallelismDegree;
	}

	/**
	 * Process query on this site
	 */
	public void processQuery(){

		logger.debug("Processing query, k = "+ this.k);
		Map<SiteDetail, TreeNode> forest = null;
		logger.info("Executing proposed algo :: ");
		long startTime = System.currentTimeMillis();
		long stopTime;
		long startTimeForTreeBuilding = System.currentTimeMillis();
		Map<SiteDetail, MBR> mbrList = collectMBR();
		forest = createBoundedTree(mbrList);
		long stopTimeForTreeBuilding = System.currentTimeMillis();
		findAllKdoms(forest,false);
		stopTime = System.currentTimeMillis();
		logger.info("Forest build took time :: " + (stopTimeForTreeBuilding - startTimeForTreeBuilding)+"ms");
		logger.info("Proposed algo finished in time :: " + (stopTime - startTime)+"ms");
		
		logger.info("Executing modified proposed algo :: ");
		startTime = System.currentTimeMillis();
		startTimeForTreeBuilding = System.currentTimeMillis();
		mbrList = collectMBR();
		forest = createBoundedTree(mbrList);
		stopTimeForTreeBuilding = System.currentTimeMillis();
		findAllKdoms(forest,true);
		stopTime = System.currentTimeMillis();
		logger.info("Forest build took time :: " + (stopTimeForTreeBuilding - startTimeForTreeBuilding)+"ms");
		logger.info("Modified Proposed algo finished in time :: " + (stopTime - startTime)+"ms");
		
		logger.info("Executing Enclosed area proposed algo :: ");
		startTime = System.currentTimeMillis();
		startTimeForTreeBuilding = System.currentTimeMillis();
		mbrList = collectMBR();
		forest = createTree(mbrList);
		stopTimeForTreeBuilding = System.currentTimeMillis();
		findAllKdoms(forest,true);
		stopTime = System.currentTimeMillis();
		logger.info("Forest build took time :: " + (stopTimeForTreeBuilding - startTimeForTreeBuilding)+"ms");
		logger.info("Modified Enclosed algo finished in time :: " + (stopTime - startTime)+"ms");
		
/*		
		logger.info("Executing linear chian algo :: ");
		startTime = System.currentTimeMillis();
		Map<SiteDetail, TreeNode> linearChain = createLinearChain();
		findAllKdoms(linearChain,true);
		stopTime = System.currentTimeMillis();
		logger.info("Executed linear chian algo in time:: "+ (stopTime - startTime)+"ms");
*/
/*		logger.info("Executing naive algo :: ");
		startTime = System.currentTimeMillis();
		Set<Tuple> data  = collectData();
		DataBase db = new InMemoryDB(data);
		KDomFinder kdomFinder = new OneScanKDomFinder(k, db, null, this.queryDimensionSet);
		kdomFinder.run();
		Set<Tuple> kdoms = kdomFinder.getKdoms();
		logger.info("KDoms computed :: "  + kdoms.size());
		stopTime = System.currentTimeMillis();
		logger.info("Executed naive algo in time:: "+ (stopTime - startTime)+"ms");
*/
		logger.info("Executing modified naive algo :: ");
		startTime = System.currentTimeMillis();
		forest = createForestForOneHopAlgo();
		findAllKdoms(forest, true);
		stopTime = System.currentTimeMillis();
		logger.info("Executed modified naive algo in time:: "+ (stopTime - startTime)+"ms");
	
	}
	
	Map<SiteDetail, TreeNode> createForestForOneHopAlgo(){
		Map<SiteDetail, TreeNode> forest = new LinkedHashMap<SiteDetail, TreeNode>();
		for (SiteDetail siteDetail : ClusterProerties.getSiteDetail()) {
			if(siteDetail.equals(ServerProperties.getLocalSiteDetails()))
				continue;
			forest.put(siteDetail, new TreeNode());
		}
		return forest;
	}

	/**
	 * Create linear chain
	 * @return
	 */
	private Map<SiteDetail, TreeNode> createLinearChain(){
		Set<SiteDetail> remoteSiteDetails = ClusterProerties.getSiteDetail();
		SiteDetail localSiteDetail = ServerProperties.getLocalSiteDetails();
		TreeNode head = new TreeNode();
		TreeNode previous = head;
		for (SiteDetail siteDetail : remoteSiteDetails) {
			if(siteDetail == localSiteDetail ){
				continue;
			}
			TreeNode childNode= new TreeNode();
			childNode.setRoot(false);
			previous.addChildren(siteDetail, childNode);
			previous = childNode;
		}
		Map<SiteDetail , TreeNode> linearChain = new LinkedHashMap<SiteDetail, TreeNode>();
		linearChain.put(localSiteDetail, head);
		return linearChain;
	}

	/**
	 * Collect mbr from all sites
	 * @return
	 */
	private Map<SiteDetail, MBR> collectMBR(){
		logger.debug("Collecting MBR");
		Set<SiteDetail> remoteSiteDetails = ClusterProerties.getSiteDetail();
		Map<SiteDetail, MBR> mbrCollection = new LinkedHashMap<SiteDetail, MBR>();		
		Map<Thread, MBRFinder> childThreads = new LinkedHashMap<Thread, MBRFinder>();

		/*
		 * Start thread to copute local mbr
		 */
		LocalMBRFinder localMbrFinder= new LocalMBRFinder(queryDimensionSet);
		Thread localMBRThread = new Thread(localMbrFinder);
		localMBRThread.start();
		childThreads.put(localMBRThread, localMbrFinder);

		/*
		 * start thread requesting MBR from each remote site
		 */
		for (SiteDetail siteDetail : remoteSiteDetails) {
			MBRFinder remoteMBRFinder = new MBRRequester(siteDetail,queryDimensionSet);
			Thread clientThread = new Thread(remoteMBRFinder);
			clientThread.start();
			childThreads.put(clientThread, remoteMBRFinder);
		}

		/*
		 * Wait for all thread to complete there execution 
		 * and collect all mbrs
		 */
		for (Map.Entry<Thread, MBRFinder> threadEntry : childThreads.entrySet()) {
			try {

				//wait for other threads to complete
				threadEntry.getKey().join();

				MBRFinder mbrFinder = threadEntry.getValue();
				MBR mbr = mbrFinder.getMbr();
				SiteDetail siteDetail = null;

				if(mbrFinder instanceof MBRRequester){
					MBRRequester remoteMbrFinder = (MBRRequester)mbrFinder;
					siteDetail = remoteMbrFinder.getSiteDetails();
				}
				else{
					siteDetail = ServerProperties.getLocalSiteDetails();					
				}

				mbrCollection.put(siteDetail, mbr);
				logger.debug("MBR collected from site :: " +  siteDetail + ", MBR :: "+ mbr);

			} catch (InterruptedException e) {
				logger.error("Thread interrupted, " + siteDetail + "\n" + e.getMessage());				
			}
		}		

		return mbrCollection;
	}

	/**
	 * Create tree to process query remotely 
	 * @param mbrList
	 * @return
	 */
	private Map<SiteDetail, TreeNode> createBoundedTree(Map<SiteDetail, MBR> mbrList){
		TreeGenerator treeGenerator = new ParallelTrees(this.parallaismDegree);
		Map<SiteDetail, TreeNode> forest = treeGenerator.treeFormation(mbrList);
		return forest;
	}

	/**
	 * Create tree to process query remotely 
	 * @param mbrList
	 * @return
	 */
	private Map<SiteDetail, TreeNode> createTree(Map<SiteDetail, MBR> mbrList){
		TreeGenerator treeGenerator = new Forest(Integer.MAX_VALUE);
		Map<SiteDetail, TreeNode> forest = treeGenerator.treeFormation(mbrList);
		return forest;
	}
	
	/**
	 * Find all kdomss
	 * @param forest
	 */
	private  void findAllKdoms(Map<SiteDetail, TreeNode> forest, boolean delayFlag){
		Set<Tuple> probableFullSky = new LinkedHashSet<Tuple>();
		Set<Tuple> probableKDoms = collectAllKdoms(forest, delayFlag, probableFullSky);		
		Set<Tuple> finalKDom = filterKdoms(probableKDoms, probableFullSky);
		logger.info("Final KDoms :: " + finalKDom.size());
	}
	

	/**
	 * Collect kdoms from sites in subtrees
	 * @param forest
	 * @return
	 */
	private Set<Tuple> collectAllKdoms(Map<SiteDetail, TreeNode> forest, boolean delayFlag , Set<Tuple> probableFullSkys){
		//send the requeset to clients and get kdom and full skylines
		if (logger.isDebugEnabled()) {
			logger.debug("Collecting KDOMs");		
			logger.debug("Forest :: "+ forest);
			logger.debug("Forest size:: "+ forest.size());
		}
		Map<Thread, KDomFinder> childThreads = new LinkedHashMap<Thread, KDomFinder>();


		/*
		 * Start thread to copute local mbr
		 */
		
		Set<Tuple> probableKDoms = new LinkedHashSet<Tuple>();
		Map<String, Double> filter = null;
		OneScanKDomFinder localKDomFinder= new OneScanKDomFinder(this.k,probableFullSkys, ServerProperties.getDatabase(), filter, queryDimensionSet);
		if(delayFlag){
			localKDomFinder.run();
			Set<Tuple> localKdoms = localKDomFinder.getKdoms();
			Set<Tuple> fullSky = new LinkedHashSet<Tuple>(localKdoms);
			fullSky.addAll(probableFullSkys);
			
			Tuple maxVDRTuple = Tuple.getMaxVDRTuple(queryDimensionSet, fullSky);
				filter = maxVDRTuple.getData();
				
		}else{

			Thread localKDomThread = new Thread(localKDomFinder);
			localKDomThread.start();
			childThreads.put(localKDomThread, localKDomFinder);
			MBRFinder mbrFinder = new LocalMBRFinder(queryDimensionSet);
			mbrFinder.run();
			filter = mbrFinder.getMbr().getUpperBound();

		}

		/*
		 * start thread requesting KDOM from each remote site
		 */

		for (Map.Entry<SiteDetail, TreeNode> treeEntry : forest.entrySet()) {
			SiteDetail siteDetail = treeEntry.getKey();
			TreeNode treeNode = treeEntry.getValue();
			KDomFinder remoteKDomFinder = new KDomRequester(siteDetail, treeNode, filter, this.k, this.queryDimensionSet, false);
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
				noOfMessages+=kDomFinder.getNoOfMessage();
				if(kDomFinder instanceof KDomRequester) {
				KDomRequester remoteMbrFinder = (KDomRequester)kDomFinder;
				siteDetail = remoteMbrFinder.getSiteDetails();
				} else {
					siteDetail = ServerProperties.getLocalSiteDetails();
				}
				logger.debug("KDOM collected from site :: " +  siteDetail);

			} catch (InterruptedException e) {
				logger.error("Thread interrupted, " + siteDetail + "\n" + e.getMessage());				
			}
		}		
		logger.debug("Total probable kdoms :: "+ probableKDoms.size());
		logger.info("Total no. of messages transfered :: "+ noOfMessages);
		Set<Tuple> finalKdoms = filterKdoms(probableKDoms, probableFullSkys);
		return finalKdoms;

	}
	
		
	/**
	 * Merge koms collected from remote site
	 * @param probableKdoms
	 * @param probableFullSky
	 * @return
	 */
	private Set<Tuple> filterKdoms(Set<Tuple> probableKdoms, Set<Tuple> probableFullSky){
		//compare local and remote kdoms
		DataBase db = new InMemoryDB(probableKdoms);
		KDomFinder kDomFinder = new OneScanKDomFinder(k, probableFullSky, db, null,this.queryDimensionSet);
		kDomFinder.run();
		Set<Tuple> finalKDoms = kDomFinder.getKdoms();

		return finalKDoms;
	}

	/**
	 * Collect all fractioned data from all sites
	 * @return
	 */
	private Set<Tuple> collectData()
	{
		logger.debug("Collecting Data");
		Set<SiteDetail> remoteSiteDetails = ClusterProerties.getSiteDetail();
		LinkedHashSet<Tuple> dataCollection =new LinkedHashSet<Tuple>();
		Map<Thread, DataFinder> childThreads = new LinkedHashMap<Thread, DataFinder>();

		/*
		 * Start thread to collecting local data
		 */
		LocalDataRequester localDataFinder= new LocalDataRequester();
		Thread localMBRThread = new Thread(localDataFinder);
		localMBRThread.start();
		childThreads.put(localMBRThread, localDataFinder);

		/*
		 * start thread requesting MBR from each remote site
		 */
		for (SiteDetail siteDetail : remoteSiteDetails) {
			RemoteDataRequester remoteDataRequester = new RemoteDataRequester(siteDetail);
			Thread clientThread = new Thread(remoteDataRequester);
			clientThread.start();
			childThreads.put(clientThread, remoteDataRequester);
		}

		/*
		 * Wait for all thread to complete there execution 
		 * and collect all data
		 */
		int noOfMessages = 0;
		for (Map.Entry<Thread, DataFinder> threadEntry : childThreads.entrySet()) {
			try {

				//wait for other threads to complete
				threadEntry.getKey().join();

				DataFinder dataRequester = threadEntry.getValue();
				List<Tuple> data = dataRequester.getData();
				SiteDetail siteDetail = null;

				if(dataRequester instanceof RemoteDataRequester){
					noOfMessages+= data.size();
					RemoteDataRequester remotedataFinder = (RemoteDataRequester)dataRequester;
					siteDetail = remotedataFinder.getSiteDetails();
				}
				else{
					siteDetail = ServerProperties.getLocalSiteDetails();					
				}


				dataCollection.addAll(data);
				logger.info("Data collected from site :: " +  siteDetail + ", data size :: "+ data.size());

			} catch (InterruptedException e) {
				logger.error("Thread interrupted, " + siteDetail + "\n" + e.getMessage());				
			}
		}		
		logger.info("Total no. of messages transfered :: "+ noOfMessages);
		return dataCollection;
	}

}
