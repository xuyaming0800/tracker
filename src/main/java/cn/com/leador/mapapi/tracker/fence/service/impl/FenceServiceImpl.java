package cn.com.leador.mapapi.tracker.fence.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import cn.com.leador.mapapi.common.bean.ResultBean;
import cn.com.leador.mapapi.common.exception.BusinessException;
import cn.com.leador.mapapi.common.exception.BusinessExceptionEnum;
import cn.com.leador.mapapi.common.util.json.JsonBinder;
import cn.com.leador.mapapi.tracker.component.MongoDBUtilComponent;
import cn.com.leador.mapapi.tracker.component.RedisUtilComponent;
import cn.com.leador.mapapi.tracker.constants.TrackerConstants;
import cn.com.leador.mapapi.tracker.exception.TrackerException;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;
import cn.com.leador.mapapi.tracker.fence.bean.FenceBean;
import cn.com.leador.mapapi.tracker.fence.service.FenceService;

@Service
public class FenceServiceImpl implements FenceService<FenceBean, FenceBean> {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private MongoDBUtilComponent mongoDBUtilComponent;
	@Autowired
	private RedisUtilComponent redisUtilComponent;
	

	@Value("${fence.max_count.entity}")
	private Integer maxCount;

	@Value("${fence.max_count.per.entity}")
	private Integer perMaxCount;

	@SuppressWarnings("serial")
	@Override
	public ResultBean<FenceBean> createFence(FenceBean bean)
			throws BusinessException {
		Jedis jedis = null;
		ResultBean<FenceBean> result = new ResultBean<FenceBean>();
		try {
			jedis = redisUtilComponent.getRedisInstance();
			JsonBinder binder = JsonBinder.buildNonNullBinder(false);
			List<String> entites = bean.getMonitored_persons();
			// 检测entites是否超过了限制
			if (entites.size() > maxCount) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.FENCE_ENTITY_COUNT_IS_MAX);
				throw ex;
			}
			// 锁定SERVICE_ID_ENTITY
			for (String entityName : entites) {
				redisUtilComponent
						.lockIdByRedis(TrackerConstants.TRACK_FENCE_KEY_PREFIX,
								bean.getService_id() + "_" + entityName,
								TrackerExceptionEnum.FENCE_ENTITY_IS_ADD_NOW,
								15, jedis);
			}
			// 判断serviceId是否存在
			if (!mongoDBUtilComponent.checkAppIsExist(bean.getService_id(),
					bean.getAk())) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_APP_ID_NOT_FOUND);
				throw ex;
			}
			// 检测已经存在的数量
			for (final String entityName : entites) {
				String value = redisUtilComponent
						.getRedisStringCache(TrackerConstants.TRACK_FENCE_ENTITY_KEY_PREFIX
								+ bean.getService_id() + "_" + entityName);
				Integer count = null;
				if (value == null) {
					Map<String, Object> queryMap = new HashMap<String, Object>();
					queryMap.put("service_id", bean.getService_id());
					queryMap.put("monitored_persons",
							new HashMap<String, Object>() {
								{
									put("$all", new String[] { entityName });
								}
							});
					count = mongoDBUtilComponent.countObject("fence_info",
							queryMap);
					value = String.valueOf(count);
					// 放入缓存
					redisUtilComponent.setRedisStringCache(
							TrackerConstants.TRACK_FENCE_ENTITY_KEY_PREFIX
									+ bean.getService_id() + "_" + entityName,
							value, 0);
				} else {
					count = Integer.valueOf(value);
				}
				if (count >= perMaxCount) {
					TrackerException ex = new TrackerException(
							TrackerExceptionEnum.FENCE_PER_ENTITY_COUNT_IS_MAX);
					throw ex;
				}
			}
			Map<String, Object> queryMap = new HashMap<String, Object>();
			// 获取最大FENCE_ID
			Long maxId = redisUtilComponent.increament(
					TrackerConstants.MAX_FENCE_ID_KEY, jedis);
			bean.setFence_id(maxId.toString());
			// 更新最大值
			queryMap.clear();
			Map<String, Long> updateMap = new HashMap<String, Long>();
			queryMap.put("status", 0);
			updateMap.put("maxId", maxId);
			mongoDBUtilComponent.upsertCommonObject("fence_max_id", queryMap,
					null, updateMap);
			// 插入
			Date date = new Date();
			bean.setCreate_time(date);
			bean.setModify_time(date);
			mongoDBUtilComponent
					.insertObject("fence_info", binder.toJson(bean));
			for (final String entityName : entites) {
				String value = redisUtilComponent
						.getRedisStringCache(TrackerConstants.TRACK_FENCE_ENTITY_KEY_PREFIX
								+ bean.getService_id() + "_" + entityName);
				Integer count = null;
				if (value == null) {
					queryMap.clear();
					queryMap.put("service_id", bean.getService_id());
					queryMap.put("monitored_persons",
							new HashMap<String, Object>() {
								{
									put("$all", new String[] { entityName });
								}
							});
					count = mongoDBUtilComponent.countObject("fence_info",
							queryMap);
				} else {
					count = Integer.valueOf(value)+1;
				}
				// 放入缓存
				redisUtilComponent.setRedisStringCache(
						TrackerConstants.TRACK_FENCE_ENTITY_KEY_PREFIX
								+ bean.getService_id() + "_" + entityName,
						String.valueOf(count), 0);
			}
			for (String entityName : entites) {
				redisUtilComponent.releaseIdByRedis(
						TrackerConstants.TRACK_FENCE_KEY_PREFIX,
						bean.getService_id() + "_" + entityName, jedis);
			}
			result.setSuccessful(true);
			result.setResult(bean);
			return result;

		} catch (Exception e) {
			if (e instanceof BusinessException) {
				throw e;
			}
			logger.error(e.getMessage(), e);
			BusinessException ex = new BusinessException(
					BusinessExceptionEnum.SYSTEM_ERROR);
			throw ex;
		} finally {
			redisUtilComponent.returnRedis(jedis);
		}
	}

	@SuppressWarnings("serial")
	@Override
	public ResultBean<FenceBean> deleteFence(FenceBean bean)
			throws BusinessException {
		Jedis jedis = null;
		ResultBean<FenceBean> result = new ResultBean<FenceBean>();
		try {
			jedis = redisUtilComponent.getRedisInstance();
			// JsonBinder binder = JsonBinder.buildNonNullBinder(false);
			// 判断serviceId是否存在
			if (!mongoDBUtilComponent.checkAppIsExist(bean.getService_id(),
					bean.getAk())) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_APP_ID_NOT_FOUND);
				throw ex;
			}
			// 获取监控对象
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("fence_id", bean.getFence_id());
			List<FenceBean> list = mongoDBUtilComponent
					.selectObjectMultiProjection("fence_info", queryMap,
							new String[] { "monitored_persons" }, false,
							FenceBean.class, null);
			if (list == null || list.size() == 0) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.FENCE_IS_NOT_FOUND);
				throw ex;
			}
			FenceBean _bean = list.get(0);
			// 锁定Serviceid_name
			for (String entityName : _bean.getMonitored_persons()) {
				redisUtilComponent
						.lockIdByRedis(TrackerConstants.TRACK_FENCE_KEY_PREFIX,
								bean.getService_id() + "_" + entityName,
								TrackerExceptionEnum.FENCE_ENTITY_IS_ADD_NOW,
								15, jedis);
			}
			// 删除
			mongoDBUtilComponent.removeCommonObject("fence_info", queryMap);
			// 更新计数缓存
			for (final String entityName : _bean.getMonitored_persons()) {
				queryMap.clear();
				queryMap.put("service_id", bean.getService_id());
				queryMap.put("monitored_persons",
						new HashMap<String, Object>() {
							{
								put("$all", new String[] { entityName });
							}
						});
				Integer count = mongoDBUtilComponent.countObject("fence_info",
						queryMap);
				String value = String.valueOf(count);
				// 放入缓存
				redisUtilComponent.setRedisStringCache(
						TrackerConstants.TRACK_FENCE_ENTITY_KEY_PREFIX
								+ bean.getService_id() + "_" + entityName,
						value, 0);
			}
			for (String entityName : _bean.getMonitored_persons()) {
				redisUtilComponent.releaseIdByRedis(
						TrackerConstants.TRACK_FENCE_KEY_PREFIX,
						bean.getService_id() + "_" + entityName, jedis);
			}
			result.setSuccessful(true);
			result.setResult(bean);
			return result;

		} catch (Exception e) {
			if (e instanceof BusinessException) {
				throw e;
			}
			logger.error(e.getMessage(), e);
			BusinessException ex = new BusinessException(
					BusinessExceptionEnum.SYSTEM_ERROR);
			throw ex;
		} finally {
			redisUtilComponent.returnRedis(jedis);
		}
	}

	@SuppressWarnings("serial")
	@Override
	public ResultBean<FenceBean> updateFence(FenceBean bean)
			throws BusinessException {
		Jedis jedis = null;
		ResultBean<FenceBean> result = new ResultBean<FenceBean>();
		try {
			jedis = redisUtilComponent.getRedisInstance();
			JsonBinder binder = JsonBinder.buildNonNullBinder(false);
			// 判断serviceId是否存在
			if (!mongoDBUtilComponent.checkAppIsExist(bean.getService_id(),
					bean.getAk())) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_APP_ID_NOT_FOUND);
				throw ex;
			}
			// 获取监控对象
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("fence_id", bean.getFence_id());
			List<FenceBean> list = mongoDBUtilComponent
					.selectObjectMultiProjection("fence_info", queryMap,
							new String[] { "monitored_persons" }, false,
							FenceBean.class, null);
			if (list == null || list.size() == 0) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.FENCE_IS_NOT_FOUND);
				throw ex;
			}
			FenceBean _bean = list.get(0);
			if(bean.getMonitored_persons()!=null){
				// 锁定Serviceid_name
				for (String entityName : _bean.getMonitored_persons()) {
					redisUtilComponent
							.lockIdByRedis(TrackerConstants.TRACK_FENCE_KEY_PREFIX,
									bean.getService_id() + "_" + entityName,
									TrackerExceptionEnum.FENCE_ENTITY_IS_ADD_NOW,
									15, jedis);
				}
				for (String entityName : bean.getMonitored_persons()) {
					if(!_bean.getMonitored_persons().contains(entityName)){
						redisUtilComponent
						.lockIdByRedis(TrackerConstants.TRACK_FENCE_KEY_PREFIX,
								bean.getService_id() + "_" + entityName,
								TrackerExceptionEnum.FENCE_ENTITY_IS_ADD_NOW,
								15, jedis);
					}
					
				}
				// 检测
				for (final String entityName : bean.getMonitored_persons()) {
					String value = redisUtilComponent
							.getRedisStringCache(TrackerConstants.TRACK_FENCE_ENTITY_KEY_PREFIX
									+ bean.getService_id() + "_" + entityName);
					Integer count = null;
					if (value == null) {
						queryMap.clear();
						queryMap.put("service_id", bean.getService_id());
						queryMap.put("monitored_persons",
								new HashMap<String, Object>() {
									{
										put("$all", new String[] { entityName });
									}
								});
						count = mongoDBUtilComponent.countObject("fence_info",
								queryMap);
						value = String.valueOf(count);
						// 放入缓存
						redisUtilComponent.setRedisStringCache(
								TrackerConstants.TRACK_FENCE_ENTITY_KEY_PREFIX
										+ bean.getService_id() + "_" + entityName,
								value, 0);
					} else {
						count = Integer.valueOf(value);
					}
					if(count==perMaxCount)count=count-1;
					if (count >= perMaxCount) {
						TrackerException ex = new TrackerException(
								TrackerExceptionEnum.FENCE_PER_ENTITY_COUNT_IS_MAX);
						throw ex;
					}
				}
			}
			
			
			
			
			// 更新
			queryMap.clear();
			queryMap.put("fence_id", bean.getFence_id());
			Map<String, Object> updateMap = new HashMap<String, Object>();
			updateMap.put("$set", binder.fromJson(binder.toJson(bean), Map.class));
			mongoDBUtilComponent.executeUpdate("fence_info", queryMap,
					updateMap);
			// 更新计数缓存
			for (final String entityName : _bean.getMonitored_persons()) {
				queryMap.clear();
				queryMap.put("service_id", bean.getService_id());
				queryMap.put("monitored_persons",
						new HashMap<String, Object>() {
							{
								put("$all", new String[] { entityName });
							}
						});
				Integer count = mongoDBUtilComponent.countObject("fence_info",
						queryMap);
				String value = String.valueOf(count);
				// 放入缓存
				redisUtilComponent.setRedisStringCache(
						TrackerConstants.TRACK_FENCE_ENTITY_KEY_PREFIX
								+ bean.getService_id() + "_" + entityName,
						value, 0);
			}
			if(bean.getMonitored_persons()!=null){
				for (final String entityName : bean.getMonitored_persons()) {
					queryMap.clear();
					queryMap.put("service_id", bean.getService_id());
					queryMap.put("monitored_persons",
							new HashMap<String, Object>() {
								{
									put("$all", new String[] { entityName });
								}
							});
					Integer count = mongoDBUtilComponent.countObject("fence_info",
							queryMap);
					String value = String.valueOf(count);
					// 放入缓存
					redisUtilComponent.setRedisStringCache(
							TrackerConstants.TRACK_FENCE_ENTITY_KEY_PREFIX
									+ bean.getService_id() + "_" + entityName,
							value, 0);
				}
				for (String entityName : _bean.getMonitored_persons()) {
					redisUtilComponent.releaseIdByRedis(
							TrackerConstants.TRACK_FENCE_KEY_PREFIX,
							bean.getService_id() + "_" + entityName, jedis);
				}
				for (String entityName : bean.getMonitored_persons()) {
					redisUtilComponent.releaseIdByRedis(
							TrackerConstants.TRACK_FENCE_KEY_PREFIX,
							bean.getService_id() + "_" + entityName, jedis);
				}
			}
			
			result.setSuccessful(true);
			result.setResult(bean);
			return result;

		} catch (Exception e) {
			if (e instanceof BusinessException) {
				throw e;
			}
			logger.error(e.getMessage(), e);
			BusinessException ex = new BusinessException(
					BusinessExceptionEnum.SYSTEM_ERROR);
			throw ex;
		} finally {
			redisUtilComponent.returnRedis(jedis);
		}
	}

	@SuppressWarnings("serial")
	@Override
	public ResultBean<List<FenceBean>> listFence(FenceBean bean)
			throws BusinessException {
		ResultBean<List<FenceBean>> result = new ResultBean<List<FenceBean>>();
		try {
			// 判断serviceId是否存在
			if (!mongoDBUtilComponent.checkAppIsExist(bean.getService_id(),
					bean.getAk())) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_APP_ID_NOT_FOUND);
				throw ex;
			}
			// 获取ID组
			final List<String> ids = bean.getFence_ids();
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("fence_id", new HashMap<String, Object>() {
				{
					put("$in", ids);
				}
			});
			//查询
			List<FenceBean> list = mongoDBUtilComponent
					.selectObjectMultiProjection("fence_info", queryMap,
							null, false,
							FenceBean.class, null);
			result.setSuccessful(true);
			result.setResult(list);
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

	@SuppressWarnings("serial")
	@Override
	public ResultBean<FenceBean> listFenceStatus(FenceBean bean)
			throws BusinessException {
		ResultBean<FenceBean> result = new ResultBean<FenceBean>();
		try {
			// 判断serviceId是否存在
			if (!mongoDBUtilComponent.checkAppIsExist(bean.getService_id(),
					bean.getAk())) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_APP_ID_NOT_FOUND);
				throw ex;
			}
			// 获取Entity组
			final List<String> entites = bean.getMonitored_persons();
			Map<String, Object> queryMap = new HashMap<String, Object>();
			if(entites!=null&&entites.size()>0){
				queryMap.put("monitored_persons", new HashMap<String, Object>() {
					{
						put("$all", entites);
					}
				});
			}
			
			queryMap.put("fence_id", bean.getFence_id());
			//查询
			List<FenceBean> list = mongoDBUtilComponent
					.selectObjectMultiProjection("fence_info", queryMap,
							new String[]{"monitored_type"}, false,
							FenceBean.class, null);
			result.setSuccessful(true);
			if(list.size()>0){
				FenceBean fenceBean=list.get(0);
				if(entites!=null&&entites.size()>0){
					Map<String,Integer> type=new HashMap<String,Integer>();
					for(String name:entites){
						type.put(name, fenceBean.getMonitored_type().get(name));
					}
					fenceBean.setMonitored_type(type);
				}
				result.setResult(fenceBean);
			}else{
				result.setResult(null);
			}
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
