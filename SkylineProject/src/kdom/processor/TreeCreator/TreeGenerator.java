package kdom.processor.TreeCreator;

import java.util.Map;

import kdom.core.MBR;
import kdom.core.SiteDetail;
import kdom.core.TreeNode;
/**
 * @author ritika
 *
 */
public interface TreeGenerator
{
	public Map<SiteDetail, TreeNode> treeFormation(Map<SiteDetail,MBR> allMBR);
}
