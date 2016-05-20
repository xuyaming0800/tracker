package cn.com.leador.mapapi.tracker.fence.bean;

import java.io.Serializable;

import cn.com.leador.mapapi.tracker.bean.TrackerAbstractBean;

public class FenceEntityBean extends TrackerAbstractBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6030258657344294773L;
	private String entity_name;
	private FencePointBean loc;
	private Long loc_time;
	
	public String getEntity_name() {
		return entity_name;
	}
	public void setEntity_name(String entity_name) {
		this.entity_name = entity_name;
	}
	public FencePointBean getLoc() {
		return loc;
	}
	public void setLoc(FencePointBean loc) {
		this.loc = loc;
	}
	public Long getLoc_time() {
		return loc_time;
	}
	public void setLoc_time(Long loc_time) {
		this.loc_time = loc_time;
	}
	
	

}
