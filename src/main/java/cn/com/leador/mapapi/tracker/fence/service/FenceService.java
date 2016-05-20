package cn.com.leador.mapapi.tracker.fence.service;

import java.util.List;

import cn.com.leador.mapapi.common.bean.ResultBean;
import cn.com.leador.mapapi.common.exception.BusinessException;

public interface FenceService<T,S> {
	/**
	 * 增加地址围栏
	 * @param bean
	 * @return
	 * @throws BusinessException
	 */
	public ResultBean<T> createFence(S bean) throws BusinessException;
	/**
	 * 删除地址围栏
	 * @param bean
	 * @return
	 * @throws BusinessException
	 */
	public ResultBean<T> deleteFence(S bean) throws BusinessException;
	/**
	 * 更新地址围栏
	 * @param bean
	 * @return
	 * @throws BusinessException
	 */
	public ResultBean<T> updateFence(S bean) throws BusinessException;
	/**
	 * 列出地址围栏
	 * @param bean
	 * @return
	 * @throws BusinessException
	 */
	public ResultBean<List<T>> listFence(S bean) throws BusinessException;
	/**
	 * 查询围栏内监控对象状态
	 * @param bean
	 * @return
	 * @throws BusinessException
	 */
	public ResultBean<T> listFenceStatus(S bean) throws BusinessException;

}
