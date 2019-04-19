package common.paging;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public interface PagingFilter {

	public final static String DIR_DESC = "desc";
	public final static String DIR_ASC = "asc";

	public void setLimit(Integer limit);
	public Integer getLimit();
	public Integer getStart() ;
	public void setStart(Integer start);
	public String getDir() ;
	public void setDir(String dir);
	public String getSort();
	public void setSort(String sort);
	
	
}
