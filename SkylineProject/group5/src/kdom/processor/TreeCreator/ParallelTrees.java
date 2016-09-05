package kdom.processor.TreeCreator;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import kdom.core.MBR;
import kdom.core.SiteDetail;
import kdom.core.TreeNode;
import kdom.properties.ServerProperties;

import org.apache.log4j.Logger;
/**
 * @author ritika
 *
 */
public class ParallelTrees implements TreeGenerator 
{
	private final static Logger logger = Logger.getLogger(ParallelTrees.class);

	private int parallelDegree = 5;

	public ParallelTrees(int parallelDegree){
		this.parallelDegree = parallelDegree;
	}

	public Map<SiteDetail, TreeNode> treeFormation(Map<SiteDetail, MBR> MBRCollection) 
	{

		boolean isUnderDominatation=false;
		MBR childMbr=null;
		Map<SiteDetail,Double> dominationArrayList= new LinkedHashMap<SiteDetail, Double>();

		//Initialize forest
		Map<SiteDetail, TreeNode> forest = new LinkedHashMap<SiteDetail,TreeNode>();
		for(SiteDetail siteDetail : MBRCollection.keySet()){
			if(siteDetail.equals(ServerProperties.getLocalSiteDetails()))
				continue;
			forest.put(siteDetail, new TreeNode());
		}

		Map<SiteDetail, MBR> clonedMBRCollection = new LinkedHashMap<SiteDetail, MBR>(MBRCollection);
		Iterator<Map.Entry<SiteDetail, MBR>> childCollectionIterator = MBRCollection.entrySet().iterator(); 
		while (childCollectionIterator.hasNext()) {
			dominationArrayList.clear();
			Map.Entry<SiteDetail,MBR> childEntry = (Map.Entry<SiteDetail,MBR>) childCollectionIterator.next();			
			childMbr=childEntry.getValue();
			SiteDetail childSite = childEntry.getKey();
			if(childSite.equals(ServerProperties.getLocalSiteDetails()))
				continue;
			boolean isPrunable = false;
			for (Entry<SiteDetail, MBR> parentEntry : clonedMBRCollection.entrySet())
			{
				MBR parentMbr = parentEntry.getValue();
				isPrunable = parentMbr.isDominate(childMbr);
				if(isPrunable) {
					childCollectionIterator.remove();
					forest.remove(childSite);
					break;
				}
				isUnderDominatation=childMbr.isUnderDominationOf(parentMbr);
				if(isUnderDominatation)
				{
					logger.debug("Childmbr ::" + childMbr);
					logger.debug("Parentmbr ::" + parentMbr);
					logger.debug("isunderdominatembr ::" + isUnderDominatation);

					dominationArrayList.put(parentEntry.getKey(),getPruningPower(parentMbr, childMbr));
				}

				//for each entry entry1 find the minimum lower 
				//add to arraylist the volume and the sitedails of the node 
				//find the max of volume and then take corresponding sitedetail and set the
				//child pointer of the originator to be true..
				//
			}

			if(isPrunable){
				clonedMBRCollection.remove(childEntry.getKey());
				logger.debug("Pruning child site from forest:: "+ childSite);
				continue;
			}

			SiteDetail parent=findParent(dominationArrayList);
			//if(parent != null)
			int localChildCOunt = 0;
			while(parent != null ){
				//child of Sorg

				if(parent.equals(ServerProperties.getLocalSiteDetails()))
				{	
					if(localChildCOunt == getParallalismDegree()){
						dominationArrayList.remove(parent);
						parent = findParent(dominationArrayList);		
						continue;
					}
					else{
						localChildCOunt++;
						break;	
					}					
					
				}

				//child of non-Sorg
				TreeNode parentNode = forest.get(parent);
				if(parentNode.getChildren().size() == getParallalismDegree()){
					dominationArrayList.remove(parent);
					parent = findParent(dominationArrayList);
					continue;
				}

				logger.debug("removing child site from forest:: "+ childSite);
				TreeNode childNode = forest.get(childSite);
				childNode.setRoot(false);
				parentNode.addChildren(childSite, childNode);
				parent = null;
			} 

		}

		Iterator<Map.Entry<SiteDetail, TreeNode>> forestIterator = forest.entrySet().iterator();
		while (forestIterator.hasNext()) {
			Map.Entry<SiteDetail,TreeNode> entry = (Map.Entry<SiteDetail,TreeNode>) forestIterator.next();
			if(!(entry.getValue().isRoot())){
				forestIterator.remove();
			}
		}

		return forest;
	}

	protected int getParallalismDegree(){
		return this.parallelDegree;
	}

	protected double getPruningPower(MBR parentMbr, MBR childMbr){
		return parentMbr.dominationArea(childMbr);
	}

	private SiteDetail findParent(Map<SiteDetail, Double> dominationList) 
	{
		Iterator<Map.Entry<SiteDetail,Double>> listIterator=dominationList.entrySet().iterator();
		double max=Double.NEGATIVE_INFINITY,temp=0;
		SiteDetail parent=null;
		while (listIterator.hasNext()) 
		{
			Map.Entry<SiteDetail, Double> parentEntry = listIterator.next();
			temp=parentEntry.getValue();
			if (max<temp)
			{
				max=temp;	
				parent = parentEntry.getKey();
			}

		}

		return parent;

	}

}
