package cn.com.leador.mapapi.tracker.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import cn.com.leador.mapapi.common.exception.BusinessException;
import cn.com.leador.mapapi.common.util.json.JsonBinder;
import cn.com.leador.mapapi.tracker.exception.TrackerException;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;

import com.fasterxml.jackson.databind.JavaType;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * MongoDB封装方法
 * 
 * @author xuyaming
 *
 */
@Component
public class MongoDBUtilComponent {
	private Logger logger = LogManager.getLogger(this.getClass());
	@Autowired
	private MongoTemplate mongoTemplate;

	private enum JSON_STYLE {
		MAP(0), ARRAY(1), STRING(2);
		private int code;

		public int getCode() {
			return code;
		}

		private JSON_STYLE(int code) {
			this.code = code;
		}
	}

	@SuppressWarnings("unchecked")
	public void insertObjects(String tableName, List<String> jsons)
			throws BusinessException {
		if (jsons != null) {
			try {
				DBCollection dbcoll = mongoTemplate.getDb().getCollection(
						tableName);
				List<BasicDBObject> inserList=new ArrayList<BasicDBObject>();
				for (String json : jsons) {
					Integer style = this.verifyJsonStyle(json);
					JsonBinder binder = JsonBinder.buildNormalBinder(false);
					Map<String, Object> insertMap = null;
					if (style.equals(JSON_STYLE.MAP.getCode())) {
						insertMap = binder.fromJson(json, Map.class, binder
								.getCollectionType(Map.class, String.class,
										Object.class));
					} else if (style.equals(JSON_STYLE.ARRAY.getCode())) {
						logger.error("插入时候的JSON串必须是JSON格式");
						throw new TrackerException(
								TrackerExceptionEnum.MONGONDB_INSERT_ERROR);
					}
					
					BasicDBObject insert = new BasicDBObject();
					if (insertMap != null)
						insert.putAll(insertMap);
					inserList.add(insert);
					// 记录日志 供万一写数据失败 追回使用
					logger.info("[INSERT] " + json);
				}
				dbcoll.insert(inserList);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new TrackerException(
						TrackerExceptionEnum.MONGONDB_INSERT_ERROR);

			}

		}

	}

	/**
	 * 插入mongodb
	 * 
	 * @param tableName
	 * @param insertMap
	 */
	@SuppressWarnings("unchecked")
	public void insertObject(String tableName, String json)
			throws BusinessException {
		try {
			Integer style = this.verifyJsonStyle(json);
			JsonBinder binder = JsonBinder.buildNormalBinder(false);
			Map<String, Object> insertMap = null;
			if (style.equals(JSON_STYLE.MAP.getCode())) {
				insertMap = binder.fromJson(json, Map.class, binder
						.getCollectionType(Map.class, String.class,
								Object.class));
			} else if (style.equals(JSON_STYLE.ARRAY.getCode())) {
				logger.error("插入时候的JSON串必须是JSON格式");
				throw new TrackerException(
						TrackerExceptionEnum.MONGONDB_INSERT_ERROR);
			}
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject insert = new BasicDBObject();
			if (insertMap != null)
				insert.putAll(insertMap);
			dbcoll.insert(insert);
			// 记录日志 供万一写数据失败 追回使用
			logger.info("[INSERT] " + json);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_INSERT_ERROR);

		}
	}

	/**
	 * 插入mongodb
	 * 
	 * @param tableName
	 * @param insertMap
	 * @throws BusinessException
	 */
	public void insertObject(String tableName, Map<String, Object> insertMap)
			throws BusinessException {
		try {
			JsonBinder binder = JsonBinder.buildNormalBinder(false);
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject insert = new BasicDBObject();
			if (insertMap != null)
				insert.putAll(insertMap);
			dbcoll.insert(insert);
			// 记录日志 供万一写数据失败 追回使用
			logger.info("[INSERT] " + binder.toJson(insertMap));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_INSERT_ERROR);

		}
	}

	/**
	 * 查询mongodb
	 * 
	 * @param tableName
	 * @param selectQueryMap
	 * @param selectlocates
	 * @param isReturnId
	 * @return
	 * @throws BusinessException
	 */
	public List<Object> selectObjectMultiProjection(String tableName,
			Map<String, Object> selectQueryMap, String[] selectlocates,
			boolean isReturnId) throws BusinessException {
		try {
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject query = new BasicDBObject();
			query.putAll(selectQueryMap);
			DBCursor dBCursor = null;
			List<Object> result = new ArrayList<Object>();
			if (selectlocates != null && selectlocates.length > 0) {
				BasicDBObject projection = new BasicDBObject();
				for (String selectlocate : selectlocates) {
					projection.put(selectlocate, 1);
				}
				if (!isReturnId) {
					projection.put("_id", 0);
				}
				dBCursor = dbcoll.find(query, projection);
			} else {
				if (!isReturnId) {
					BasicDBObject projection = new BasicDBObject();
					projection.put("_id", 0);
					dBCursor = dbcoll.find(query,projection);
				}else{
					dBCursor = dbcoll.find(query);
				}
				
			}
			for (DBObject _obj : dBCursor) {
				if (_obj.toMap().size() > 0)
					result.add(_obj);
			}
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_QUERY_ERROR);

		}
	}

	/**
	 * 泛型的分页查询
	 * 
	 * @param tableName
	 * @param selectQueryMap
	 * @param selectlocates
	 * @param isReturnId
	 * @param clazz
	 * @param javaType
	 * @param pageIndex
	 *            from 1
	 * @param pageSize
	 * @return
	 * @throws BusinessException
	 */
	public <T> List<T> selectObjectMultiProjection(String tableName,
			Map<String, Object> selectQueryMap, String[] selectlocates,
			boolean isReturnId, Class<T> clazz, JavaType javaType,
			Integer pageIndex, Integer pageSize) throws BusinessException {
		try {
			JsonBinder binder = JsonBinder.buildNonNullBinder(false);
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject query = new BasicDBObject();
			query.putAll(selectQueryMap);
			DBCursor dBCursor = null;
			List<T> result = new ArrayList<T>();
			if (selectlocates != null && selectlocates.length > 0) {
				BasicDBObject projection = new BasicDBObject();
				for (String selectlocate : selectlocates) {
					projection.put(selectlocate, 1);
				}
				if (!isReturnId) {
					projection.put("_id", 0);
				}
				dBCursor = dbcoll.find(query, projection)
						.skip((pageIndex - 1) * pageSize).limit(pageSize);
			} else {
				if (!isReturnId) {
					BasicDBObject projection = new BasicDBObject();
					projection.put("_id", 0);
					dBCursor = dbcoll.find(query,projection).skip((pageIndex - 1) * pageSize)
							.limit(pageSize);
				}else{
					dBCursor = dbcoll.find(query).skip((pageIndex - 1) * pageSize)
							.limit(pageSize);
				}
			}
			for (DBObject _obj : dBCursor) {
				if (_obj.toMap().size() > 0) {
					T t = null;
					if (javaType != null) {
						t = binder.fromJson(_obj.toString(), clazz, javaType);
					} else {
						t = binder.fromJson(_obj.toString(), clazz);
					}
					result.add(t);
				}

			}
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_QUERY_ERROR);

		}
	}
	
	public <T> List<T> selectObjectMultiProjection(String tableName,
			Map<String, Object> selectQueryMap, String[] selectlocates,
			boolean isReturnId, Class<T> clazz, JavaType javaType,
			Integer pageIndex, Integer pageSize,Map<String,Object> sort) throws BusinessException {
		try {
			JsonBinder binder = JsonBinder.buildNonNullBinder(false);
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject query = new BasicDBObject();
			BasicDBObject sortMap = new BasicDBObject();
			sortMap.putAll(sort);
			query.putAll(selectQueryMap);
			DBCursor dBCursor = null;
			List<T> result = new ArrayList<T>();
			if (selectlocates != null && selectlocates.length > 0) {
				BasicDBObject projection = new BasicDBObject();
				for (String selectlocate : selectlocates) {
					projection.put(selectlocate, 1);
				}
				if (!isReturnId) {
					projection.put("_id", 0);
				}
				dBCursor = dbcoll.find(query, projection).sort(sortMap)
						.skip((pageIndex - 1) * pageSize).limit(pageSize);
			} else {
				if (!isReturnId) {
					BasicDBObject projection = new BasicDBObject();
					projection.put("_id", 0);
					dBCursor = dbcoll.find(query,projection).sort(sortMap).skip((pageIndex - 1) * pageSize)
							.limit(pageSize);
				}else{
					dBCursor = dbcoll.find(query).sort(sortMap).skip((pageIndex - 1) * pageSize)
							.limit(pageSize);
				}
				
			}
			for (DBObject _obj : dBCursor) {
				if (_obj.toMap().size() > 0) {
					T t = null;
					if (javaType != null) {
						t = binder.fromJson(_obj.toString(), clazz, javaType);
					} else {
						t = binder.fromJson(_obj.toString(), clazz);
					}
					result.add(t);
				}

			}
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_QUERY_ERROR);

		}
	}

	/**
	 * 泛型的查询方法
	 * 
	 * @param tableName
	 * @param selectQueryMap
	 * @param selectlocates
	 * @param isReturnId
	 * @param clazz
	 * @param javaType
	 * @return
	 * @throws BusinessException
	 */
	public <T> List<T> selectObjectMultiProjection(String tableName,
			Map<String, Object> selectQueryMap, String[] selectlocates,
			boolean isReturnId, Class<T> clazz, JavaType javaType)
			throws BusinessException {
		try {
			JsonBinder binder = JsonBinder.buildNonNullBinder(false);
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject query = new BasicDBObject();
			query.putAll(selectQueryMap);
			DBCursor dBCursor = null;
			List<T> result = new ArrayList<T>();
			if (selectlocates != null && selectlocates.length > 0) {
				BasicDBObject projection = new BasicDBObject();
				for (String selectlocate : selectlocates) {
					projection.put(selectlocate, 1);
				}
				if (!isReturnId) {
					projection.put("_id", 0);
				}
				dBCursor = dbcoll.find(query, projection);
			} else {
				if (!isReturnId) {
					BasicDBObject projection = new BasicDBObject();
					projection.put("_id", 0);
					dBCursor = dbcoll.find(query,projection);
				}else{
					dBCursor = dbcoll.find(query);
				}
			}
			for (DBObject _obj : dBCursor) {
				if (_obj.toMap().size() > 0) {
					T t = null;
					if (javaType != null) {
						t = binder.fromJson(_obj.toString(), clazz, javaType);
					} else {
						t = binder.fromJson(_obj.toString(), clazz);
					}
					result.add(t);
				}

			}
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_QUERY_ERROR);

		}
	}

	/**
	 * 创建索引
	 * 
	 * @param tableName
	 * @param indexMap
	 * @throws BusinessException
	 */
	public void createIndex(String tableName, Map<String, Object> indexMap)
			throws BusinessException {
		try {
			JsonBinder binder = JsonBinder.buildNormalBinder(false);
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject query = new BasicDBObject();
			query.putAll(indexMap);
			// 记录日志 供万一写数据失败 追回使用
			logger.info("[INDEX] " + binder.toJson(indexMap));
			dbcoll.createIndex(query);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_INSERT_ERROR);

		}

	}

	public void dropIndex(String tableName, Map<String, Object> indexMap)
			throws BusinessException {
		try {
			JsonBinder binder = JsonBinder.buildNormalBinder(false);
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject query = new BasicDBObject();
			query.putAll(indexMap);
			// 记录日志 供万一写数据失败 追回使用
			logger.info("[INDEX] " + binder.toJson(indexMap));
			dbcoll.dropIndex(query);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_INSERT_ERROR);

		}

	}

	/**
	 * 计数器查询
	 * 
	 * @param tableName
	 * @param selectQueryMap
	 * @return
	 * @throws BusinessException
	 */
	public Integer countObject(String tableName,
			Map<String, Object> selectQueryMap) throws BusinessException {
		try {
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject query = new BasicDBObject();
			query.putAll(selectQueryMap);
			Integer result = dbcoll.find(query).count();
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_QUERY_ERROR);

		}
	}
	
	public <T> List<T> aggregateObject(String tableName,List<Map<String,Object>> list,Class<T> clazz, JavaType javaType,JsonBinder binder)throws BusinessException{
		try {
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			List<BasicDBObject> pipeline = new LinkedList<BasicDBObject>();
			for(Map<String,Object> _map:list){
				BasicDBObject query=new BasicDBObject();
				query.putAll(_map);
				pipeline.add(query);
			}
			List<T> result = new ArrayList<T>();
			AggregationOutput outPut = dbcoll.aggregate(pipeline);
			for(DBObject obj:outPut.results()){
				if (obj.toMap().size() > 0) {
					T t=null;
					if (javaType != null) {
						t = binder.fromJson(obj.toString(), clazz, javaType);
					} else {
						t = binder.fromJson(obj.toString(), clazz);
					}
					result.add(t);
				}
			}
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_QUERY_ERROR);

		}
	}
	

	/**
	 * 查询mongodb
	 * 
	 * @param tableName
	 * @param selectQueryMap
	 * @param selectlocate
	 * @param isReturnId
	 * @return
	 * @throws BusinessException
	 */
	public List<Object> selectObject(String tableName,
			Map<String, Object> selectQueryMap, String selectlocate,
			boolean isReturnId) throws BusinessException {
		try {
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject query = new BasicDBObject();
			query.putAll(selectQueryMap);
			DBCursor dBCursor = null;
			List<Object> result = new ArrayList<Object>();
			if (selectlocate != null) {
				BasicDBObject projection = new BasicDBObject();
				projection.put(selectlocate, 1);
				if (!isReturnId) {
					projection.put("_id", 0);
				}
				dBCursor = dbcoll.find(query, projection);
			} else {
				if (!isReturnId) {
					BasicDBObject projection = new BasicDBObject();
					projection.put("_id", 0);
					dBCursor = dbcoll.find(query,projection);
				}else{
					dBCursor = dbcoll.find(query);
				}
			}
			for (DBObject _obj : dBCursor) {
				if (_obj.toMap().size() > 0)
					result.add(_obj);
			}
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_QUERY_ERROR);

		}
	}

	/**
	 * 
	 * @param tableName
	 * @param updateQueryMap
	 * @param updatelocate
	 * @param updatejsonObject
	 * @param isDlc
	 * @param isNewListItem
	 *            是否是新数组元素 仅对数组元素对象起作用
	 * @throws BusinessException
	 */
	public void updateArrayObject(String tableName,
			Map<String, Object> updateQueryMap, String updatelocate,
			Object updatejsonObject, boolean isDlc, Boolean isNewListItem)
			throws BusinessException {
		try {
			JsonBinder binder = JsonBinder.buildNormalBinder(false);
			String queryJson = binder.toJson(updateQueryMap);
			String updateJson = binder.toJson(updatejsonObject);
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject updateQuery = new BasicDBObject();
			updateQuery.putAll(updateQueryMap);
			BasicDBObject update = new BasicDBObject();
			if (!isDlc) {
				// 非追加模式 先清除
				update.put(updatelocate, new BasicDBObject("$exists", true));
				dbcoll.updateMulti(updateQuery, new BasicDBObject("$unset",
						update));
			}
			update.put(updatelocate, updatejsonObject);
			if (!(updatejsonObject instanceof List)) {
				dbcoll.updateMulti(updateQuery, new BasicDBObject("$push",
						update));
			} else {
				if (isNewListItem) {
					dbcoll.updateMulti(updateQuery, new BasicDBObject("$push",
							update));
				} else {
					dbcoll.updateMulti(updateQuery, new BasicDBObject(
							"$pushAll", update));
				}

			}

			// 记录日志 供万一写数据失败 追回使用
			logger.info("[UPDATE_QUERY] " + queryJson + " [UPDATE_LOCATE] "
					+ updatelocate + " [UPDATE] " + updateJson);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_UPDATE_ERROR);
		}
	}

	public void upsertCommonObject(String tableName,
			Map<String, Object> updateQueryMap, String updatelocate,
			Object updateMap) throws BusinessException {

		try {
			String updatejson = "";
			JsonBinder binder = JsonBinder.buildNormalBinder(false);
			updatejson = binder.toJson(updateMap);
			String queryJson = binder.toJson(updateQueryMap);
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject updateQuery = new BasicDBObject();
			updateQuery.putAll(updateQueryMap);
			BasicDBObject update = new BasicDBObject();
			// JSON形式 直接set
			if (updatelocate == null && updateMap instanceof Map) {
				update.putAll((Map<?, ?>) updateMap);
			} else {
				update.put(updatelocate, updateMap);
			}
			dbcoll.update(updateQuery, new BasicDBObject("$set", update), true,
					false);

			// 记录日志 供万一写数据失败 追回使用
			logger.info("[UPDATE_QUERY] " + queryJson + " [UPDATE_LOCATE] "
					+ updatelocate + " [UPDATE] " + updatejson);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_UPDATE_ERROR);
		}
	}

	public void updateCommonObject(String tableName,
			Map<String, Object> updateQueryMap, String updatelocate,
			Object updateMap) throws BusinessException {

		try {
			String updatejson = "";
			JsonBinder binder = JsonBinder.buildNormalBinder(false);
			updatejson = binder.toJson(updateMap);
			String queryJson = binder.toJson(updateQueryMap);
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject updateQuery = new BasicDBObject();
			updateQuery.putAll(updateQueryMap);
			BasicDBObject update = new BasicDBObject();
			// JSON形式 直接set
			update.put(updatelocate, updateMap);
			dbcoll.updateMulti(updateQuery, new BasicDBObject("$set", update));

			// 记录日志 供万一写数据失败 追回使用
			logger.info("[UPDATE_QUERY] " + queryJson + " [UPDATE_LOCATE] "
					+ updatelocate + " [UPDATE] " + updatejson);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_UPDATE_ERROR);
		}

	}
	
	public void executeUpdate(String tableName,
			Map<String, Object> updateQueryMap, Map<String, Object> updateMap)
			throws BusinessException {
		try {
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject updateQuery = new BasicDBObject();
			updateQuery.putAll(updateQueryMap);
			BasicDBObject update = new BasicDBObject();

			// JSON形式 直接set
			update.putAll(updateMap);
			dbcoll.update(updateQuery, update);

			// 记录日志 供万一写数据失败 追回使用
			for (String key : updateMap.keySet()) {
				String updatejson = "";
				JsonBinder binder = JsonBinder.buildNormalBinder(false);
				updatejson = binder.toJson(updateMap.get(key));
				String queryJson = binder.toJson(updateQueryMap);
				logger.info("[UPDATE_QUERY] " + queryJson + " [UPDATE_LOCATE] "
						+ key + " [UPDATE] " + updatejson);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_UPDATE_ERROR);
		}
	}

	public void batchUpdateCommonObject(String tableName,
			Map<String, Object> updateQueryMap, Map<String, Object> updateMap)
			throws BusinessException {

		try {
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject updateQuery = new BasicDBObject();
			updateQuery.putAll(updateQueryMap);
			BasicDBObject update = new BasicDBObject();

			// JSON形式 直接set
			update.putAll(updateMap);
			dbcoll.updateMulti(updateQuery, new BasicDBObject("$set", update));

			// 记录日志 供万一写数据失败 追回使用
			for (String key : updateMap.keySet()) {
				String updatejson = "";
				JsonBinder binder = JsonBinder.buildNormalBinder(false);
				updatejson = binder.toJson(updateMap.get(key));
				String queryJson = binder.toJson(updateQueryMap);
				logger.info("[UPDATE_QUERY] " + queryJson + " [UPDATE_LOCATE] "
						+ key + " [UPDATE] " + updatejson);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_UPDATE_ERROR);
		}

	}

	public void batchUpdateCommonObject(String tableName,
			Map<String, Object> updateQueryMap, Map<String, Object> updateMap,
			String createLocate, Map<String, Object> creatQueryMap, Object obj)
			throws BusinessException {

		try {
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject updateQuery = new BasicDBObject();
			updateQuery.putAll(updateQueryMap);
			BasicDBObject update = new BasicDBObject();

			if (createLocate != null) {
				// 没有则直接创建路径
				if (!this.checkObjectIsExist(tableName, updateQueryMap)) {
					this.updateArrayObject(tableName, creatQueryMap,
							createLocate, obj, true, false);
				}
			}

			// JSON形式 直接set
			update.putAll(updateMap);
			dbcoll.updateMulti(updateQuery, new BasicDBObject("$set", update));

			// 记录日志 供万一写数据失败 追回使用
			for (String key : updateMap.keySet()) {
				String updatejson = "";
				JsonBinder binder = JsonBinder.buildNormalBinder(false);
				updatejson = binder.toJson(updateMap.get(key));
				String queryJson = binder.toJson(updateQueryMap);
				logger.info("[UPDATE_QUERY] " + queryJson + " [UPDATE_LOCATE] "
						+ key + " [UPDATE] " + updatejson);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_UPDATE_ERROR);
		}

	}

	public void deleteCommonObject(String tableName,
			Map<String, Object> deleteQueryMap, String deletelocate)
			throws BusinessException {

		try {
			JsonBinder binder = JsonBinder.buildNormalBinder(false);
			String queryJson = binder.toJson(deleteQueryMap);
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject updateQuery = new BasicDBObject();
			updateQuery.putAll(deleteQueryMap);
			BasicDBObject delete = new BasicDBObject();
			delete.put(deletelocate, new BasicDBObject("$exists", true));
			dbcoll.updateMulti(updateQuery, new BasicDBObject("$unset", delete));
			// 记录日志 供万一写数据失败 追回使用
			logger.info("[DELETE_QUERY] " + queryJson + " [DELETE_LOCATE] "
					+ deletelocate);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_DELETE_ERROR);
		}

	}

	public void removeCommonObject(String tableName,
			Map<String, Object> deleteQueryMap) throws BusinessException {

		try {
			JsonBinder binder = JsonBinder.buildNormalBinder(false);
			String queryJson = binder.toJson(deleteQueryMap);
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject updateQuery = new BasicDBObject();
			updateQuery.putAll(deleteQueryMap);
			dbcoll.remove(updateQuery);
			// 记录日志 供万一写数据失败 追回使用
			logger.info("[REMOVE_QUERY] " + queryJson);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_DELETE_ERROR);
		}

	}

	public Boolean checkPropertyIsExist(String tableName,
			Map<String, Object> queryMap, String checkLocate) {
		DBCollection dbcoll = mongoTemplate.getDb().getCollection(tableName);
		BasicDBObject selectQuery = new BasicDBObject();
		queryMap.put(checkLocate, new BasicDBObject("$exists", true));
		selectQuery.putAll(queryMap);
		if (dbcoll.findOne(selectQuery) != null) {
			return true;
		} else {
			return false;
		}

	}

	public Boolean checkAppIsExist(String serviceId, String ak) {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("ak", ak);
		query.put("id", serviceId);
		return this.checkObjectIsExist("app_info", query);
	}

	public Boolean checkObjectIsExist(String tableName,
			Map<String, Object> queryMap) {
		DBCollection dbcoll = mongoTemplate.getDb().getCollection(tableName);
		BasicDBObject selectQuery = new BasicDBObject();
		selectQuery.putAll(queryMap);
		if (dbcoll.findOne(selectQuery) != null) {
			return true;
		} else {
			return false;
		}

	}

	public Boolean checkObjectIsExist(String tableName,
			List<Map<String, Object>> queryMapList) {
		DBCollection dbcoll = mongoTemplate.getDb().getCollection(tableName);
		BasicDBObject selectQuery = new BasicDBObject();
		selectQuery.put("$or", queryMapList);
		if (dbcoll.findOne(selectQuery) != null) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 
	 * @param tableName
	 * @param deleteQueryMap
	 * @param deletelocate
	 * @param deleteArray
	 * @param isNewListItem
	 *            是否是新数组元素 仅对数组元素对象起作用
	 * @throws BusinessException
	 */
	public void deleteArrayObject(String tableName,
			Map<String, Object> deleteQueryMap, String deletelocate,
			Object deleteArray, Boolean isNewListItem) throws BusinessException {

		try {
			JsonBinder binder = JsonBinder.buildNormalBinder(false);
			String queryJson = binder.toJson(deleteQueryMap);
			DBCollection dbcoll = mongoTemplate.getDb()
					.getCollection(tableName);
			BasicDBObject updateQuery = new BasicDBObject();
			updateQuery.putAll(deleteQueryMap);
			BasicDBObject delete = new BasicDBObject();
			delete.put(deletelocate, deleteArray);
			if (!(deleteArray instanceof List)) {
				dbcoll.updateMulti(updateQuery, new BasicDBObject("$pull",
						delete));

			} else {
				if (isNewListItem) {
					dbcoll.updateMulti(updateQuery, new BasicDBObject("$pull",
							delete));
				} else {
					dbcoll.updateMulti(updateQuery, new BasicDBObject(
							"$pullAll", delete));
				}

			}
			// 记录日志 供万一写数据失败 追回使用
			logger.info("[DELETE_QUERY] " + queryJson + " [DELETE_LOCATE] "
					+ deletelocate + " [DELETE_ARRAY] " + deleteArray);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new TrackerException(
					TrackerExceptionEnum.MONGONDB_DELETE_ERROR);
		}

	}

	private Integer verifyJsonStyle(String json) {
		if (json.indexOf("{") == 0) {
			return JSON_STYLE.MAP.getCode();
		} else if (json.indexOf("[") == 0) {
			return JSON_STYLE.ARRAY.getCode();
		} else {
			return JSON_STYLE.STRING.getCode();
		}
	}

	/**
	 * 将mongodb的对象转为通用对象
	 * 
	 * @param obj
	 * @return
	 */
	// @SuppressWarnings("unchecked")
	public Object transferMongoObjectoCommon(Object obj) {
		// if(obj instanceof BasicDBObject){
		// Map<Object,Object> map=((BasicDBObject)obj).toMap();
		// for(Object key:map.keySet()){
		// Object value=map.get(key);
		// value=transferMongoObjectoCommon(value);
		// map.put(key, value);
		// }
		// return map;
		// }else if(obj instanceof BasicDBList){
		// Object[] arrays=((BasicDBList)obj).toArray();
		// int i=0;
		// for(Object _obj:arrays){
		// _obj=transferMongoObjectoCommon(_obj);
		// arrays[i++]=_obj;
		// }
		// return arrays;
		// }
		return obj;
	}

}
