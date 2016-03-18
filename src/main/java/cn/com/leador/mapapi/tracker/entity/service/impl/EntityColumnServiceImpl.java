package cn.com.leador.mapapi.tracker.entity.service.impl;

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
import cn.com.leador.mapapi.tracker.entity.bean.EntityColumnBean;
import cn.com.leador.mapapi.tracker.entity.service.EntityColumnService;
import cn.com.leador.mapapi.tracker.exception.TrackerException;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;

@Service
public class EntityColumnServiceImpl implements
		EntityColumnService<EntityColumnBean, EntityColumnBean> {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private MongoDBUtilComponent mongoDBUtilComponent;
	@Autowired
	private RedisUtilComponent redisUtilComponent;

	@Value("${entity.column-max-count}")
	private Integer columnMax;

	@Value("${entity.column-max-index-count}")
	private Integer columnIndexMax;

	@Override
	public ResultBean<EntityColumnBean> addColumn(EntityColumnBean bean)
			throws BusinessException {
		Jedis jedis = null;
		boolean isAddIndex = false;
		ResultBean<EntityColumnBean> result = new ResultBean<EntityColumnBean>();
		try {
			jedis = redisUtilComponent.getRedisInstance();
			// 锁定SERVICE_ID
			redisUtilComponent.lockIdByRedis(
					TrackerConstants.ENTITY_COLUMN_KEY_PREFIX,
					bean.getService_id(),
					TrackerExceptionEnum.REDIS_ENTITY_COLUMN_DUP, 5, jedis);
			// 判断serviceId是否存在
			if (!mongoDBUtilComponent.checkAppIsExist(bean.getService_id(),
					bean.getAk())) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_APP_ID_NOT_FOUND);
				throw ex;
			}
			// 判断名称是否存在
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("service_id", bean.getService_id());
			queryMap.put("column_key", bean.getColumn_key());
			boolean check = mongoDBUtilComponent.checkObjectIsExist(
					"entity_column_info", queryMap);
			if (check) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_ENTITY_COLUMN_DUP);
				throw ex;
			}
			// 判断自定义字段数量
			queryMap.clear();
			queryMap.put("service_id", bean.getService_id());
			Integer count = mongoDBUtilComponent.countObject(
					"entity_column_info", queryMap);
			if (count >= columnMax) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_ENTITY_COLUMN_MAX);
				throw ex;
			}
			// 判断索引字段数量
			queryMap.clear();
			queryMap.put("service_id", bean.getService_id());
			queryMap.put("is_search", 1);
			count = mongoDBUtilComponent.countObject("entity_column_info",
					queryMap);
			if (count >= columnIndexMax) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_ENTITY_COLUMN_INDEX_MAX);
				throw ex;
			}
			// 插入
			Date date = new Date();
			bean.setCreate_time(date);
			bean.setModify_time(date);
			JsonBinder binder = JsonBinder.buildNonNullBinder(false);
			mongoDBUtilComponent.insertObject("entity_column_info",
					binder.toJson(bean));
			redisUtilComponent.releaseIdByRedis(
					TrackerConstants.ENTITY_COLUMN_KEY_PREFIX,
					bean.getService_id(), jedis);
			result.setSuccessful(true);
			result.setResult(bean);
			return result;

		} catch (Exception e) {
			if (isAddIndex) {
				Map<String, Object> queryMap = new HashMap<String, Object>();
				queryMap.put(bean.getColumn_key(), 1);
				mongoDBUtilComponent.dropIndex("entity_info", queryMap);
			}
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

	@Override
	public ResultBean<List<EntityColumnBean>> listColumn(EntityColumnBean bean)
			throws BusinessException {
		try {
			// 判断serviceId是否存在
			if (!mongoDBUtilComponent.checkAppIsExist(bean.getService_id(),
					bean.getAk())) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_APP_ID_NOT_FOUND);
				throw ex;
			}
			// 查询列表
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("service_id", bean.getService_id());
			queryMap.put("ak", bean.getAk());
			List<EntityColumnBean> list=mongoDBUtilComponent.selectObjectMultiProjection(
					"entity_column_info", queryMap, new String[] {
							"service_id", "column_key", "column_desc",
							"column_key", "create_time", "modify_time",
							"is_search" }, false,EntityColumnBean.class,null);
			ResultBean<List<EntityColumnBean>> result = new ResultBean<List<EntityColumnBean>>();
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

}
