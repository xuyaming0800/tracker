package cn.com.leador.mapapi.tracker.entity.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import cn.com.leador.mapapi.tracker.bean.TrackerAbstractBean;

public class EntityBean extends TrackerAbstractBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9092364581087576728L;
	private String entity_name;
	private Map<String, Object> custom_field;
	private List<Map<String, Object>> custom_field_index;
	private List<Map<String, Object>> custom_field_common;
	private EntityLocationBean realtime_point;
	private Integer page_index;
	private Integer page_size;
	private String[] entity_names;
	private Integer return_type;
	private Integer total;
	
	

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getReturn_type() {
		return return_type;
	}

	public void setReturn_type(Integer return_type) {
		this.return_type = return_type;
	}

	public String[] getEntity_names() {
		return entity_names;
	}

	public void setEntity_names(String[] entity_names) {
		this.entity_names = entity_names;
	}

	public Integer getPage_index() {
		return page_index;
	}

	public void setPage_index(Integer page_index) {
		this.page_index = page_index;
	}

	public Integer getPage_size() {
		return page_size;
	}

	public void setPage_size(Integer page_size) {
		this.page_size = page_size;
	}


	public String getEntity_name() {
		return entity_name;
	}

	public void setEntity_name(String entity_name) {
		this.entity_name = entity_name;
	}

	public Map<String, Object> getCustom_field() {
		return custom_field;
	}

	public void setCustom_field(Map<String, Object> custom_field) {
		this.custom_field = custom_field;
	}

	public List<Map<String, Object>> getCustom_field_index() {
		return custom_field_index;
	}

	public void setCustom_field_index(
			List<Map<String, Object>> custom_field_index) {
		this.custom_field_index = custom_field_index;
	}

	public List<Map<String, Object>> getCustom_field_common() {
		return custom_field_common;
	}

	public void setCustom_field_common(
			List<Map<String, Object>> custom_field_common) {
		this.custom_field_common = custom_field_common;
	}

	public EntityLocationBean getRealtime_point() {
		return realtime_point;
	}

	public void setRealtime_point(EntityLocationBean realtime_point) {
		this.realtime_point = realtime_point;
	}
	
	

}
