package cn.com.leador.mapapi.tracker.entity.service;

import java.util.List;

import cn.com.leador.mapapi.common.bean.ResultBean;
import cn.com.leador.mapapi.common.exception.BusinessException;

public interface EntityService<T,S> {
	/**
	 * 添加实体
	 * @param bean
	 * @return
	 * @throws BusinessException
	 */
	public ResultBean<T> addEntity(S bean) throws BusinessException;
	/**
	 * 查询实体
	 * @param bean
	 * @return
	 * @throws BusinessException
	 */
	public ResultBean<List<T>> listEntity(S bean)throws BusinessException;
	/**
	 * 删除实体
	 * @param bean
	 * @return
	 * @throws BusinessException
	 */
	public ResultBean<T> deleteEntity(S bean) throws BusinessException;
	/**
	 * 级联删除实体的关联的数据
	 * @param bean
	 * @throws BusinessException
	 */
	public void cascadeDeleteEntity(S bean) throws BusinessException;
	/**
	 * 更新实体
	 * @param bean
	 * @return
	 * @throws BusinessException
	 */
	public ResultBean<T> updateEntity(S bean) throws BusinessException;
	

}
