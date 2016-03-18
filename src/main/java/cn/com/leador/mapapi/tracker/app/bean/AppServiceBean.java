package cn.com.leador.mapapi.tracker.app.bean;

import java.io.Serializable;
import java.util.Date;

import cn.com.leador.mapapi.tracker.util.DateUtil;

/**
 * 轨迹云注册服务相关bean
 * 
 * @author xuyaming
 *
 */
public class AppServiceBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4711637932606517046L;
	private String name;
	private String type;
	private String desc;
	private Long createTime;
	private String createDate;
	private Long updateTime;
	private String updateDate;
	private String id;
	private String ak;

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getCreateDate() {
		return createDate;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public String getAk() {
		return ak;
	}

	public void setAk(String ak) {
		this.ak = ak;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime.getTime();
		this.createDate = DateUtil.parseString(createTime,
				"yyyy-MM-dd HH:mm:ss");
	}

	public Long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime.getTime();
		this.updateDate = DateUtil.parseString(updateTime,
				"yyyy-MM-dd HH:mm:ss");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
