package cn.com.leador.mapapi.tracker.exception;

import cn.com.leador.mapapi.common.exception.BusinessExceptionBean;

public class TrackerExceptionBean extends BusinessExceptionBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6611985300766921567L;

	public TrackerExceptionBean(TrackerExceptionEnum enums){
		super(enums.getCode(),enums.getMessage());
	}

}
