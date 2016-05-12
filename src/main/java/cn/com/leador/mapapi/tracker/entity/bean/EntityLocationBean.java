package cn.com.leador.mapapi.tracker.entity.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EntityLocationBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5458450026486954856L;
	private Long loc_time=0L;
	private Double[] location=new Double[]{0.0D,0.0D};
	private Double speed=0.0D;
	private Integer direction=0;
	private Map<String,Object> trackColumns=new HashMap<String,Object>();
	public Long getLoc_time() {
		return loc_time;
	}
	public void setLoc_time(Long loc_time) {
		this.loc_time = loc_time;
	}
	public Double[] getLocation() {
		return location;
	}
	public void setLocation(Double[] location) {
		this.location = location;
	}
	public Double getSpeed() {
		return speed;
	}
	public void setSpeed(Double speed) {
		this.speed = speed;
	}
	public Integer getDirection() {
		return direction;
	}
	public void setDirection(Integer direction) {
		this.direction = direction;
	}
	public Map<String, Object> getTrackColumns() {
		return trackColumns;
	}
	public void setTrackColumns(Map<String, Object> trackColumns) {
		this.trackColumns = trackColumns;
	}
	

}
