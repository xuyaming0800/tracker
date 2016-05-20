package cn.com.leador.mapapi.tracker.fence.service;

import java.util.List;

import cn.com.leador.mapapi.common.bean.ResultBean;
import cn.com.leador.mapapi.common.exception.BusinessException;

public interface FenceAlarmService<T,S> {
	/**
	 * 查询围栏内监控对象历史报警信息
	 * @param bean
	 * @return
	 * @throws BusinessException
	 */
	public ResultBean<List<T>> listFenceAlarmByEntitys(S bean) throws BusinessException;

}
