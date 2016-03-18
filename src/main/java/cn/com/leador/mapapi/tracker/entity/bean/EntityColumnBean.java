package cn.com.leador.mapapi.tracker.entity.bean;

import java.io.Serializable;

public class EntityColumnBean extends EntityAbstractBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4935480408559211431L;
	private String ak;
	private String service_id;
	private String column_key;
	private String column_desc;
	private Integer is_search;

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

}
