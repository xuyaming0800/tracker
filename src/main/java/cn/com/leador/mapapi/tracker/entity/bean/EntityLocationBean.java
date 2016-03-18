package cn.com.leador.mapapi.tracker.entity.bean;

import java.io.Serializable;

public class EntityLocationBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5458450026486954856L;
	private Long loc_time=0L;
	private Double[] location=new Double[]{0.0D,0.0D};
	private Double speed=0.0D;
	private Double direction=0.0D;
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
	public Double getDirection() {
		return direction;
	}
	public void setDirection(Double direction) {
		this.direction = direction;
	}
	

}
