package cn.com.leador.mapapi.tracker.entity.service.impl;

import java.util.ArrayList;
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
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.ENTITY_RETURN_TYPE;
import cn.com.leador.mapapi.tracker.constants.TrackerEnumConstant.IS_SEARCH;
import cn.com.leador.mapapi.tracker.entity.bean.EntityBean;
import cn.com.leador.mapapi.tracker.entity.bean.EntityColumnBean;
import cn.com.leador.mapapi.tracker.entity.bean.EntityLocationBean;
import cn.com.leador.mapapi.tracker.entity.service.EntityService;
import cn.com.leador.mapapi.tracker.exception.TrackerException;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;

@Service
public class EntityServiceImpl implements EntityService<EntityBean, EntityBean> {

	private Logger logger = LogManager.getLogger(this.getClass());

	@Autowired
	private MongoDBUtilComponent mongoDBUtilComponent;
	@Autowired
	private RedisUtilComponent redisUtilComponent;

	@Value("${entity.max-count}")
	private Integer entityMax;

	@Override
	public ResultBean<EntityBean> addEntity(EntityBean bean)
			throws BusinessException {
		Jedis jedis = null;
		ResultBean<EntityBean> result = new ResultBean<EntityBean>();
		try {
			jedis = redisUtilComponent.getRedisInstance();
			// 锁定SERVICE_ID+entity_name
			redisUtilComponent.lockIdByRedis(
					TrackerConstants.ENTITY_KEY_PREFIX, bean.getService_id()
							+ bean.getEntity_name(),
					TrackerExceptionEnum.REDIS_ENTITY_NAME_DUP, 5, jedis);
			// 判断serviceId是否存在
			if (!mongoDBUtilComponent.checkAppIsExist(bean.getService_id(),
					bean.getAk())) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_APP_ID_NOT_FOUND);
				throw ex;
			}
			// 判断entity_name是否存在
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("service_id", bean.getService_id());
			queryMap.put("entity_name", bean.getEntity_name());
			boolean check = mongoDBUtilComponent.checkObjectIsExist(
					"entity_info", queryMap);
			if (check) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_ENTITY_NAME_DUP);
				throw ex;
			}
			// 判断entity_name数量
			queryMap.clear();
			queryMap.put("service_id", bean.getService_id());
			Integer count = mongoDBUtilComponent.countObject("entity_info",
					queryMap);
			if (count >= entityMax) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_ENTITY_MAX);
				throw ex;
			}

			// 索引字段插入
			queryMap.clear();
			queryMap.put("service_id", bean.getService_id());
			List<EntityColumnBean> list = mongoDBUtilComponent
					.selectObjectMultiProjection("entity_column_info",
							queryMap,
							new String[] { "column_key", "is_search" }, false,
							EntityColumnBean.class, null);
			Map<String, Integer> columnMap = new HashMap<String, Integer>();
			for (EntityColumnBean _bean : list) {
				columnMap.put(_bean.getColumn_key(), _bean.getIs_search());
			}
			List<Map<String, Object>> listIndex = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> listCommon = new ArrayList<Map<String, Object>>();
			// 判定字段是否可用和分段处理字段
			for (String key : bean.getCustom_field().keySet()) {
				if (columnMap.containsKey(key)) {
					Map<String, Object> _map = new HashMap<String, Object>();
					_map.put(key, bean.getCustom_field().get(key));
					if (columnMap.get(key).intValue() == IS_SEARCH.YES
							.getStatus()) {
						listIndex.add(_map);
					} else {
						listCommon.add(_map);
					}
				} else {
					// 找不到情况 百度没处理 暂时不处理
				}
			}
			bean.setCustom_field_index(listIndex);
			bean.setCustom_field_common(listCommon);

			// 插入
			Date date = new Date();
			bean.setCreate_time(date);
			bean.setModify_time(date);
			EntityLocationBean realPoint = new EntityLocationBean();
			realPoint.setLoc_time(date.getTime() / 1000);
			bean.setRealtime_point(realPoint);
			Map<String, Object> _temp = bean.getCustom_field();
			bean.setCustom_field(null);
			JsonBinder binder = JsonBinder.buildNonNullBinder(false);
			mongoDBUtilComponent.insertObject("entity_info",
					binder.toJson(bean));
			redisUtilComponent.releaseIdByRedis(
					TrackerConstants.ENTITY_KEY_PREFIX, bean.getService_id(),
					jedis);
			bean.setCustom_field(_temp);
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

	@Override
	public ResultBean<List<EntityBean>> listEntity(EntityBean bean)
			throws BusinessException {
		try {
			// 判断serviceId是否存在
			if (!mongoDBUtilComponent.checkAppIsExist(bean.getService_id(),
					bean.getAk())) {
				TrackerException ex = new TrackerException(
						TrackerExceptionEnum.REDIS_APP_ID_NOT_FOUND);
				throw ex;
			}
			// 检测自定义索引列表
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("service_id", bean.getService_id());
			List<EntityColumnBean> columnlist = mongoDBUtilComponent
					.selectObjectMultiProjection("entity_column_info",
							queryMap,
							new String[] { "column_key", "is_search" }, false,
							EntityColumnBean.class, null);
			Map<String, Integer> columnMap = new HashMap<String, Integer>();
			for (EntityColumnBean _bean : columnlist) {
				columnMap.put(_bean.getColumn_key(), _bean.getIs_search());
			}
			List<Map<String, Object>> indexColumn = new ArrayList<Map<String, Object>>();
			for (String key : bean.getCustom_field().keySet()) {
				if (!columnMap.containsKey(key)
						|| columnMap.get(key).intValue() != IS_SEARCH.YES
								.getStatus()) {
					// 不存在或者类型不对 则抛出异常
					indexColumn.clear();
					TrackerException ex = new TrackerException(
							TrackerExceptionEnum.REDIS_ENTITY_COLUMN_INDEX_NOT_FOUND);
					throw ex;
				} else {
					// 构建查询数组
					Map<String, Object> _map = new HashMap<String, Object>();
					_map.put(key, bean.getCustom_field().get(key));
					indexColumn.add(_map);
				}
			}
			// 实体名称条件构建
			List<Map<String, Object>> nameList = new ArrayList<Map<String, Object>>();
			if (bean.getEntity_names() != null
					&& bean.getEntity_names().length > 0) {
				for (String name : bean.getEntity_names()) {
					Map<String, Object> _map = new HashMap<String, Object>();
					_map.put("entity_name", name);
					nameList.add(_map);
				}
			}
			// 查询列表
			queryMap.clear();
			queryMap.put("service_id", bean.getService_id());
			// 自定义索引字段
			if (indexColumn.size() > 0) {
				Map<String, Object> _map = new HashMap<String, Object>();
				_map.put("$in", indexColumn);
				queryMap.put("custom_field_index", _map);
			}
			// 时间戳条件
			if (bean.getRealtime_point() != null
					&& bean.getRealtime_point().getLoc_time() > 0) {
				Map<String, Object> _map = new HashMap<String, Object>();
				_map.put("$gte", bean.getRealtime_point()
						.getLoc_time());
				queryMap.put("realtime_point.loc_time", _map);
			}
			// 实体名称条件
			if (nameList.size() > 0) {
				queryMap.put("$or", nameList);
			}
			// 查询总数
			Integer count = mongoDBUtilComponent.countObject("entity_info",
					queryMap);
			// 查询结果
			String[] projection = new String[] { "realtime_point",
					"entity_name", "create_time", "modify_time","custom_field_index","custom_field_common" };
			if (bean.getReturn_type().intValue() == ENTITY_RETURN_TYPE.SIMPLE
					.getStatus()) {
				projection = new String[] { "entity_name" };
			}
			List<EntityBean> list = mongoDBUtilComponent
					.selectObjectMultiProjection("entity_info", queryMap,
							projection, false, EntityBean.class, null,
							bean.getPage_index(), bean.getPage_size());
			ResultBean<List<EntityBean>> result = new ResultBean<List<EntityBean>>();
			result.setSuccessful(true);
			result.setResult(list);
			result.setTotal(count);
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
