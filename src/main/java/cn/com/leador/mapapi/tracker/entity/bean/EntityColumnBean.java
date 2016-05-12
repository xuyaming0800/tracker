package cn.com.leador.mapapi.tracker.entity.bean;

import java.io.Serializable;

import cn.com.leador.mapapi.tracker.bean.TrackerAbstractBean;

public class EntityColumnBean extends TrackerAbstractBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4935480408559211431L;
	private String column_key;
	private String column_desc;
	private Integer is_search;
	private Integer procced_count;


	public String getColumn_key() {
		return column_key;
	}

	public void setColumn_key(String column_key) {
		this.column_key = column_key;
	}

	public String getColumn_desc() {
		return column_desc;
	}

	public void setColumn_desc(String column_desc) {
		this.column_desc = column_desc;
	}

	public Integer getIs_search() {
		return is_search;
	}

	public void setIs_search(Integer is_search) {
		this.is_search = is_search;
	}

	public Integer getProcced_count() {
		return procced_count;
	}

	public void setProcced_count(Integer procced_count) {
		this.procced_count = procced_count;
	}
	
	

}
