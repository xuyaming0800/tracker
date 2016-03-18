package cn.com.leador.mapapi.tracker.entity.service;

import java.util.List;

import cn.com.leador.mapapi.common.bean.ResultBean;
import cn.com.leador.mapapi.common.exception.BusinessException;

public interface EntityColumnService<T,S> {
	/**
	 * 增加自定义字段
	 * @param bean
	 * @return
	 */
	public ResultBean<T> addColumn(S bean) throws BusinessException;
	/**
	 * 获取自定义字段列表
	 * @param bean
	 * @return
	 */
	public ResultBean<List<T>> listColumn(S bean)throws BusinessException;

}
