package cn.com.leador.mapapi.tracker.bean;

import java.util.Date;

import cn.com.leador.mapapi.tracker.util.DateUtil;


public abstract class TrackerAbstractBean {
	private String ak;
	private String service_id;
	private String create_time;
	private Long create_timestamp;
	private String modify_time;
	private Long modify_timestamp;
	
	

	public String getAk() {
		return ak;
	}

	public void setAk(String ak) {
		this.ak = ak;
	}

	public String getService_id() {
		return service_id;
	}

	public void setService_id(String service_id) {
		this.service_id = service_id;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public Long getCreate_timestamp() {
		return create_timestamp;
	}

	public void setCreate_timestamp(Long create_timestamp) {
		this.create_timestamp = create_timestamp;
	}

	public String getModify_time() {
		return modify_time;
	}

	public void setModify_time(String modify_time) {
		this.modify_time = modify_time;
	}

	public Long getModify_timestamp() {
		return modify_timestamp;
	}

	public void setModify_timestamp(Long modify_timestamp) {
		this.modify_timestamp = modify_timestamp;
	}
	
	public void setCreate_time(Date createDate) {
		this.create_time = DateUtil.parseString(createDate,
				"yyyy-MM-dd HH:mm:ss");
		this.create_timestamp=createDate.getTime();
		
	}
	public void setModify_time(Date modifyDate) {
		this.modify_time = DateUtil.parseString(modifyDate,
				"yyyy-MM-dd HH:mm:ss");
		this.modify_timestamp=modifyDate.getTime();
	}

}
