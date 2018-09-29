package common;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Administrator
 */
public class PagingFilterImpl implements PagingFilter{
	
	private final static Logger logger = LogManager.getLogger(PagingFilterImpl.class);
	

	private Integer limit = 20;
	private Integer start = 0;
	private String dir = DIR_DESC;
	private String sort = null;	
	
	public static PagingFilterImpl fromRequest(HttpServletRequest req){
		
		PagingFilterImpl filter = new PagingFilterImpl();
		try{
			String start_str = req.getParameter("start");
			if(start_str != null){
				filter.start = Integer.parseInt(start_str);
			}
		}catch(Exception ex){
			logger.error(ex.getMessage(), ex);
		}
		
		try{
			String limit_str = req.getParameter("limit");
			if(limit_str != null){
				filter.limit = Integer.parseInt(limit_str);
			}
		}catch(Exception ex){
			logger.error(ex.getMessage(), ex);
		}	
		
		try {
			String dir_str = req.getParameter("dir");
			if (dir_str != null) {
				if (dir_str.equalsIgnoreCase(DIR_DESC)) {
					filter.dir = DIR_DESC;
				} else {
					filter.dir = DIR_ASC;
				}
			}
			
		}catch(Exception ex){
			logger.error(ex.getMessage(), ex);
		}
		return filter;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}
}
