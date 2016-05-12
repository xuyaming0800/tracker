package cn.com.leador.mapapi.tracker.track.bean;

import java.io.Serializable;

import cn.com.leador.mapapi.tracker.bean.TrackerAbstractBean;

public class TrackColumnBean extends TrackerAbstractBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4935480408559211431L;
	
	private String column_key;
	private String column_desc;
	private Integer column_type;
	private Integer procced_count;
	
	

	public Integer getProcced_count() {
		return procced_count;
	}

	public void setProcced_count(Integer procced_count) {
		this.procced_count = procced_count;
	}

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

	public Integer getColumn_type() {
		return column_type;
	}

	public void setColumn_type(Integer column_type) {
		this.column_type = column_type;
	}

	

}
