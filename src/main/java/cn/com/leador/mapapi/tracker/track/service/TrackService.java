package cn.com.leador.mapapi.tracker.track.service;

import java.util.List;

import cn.com.leador.mapapi.common.bean.ResultBean;
import cn.com.leador.mapapi.common.exception.BusinessException;

public interface TrackService<T,S> {
	/**
	 * 添加轨迹点
	 * @param bean
	 * @return
	 * @throws BusinessException
	 */
	public ResultBean<T> addPoint(S bean) throws BusinessException;
	/**
	 * 批量添加轨迹点
	 * @param bean
	 * @return
	 * @throws BusinessException
	 */
	public ResultBean<T> addPoints(S bean) throws BusinessException;
	/**
	 * 查询轨迹点历史
	 * @param bean
	 * @return
	 * @throws BusinessException
	 */
	public ResultBean<List<T>> getHistory(S bean )throws BusinessException;

}
