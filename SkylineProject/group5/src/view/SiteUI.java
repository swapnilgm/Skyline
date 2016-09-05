package view;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import kdom.DAO.file.FileDB;
import kdom.core.SiteDetail;
import kdom.processor.ListenerServer;
import kdom.processor.QueryProcessor;
import kdom.properties.ClusterProerties;
import kdom.properties.ServerProperties;

import org.apache.log4j.Logger;


public class SiteUI {

	private static Logger logger = Logger.getLogger(SiteUI.class);

	private static Thread serverThread;

	public static void main(String[] args){

		try{
			Scanner scanner = new Scanner(System.in);

			//read ip
			String serverIp = null;
			if(args.length>=1)
				serverIp = args[0];
			else {
				System.out.println("Enter the host ip to use :: ");
				serverIp = scanner.nextLine();
			}

			//read port
			Integer serverPort = null;
			try{
				if(args.length>=2)
					serverPort = Integer.parseInt(args[1]);
			}catch(NumberFormatException e){
				logger.debug("Invalid input argument for port");
				serverPort = null; 
			}

			if(serverPort == null){
				System.out.println("Enter the host port number :: ");
				serverPort = Integer.parseInt(scanner.nextLine());
			}



			//SiteDetail localSiteDetails = new SiteDetail(InetAddress.getLocalHost().getHostAddress(), serverPort);
			SiteDetail localSiteDetails = new SiteDetail(serverIp, serverPort);
			ServerProperties.setLocalSiteDetails(localSiteDetails);

			//read cluster file
			File clusterDetilsFile = null;
			if(args.length>=3){
				clusterDetilsFile = new File(args[2]);
				loadClusterDetails(clusterDetilsFile);
			}
			while(clusterDetilsFile == null ){
				System.out.println("Enter the cluster details file path :: ");
				String clusterDetialsPath = scanner.nextLine();
				try {
					clusterDetilsFile = new File(clusterDetialsPath);
					loadClusterDetails(clusterDetilsFile);
				} catch (FileNotFoundException e) {
					System.err.println("Invalid cluster detalis file path. Try again");
					clusterDetilsFile = null;
				}
			};
			ClusterProerties.getSiteDetail().remove(localSiteDetails);
			//read db file
			File dbFile = null;
			if(args.length>=4){
				dbFile = new File(args[3]);
				ServerProperties.setDatabase(new FileDB(dbFile));
			}

			while(dbFile == null || (!dbFile.isFile() && !dbFile.exists())) {
				System.out.println("Enter the DB file path :: ");
				String dbFilePath = scanner.nextLine();
				dbFile = new File(dbFilePath);
				try{
					ServerProperties.setDatabase(new FileDB(dbFile));
				} catch (FileNotFoundException e) {
					System.err.println("Invalid database file path. Try again");
					dbFile = null;
				}
			}

			createListner(serverPort);

			System.out.println("Press Y on all server setup ");
			String isServerSetupDone = scanner.nextLine();
			if(isServerSetupDone.equalsIgnoreCase("Y")){
				String query = null;
				int k = 0;
				//TODO add validation k < noOfDimension
				while( true ){
					System.out.println("Enter query file path :: ");					
					query = scanner.nextLine();
					Set<String> queryDimensionSet = new HashSet<String>();
					boolean quit = loadQueryFile(new File(query), queryDimensionSet);
					if(quit) {
						break;
					}
				}
			}
			scanner.close();
		}		
		catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("Exiting from system ...:)");
	}

	private static boolean  loadQueryFile(File queryFile, Set<String> queryDimensionSet) throws FileNotFoundException{

		logger.info("\n********** Reading Query **********\n");
		InputStream queryFileStream = null;

		int k = -1;

		queryFileStream = new FileInputStream(queryFile);
		Scanner scanner = null;
		scanner = new Scanner(queryFileStream);
		if(scanner.hasNextLine()){
			k = scanner.nextInt();
			scanner.nextLine();
			logger.info("K :: "+k);
		}
		if(scanner.hasNextLine()){
			String fileLine = scanner.nextLine();

			StringTokenizer stringTokenizer = new StringTokenizer(fileLine);
			while (stringTokenizer.hasMoreTokens()) {

				String dimension = stringTokenizer.nextToken();
				queryDimensionSet.add(dimension);			

			}
		}
		int parallismDegree = 0;
		if(scanner.hasNextLine()){
			parallismDegree = scanner.nextInt();
			logger.info("Parallism Degree :: "+ parallismDegree);
		}
		logger.info("Query File loaded , query dimension set::"+queryDimensionSet);

		scanner.close();


		QueryProcessor queryProcessor = new QueryProcessor(k,queryDimensionSet,parallismDegree);
		queryProcessor.processQuery();
		if(k<1 || parallismDegree < 1 ){
			return true;
		}
		return false;
	}

	/**
	 * reads cluster details file and load cluster properties in bean
	 * @param scanner
	 * @throws FileNotFoundException 
	 */
	private static void loadClusterDetails(File clusterDetailsFile) throws FileNotFoundException{
		Scanner clusterDetailsScanner = null;		
		clusterDetailsScanner = new Scanner(clusterDetailsFile);
		Set<SiteDetail> siteDetails = new HashSet<SiteDetail>();
		while (clusterDetailsScanner.hasNext()) {
			String siteDetailString  = clusterDetailsScanner.nextLine();
			StringTokenizer stringTokenizer = new StringTokenizer(siteDetailString);
			String ipAddress = stringTokenizer.nextToken();
			int sitePort = Integer.parseInt(stringTokenizer.nextToken());
			SiteDetail siteDetail = new SiteDetail(ipAddress, sitePort);
			siteDetails.add(siteDetail);
		}
		clusterDetailsScanner.close();
		ClusterProerties.setSiteDetail(siteDetails);	
	}

	/**
	 * Start the server at current site 
	 * @param port
	 * @param dbFile
	 * @throws IOException
	 */
	public static void createListner(int port) throws IOException{
		ListenerServer listenerServer = new ListenerServer(port);
		serverThread = new Thread(listenerServer);
		serverThread.start();
	}

}