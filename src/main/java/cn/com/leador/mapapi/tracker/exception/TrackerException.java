package cn.com.leador.mapapi.tracker.exception;

import cn.com.leador.mapapi.common.exception.BusinessException;
import cn.com.leador.mapapi.common.exception.BusinessExceptionEnum;

public class TrackerException extends BusinessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7511176532445320939L;

	public TrackerException(BusinessExceptionEnum enums) {
		super(enums);
	}
	public TrackerException(TrackerExceptionEnum enums) {
		super(enums.getCode(),enums.getMessage());
	}
	public TrackerException(TrackerExceptionEnum enums,String extraMessage){
		super(enums.getCode(),enums.getMessage()+"["+extraMessage+"]");
	}

}
