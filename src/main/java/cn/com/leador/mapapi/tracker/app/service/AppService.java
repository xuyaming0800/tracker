package cn.com.leador.mapapi.tracker.app.service;

import cn.com.leador.mapapi.common.bean.ResultBean;
import cn.com.leador.mapapi.common.exception.BusinessException;
import cn.com.leador.mapapi.tracker.app.bean.AppServiceBean;

public interface AppService {
	/**
	 * 增加鹰眼服务
	 * @param bean
	 * @param ak
	 * @return
	 * @throws BusinessException
	 */
	public ResultBean<AppServiceBean> addAppService(AppServiceBean bean)throws BusinessException;

}
