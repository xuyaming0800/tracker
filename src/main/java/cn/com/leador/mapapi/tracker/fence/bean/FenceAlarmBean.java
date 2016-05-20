package cn.com.leador.mapapi.tracker.fence.bean;

import java.io.Serializable;
import java.util.List;

import cn.com.leador.mapapi.tracker.bean.TrackerAbstractBean;

public class FenceAlarmBean extends TrackerAbstractBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5369158458878593606L;

	private String fence_id;
	private String monitored_person;
	private List<String> monitored_persons;
	private Long time;
	private Integer action;
	private Long begin_time;
	private Long end_time;
	private Integer page_size;
	private Integer page_num;
	

	public FenceAlarmBean() {

	}

	public FenceAlarmBean(String ak, String serviceId, String entityName,
			String create_time, Long create_timestamp, Long locTime) {
		this.setAk(ak);
		this.setService_id(serviceId);
		this.setMonitored_person(entityName);
		this.setCreate_time(create_time);
		this.setCreate_timestamp(create_timestamp);
		this.setTime(locTime);
	}

	public String getFence_id() {
		return fence_id;
	}

	public void setFence_id(String fence_id) {
		this.fence_id = fence_id;
	}

	public String getMonitored_person() {
		return monitored_person;
	}

	public void setMonitored_person(String monitored_person) {
		this.monitored_person = monitored_person;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public Integer getAction() {
		return action;
	}

	public void setAction(Integer action) {
		this.action = action;
	}

	public List<String> getMonitored_persons() {
		return monitored_persons;
	}

	public void setMonitored_persons(List<String> monitored_persons) {
		this.monitored_persons = monitored_persons;
	}

	public Long getBegin_time() {
		return begin_time;
	}

	public void setBegin_time(Long begin_time) {
		this.begin_time = begin_time;
	}

	public Long getEnd_time() {
		return end_time;
	}

	public void setEnd_time(Long end_time) {
		this.end_time = end_time;
	}

	public Integer getPage_size() {
		return page_size;
	}

	public void setPage_size(Integer page_size) {
		this.page_size = page_size;
	}

	public Integer getPage_num() {
		return page_num;
	}

	public void setPage_num(Integer page_num) {
		this.page_num = page_num;
	}
	
	
	

}
