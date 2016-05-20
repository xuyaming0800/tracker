package cn.com.leador.mapapi.tracker.fence.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import cn.com.leador.mapapi.tracker.bean.TrackerAbstractBean;

public class FenceBean extends TrackerAbstractBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -675265336508595874L;
	private String fence_id;
	private List<String> fence_ids;
	private String name;
	private String desc;
	private String creator;
	private List<String> monitored_persons;
	private Map<String,Integer> monitored_type;
	private List<ValidTimeBean> valid_times;
	private Integer valid_cycle;
	private String valid_date;
	private List<Integer> valid_days;
	private Integer coord_type;
	private Integer shape;
	private List<List<Double[]>> coords;
	private List<Object> centerSphere;
	private Map<String,Double> center;
	private Integer radius;
	private String coordStr;
	
	
	private Integer alarm_condition;
	
	public Map<String, Integer> getMonitored_type() {
		return monitored_type;
	}
	public void setMonitored_type(Map<String, Integer> monitored_type) {
		this.monitored_type = monitored_type;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public List<String> getMonitored_persons() {
		return monitored_persons;
	}
	public void setMonitored_persons(List<String> monitored_persons) {
		this.monitored_persons = monitored_persons;
	}
	public List<ValidTimeBean> getValid_times() {
		return valid_times;
	}
	public void setValid_times(List<ValidTimeBean> valid_times) {
		this.valid_times = valid_times;
	}
	public Integer getValid_cycle() {
		return valid_cycle;
	}
	public void setValid_cycle(Integer valid_cycle) {
		this.valid_cycle = valid_cycle;
	}
	public String getValid_date() {
		return valid_date;
	}
	public void setValid_date(String valid_date) {
		this.valid_date = valid_date;
	}
	public List<Integer> getValid_days() {
		return valid_days;
	}
	public void setValid_days(List<Integer> valid_days) {
		this.valid_days = valid_days;
	}
	public Integer getCoord_type() {
		return coord_type;
	}
	public void setCoord_type(Integer coord_type) {
		this.coord_type = coord_type;
	}
	public List<List<Double[]>> getCoords() {
		return coords;
	}
	public void setCoords(List<List<Double[]>> coords) {
		this.coords = coords;
	}
	public Integer getAlarm_condition() {
		return alarm_condition;
	}
	public void setAlarm_condition(Integer alarm_condition) {
		this.alarm_condition = alarm_condition;
	}
	public String getFence_id() {
		return fence_id;
	}
	public void setFence_id(String fence_id) {
		this.fence_id = fence_id;
	}
	public List<String> getFence_ids() {
		return fence_ids;
	}
	public void setFence_ids(List<String> fence_ids) {
		this.fence_ids = fence_ids;
	}
	public Integer getShape() {
		return shape;
	}
	public void setShape(Integer shape) {
		this.shape = shape;
	}
	public List<Object> getCenterSphere() {
		return centerSphere;
	}
	public void setCenterSphere(List<Object> centerSphere) {
		this.centerSphere = centerSphere;
	}
	public Map<String, Double> getCenter() {
		return center;
	}
	public void setCenter(Map<String, Double> center) {
		this.center = center;
	}
	public Integer getRadius() {
		return radius;
	}
	public void setRadius(Integer radius) {
		this.radius = radius;
	}
	public String getCoordStr() {
		return coordStr;
	}
	public void setCoordStr(String coordStr) {
		this.coordStr = coordStr;
	}
	
	
	
	

}
