package cn.com.leador.mapapi.tracker.track.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import cn.com.leador.mapapi.tracker.bean.TrackerAbstractBean;

public class TrackBean extends TrackerAbstractBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3595068549765485046L;

	private String entity_name;
	private Double longitude;
	private Double latitude;
	private Integer coord_type;
	private Long loc_time;
	private Map<String, Object> custom_field;
	private List<String> beanList;
	private List<TrackBean> error_points;
	private Long time;
	private Integer total;
	private Double speed;
	private Double distance;
	private Integer direction;
	private Boolean is_pumping; //是否抽希点
	private Map<String,Object> _id;
	private Integer sort_type;
	private Integer page_index;
	private Integer page_size;
	private Long start_time;
	private Long end_time;
	private Integer is_processed;
	private Integer simple_return;
	

	public Integer getSimple_return() {
		return simple_return;
	}

	public void setSimple_return(Integer simple_return) {
		this.simple_return = simple_return;
	}

	public Integer getIs_processed() {
		return is_processed;
	}

	public void setIs_processed(Integer is_processed) {
		this.is_processed = is_processed;
	}

	public Long getStart_time() {
		return start_time;
	}

	public void setStart_time(Long start_time) {
		this.start_time = start_time;
	}

	public Long getEnd_time() {
		return end_time;
	}

	public void setEnd_time(Long end_time) {
		this.end_time = end_time;
	}

	public Integer getSort_type() {
		return sort_type;
	}

	public void setSort_type(Integer sort_type) {
		this.sort_type = sort_type;
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

	public Map<String, Object> get_id() {
		return _id;
	}

	public void set_id(Map<String, Object> _id) {
		this._id = _id;
	}

	public Boolean isIs_pumping() {
		return is_pumping;
	}

	public void setIs_pumping(Boolean is_pumping) {
		this.is_pumping = is_pumping;
	}

	public Double getSpeed() {
		return speed;
	}

	public void setSpeed(Double speed) {
		this.speed = speed;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public Integer getDirection() {
		return direction;
	}

	public void setDirection(Integer direction) {
		this.direction = direction;
	}

	public String getEntity_name() {
		return entity_name;
	}

	public void setEntity_name(String entity_name) {
		this.entity_name = entity_name;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Integer getCoord_type() {
		return coord_type;
	}

	public void setCoord_type(Integer coord_type) {
		this.coord_type = coord_type;
	}

	public Long getLoc_time() {
		return loc_time;
	}

	public void setLoc_time(Long loc_time) {
		this.loc_time = loc_time;
	}

	public Map<String, Object> getCustom_field() {
		return custom_field;
	}

	public void setCustom_field(Map<String, Object> custom_field) {
		this.custom_field = custom_field;
	}

	public List<String> getBeanList() {
		return beanList;
	}

	public void setBeanList(List<String> beanList) {
		this.beanList = beanList;
	}

	public List<TrackBean> getError_points() {
		return error_points;
	}

	public void setError_points(List<TrackBean> error_points) {
		this.error_points = error_points;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}
	
	

}
