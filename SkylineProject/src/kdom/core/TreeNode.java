package kdom.core;


import java.util.LinkedHashMap;
/**
 * @author ritika
 *
 */
public class TreeNode
{

	private boolean isRoot;
	
	private LinkedHashMap<SiteDetail, TreeNode> children;
	
	public TreeNode(){
		this.isRoot = true;
		this.children = new LinkedHashMap<SiteDetail, TreeNode>();
		//this.siteDetail = siteDetail;
	}

	/**
	 * @return the isRoot
	 */
	public boolean isRoot() {
		return isRoot;
	}

	/**
	 * @param isRoot the isRoot to set
	 */
	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	/**
	 * @return the children
	 */
	public LinkedHashMap<SiteDetail, TreeNode> getChildren() {
		return children;
	}

	
	/**
	 * @param children the children to set
	 */
	protected void setChildren(LinkedHashMap<SiteDetail, TreeNode> children) {
		this.children = children;
	}

	/**
	 * @param childNode the children to set
	 */
	public void addChildren(SiteDetail siteDetail, TreeNode childNode) {
		this.children.put(siteDetail,childNode);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TreeNode [isRoot=" + isRoot + ", children=" + children + "]";
	}
	
	
}
