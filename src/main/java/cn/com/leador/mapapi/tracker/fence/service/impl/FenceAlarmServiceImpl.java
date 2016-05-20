package cn.com.leador.mapapi.tracker.fence.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.com.leador.mapapi.common.bean.ResultBean;
import cn.com.leador.mapapi.common.exception.BusinessException;
import cn.com.leador.mapapi.common.exception.BusinessExceptionEnum;
import cn.com.leador.mapapi.common.util.json.JsonBinder;
import cn.com.leador.mapapi.tracker.component.MongoDBUtilComponent;
import cn.com.leador.mapapi.tracker.exception.TrackerException;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;
import cn.com.leador.mapapi.tracker.fence.bean.FenceAlarmBean;
import cn.com.leador.mapapi.tracker.fence.service.FenceAlarmService;
@Service
public class FenceAlarmServiceImpl implements FenceAlarmService<FenceAlarmBean, FenceAlarmBean> {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private MongoDBUtilComponent mongoDBUtilComponent;
	
	@SuppressWarnings("serial")
	@Override
	public ResultBean<List<FenceAlarmBean>> listFenceAlarmByEntitys(
			final FenceAlarmBean bean) throws BusinessException {
		ResultBean<List<FenceAlarmBean>> result = new ResultBean<List<FenceAlarmBean>>();
		try {
			// 判断serviceId是否存在
			if (!mongoDBUtilComponent.checkAppIsExist(bean.getService_id(),
					bean.getAk())) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_APP_ID_NOT_FOUND);
				throw ex;
			}
			// 获取查询实体组
			final List<String> entites = bean.getMonitored_persons();
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("monitored_person", new HashMap<String, Object>() {
				{
					put("$in", entites);
				}
			});
			queryMap.put("fence_id", bean.getFence_id());
			queryMap.put("time", new HashMap<String, Object>() {
				{
					put("$gte", bean.getBegin_time());
					put("$lte", bean.getEnd_time());
				}
			});
			Map<String, Object> sortMap = new HashMap<String, Object>();
			System.out.println(JsonBinder.buildNonNullBinder(false).toJson(queryMap));
			sortMap.put("time", -1);
			//查询
			List<FenceAlarmBean> list = mongoDBUtilComponent
					.selectObjectMultiProjection("fence_alarm_info", queryMap,
							null, false,
							FenceAlarmBean.class, null,bean.getPage_num(),bean.getPage_size(),sortMap);
			result.setSuccessful(true);
			result.setResult(list);
			result.setTotal(list.size());
			return result;
		} catch (Exception e) {
			if (e instanceof BusinessException) {
				throw e;
			}
			logger.error(e.getMessage(), e);
			BusinessException ex = new BusinessException(
					BusinessExceptionEnum.SYSTEM_ERROR);
			throw ex;
		}
	}

}
