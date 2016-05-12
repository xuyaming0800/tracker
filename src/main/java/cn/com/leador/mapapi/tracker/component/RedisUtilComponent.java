package cn.com.leador.mapapi.tracker.component;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import cn.com.leador.mapapi.common.exception.BusinessException;
import cn.com.leador.mapapi.common.util.json.JsonBinder;
import cn.com.leador.mapapi.tracker.exception.TrackerException;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;

import com.fasterxml.jackson.databind.JavaType;
@Component
public class RedisUtilComponent {
	private Logger logger = LogManager.getLogger(this.getClass());
	@Resource
	private JedisPool jedisPool;
	
	public Jedis getRedisInstance()throws BusinessException{
		Jedis jedis=null;
		try {
			jedis = jedisPool.getResource();
			return jedis;
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}
	}
	
    public void setRedisStringCache(Jedis jedis,String key,String value,int expire)throws BusinessException{
    	if(jedis==null){
    		throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
    	}
    	try {
			jedis.set(key,value);
			if(expire>0){
				jedis.expire(key, expire);
			}
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}
    }
    public void setRedisStringCache(String key,String value,int expire)throws BusinessException{
    	Jedis jedis=null;
    	try {
    		jedis=this.getRedisInstance();
			jedis.set(key,value);
			if(expire>0){
				jedis.expire(key, expire);
			}
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}finally{
			this.returnRedis(jedis);
		}
    }
	
	public <T> void setRedisJsonCache(Jedis jedis,String key,T object,int expire)throws BusinessException{
		if(jedis==null){
    		throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
    	}
		try {
			jedis.set(key, JsonBinder.buildNormalBinder(false).toJson(object));
			if(expire>0){
				jedis.expire(key, expire);
			}
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}
	}
	
	public <T> void setRedisJsonCache(String key,T object,int expire)throws BusinessException{
		Jedis jedis=null;
		try {
			jedis=this.getRedisInstance();
			jedis.set(key, JsonBinder.buildNormalBinder(false).toJson(object));
			if(expire>0){
				jedis.expire(key, expire);
			}
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}finally{
			this.returnRedis(jedis);
		}
	}
	
	public <T> void setRedisJsonCache(Jedis jedis,String key,T object,JsonBinder binder,int expire)throws BusinessException{
		if(jedis==null){
    		throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
    	}
		try {
			jedis.set(key, binder.toJson(object));
			if(expire>0){
				jedis.expire(key, expire);
			}
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}
	}
	
	public <T> void setRedisJsonCache(String key,T object,JsonBinder binder,int expire)throws BusinessException{
		Jedis jedis=null;
		try {
			jedis=this.getRedisInstance();
			jedis.set(key, binder.toJson(object));
			if(expire>0){
				jedis.expire(key, expire);
			}
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}finally{
			this.returnRedis(jedis);
		}
	}
	
	public <T> T getRedisJsonCache(Jedis jedis,String key,Class<T> clazz)throws BusinessException{
		if(jedis==null){
    		throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
    	}
		try {
			String value=jedis.get(key);
			if(value!=null){
				return JsonBinder.buildNormalBinder(false).fromJson(jedis.get(key), clazz);
			}
			return null;
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}
	}
	
	public <T> T getRedisJsonCache(String key,Class<T> clazz)throws BusinessException{
		Jedis jedis=null;
		try {
			jedis=this.getRedisInstance();
			String value=jedis.get(key);
			if(value!=null){
				return JsonBinder.buildNormalBinder(false).fromJson(jedis.get(key), clazz);
			}
			return null;
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}finally{
			this.returnRedis(jedis);
		}
	}
	
	public <T> T getRedisJsonCache(Jedis jedis,String key,Class<T> clazz,JsonBinder binder)throws BusinessException{
		if(jedis==null){
    		throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
    	}
		try {
			String value=jedis.get(key);
			if(value!=null){
				return binder.fromJson(jedis.get(key), clazz);
			}
			return null;
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}
	}
	
	public <T> T getRedisJsonCache(Jedis jedis,String key,Class<T> clazz,JsonBinder binder,JavaType javaType)throws BusinessException{
		if(jedis==null){
    		throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
    	}
		try {
			String value=jedis.get(key);
			if(value!=null){
				return binder.fromJson(jedis.get(key), clazz,javaType);
			}
			return null;
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}
	}
	
	public <T> T getRedisJsonCache(String key,Class<T> clazz,JsonBinder binder)throws BusinessException{
		Jedis jedis=null;
		try {
			jedis=this.getRedisInstance();
			String value=jedis.get(key);
			if(value!=null){
				return binder.fromJson(jedis.get(key), clazz);
			}
			return null;
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}finally{
			this.returnRedis(jedis);
		}
	}
	public void releaseRedisCache(Jedis jedis,String key)throws BusinessException{
		if(jedis==null){
    		throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
    	}
		try {
			jedis.del(key);
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}
	}
	public void releaseRedisCache(String key)throws BusinessException{
		Jedis jedis=null;
		try {
			jedis=this.getRedisInstance();
			jedis.del(key);
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}finally{
			this.returnRedis(jedis);
		}
	}
	
	
	public void returnRedis(Jedis jedis){
		try {
			jedis.close();
		} catch (Exception e) {
			logger.error("redis关闭异常",e);
		}
	}
	public void lockIdByRedis(String prefix,String id, TrackerExceptionEnum enums, int expire,Jedis jedis) throws BusinessException {
		if(jedis==null){
    		throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
    	}
		Long returnValue = 1L;
		try {
			String key=prefix+"_"+id;
			returnValue = jedis.sadd(key, id);
			if (returnValue == 0) {
				// 给当前taskId加锁
				// 注意：旧版本的jedis失败是返回-1，目前使用的2.4.2失败返回0，成功返回1
				// 并发重复，保存失败
				logger.info("key=["+key+"]正在被锁定");
				throw new TrackerException(enums);
			}
			logger.info("key=["+key+"]将锁定"+expire+"秒");
			jedis.expire(key, expire);
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}
	}
	
	
	public void lockIdByRedis(String prefix,String id, TrackerExceptionEnum enums, int expire) throws BusinessException {
		Jedis jedis=null;
		Long returnValue = 1L;
		try {
			jedis=this.getRedisInstance();
			String key=prefix+"_"+id;
			returnValue = jedis.sadd(key, id);
			if (returnValue == 0) {
				// 给当前taskId加锁
				// 注意：旧版本的jedis失败是返回-1，目前使用的2.4.2失败返回0，成功返回1
				// 并发重复，保存失败
				logger.info("key=["+key+"]正在被锁定");
				throw new TrackerException(enums);
			}
			logger.info("key=["+key+"]将锁定"+expire+"秒");
			jedis.expire(key, expire);
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}finally{
			this.returnRedis(jedis);
		}
	}
	/**
	 * 往LIST队尾插入值
	 * @param key
	 * @param value
	 * @param jedis
	 * @throws BusinessException
	 */
	public void appendPush(String key, String value,Jedis jedis) throws BusinessException {
		if(jedis==null){
    		throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
    	}
		try {
			jedis.rpush(key, value);
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}
	}
	public void appendPush(String key, String value) throws BusinessException {
		Jedis jedis=null;
		try {
			jedis=this.getRedisInstance();
			jedis.rpush(key, value);
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}finally{
			this.returnRedis(jedis);
		}
	}
	/**
	 * 从LIST对头取得并弹出值
	 * @param key
	 * @param jedis
	 * @return
	 * @throws BusinessException
	 */
	public String prependPop(String key, Jedis jedis) throws BusinessException {
		if(jedis==null){
    		throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
    	}
		try {
			String value=jedis.lpop(key);
			return value;
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}
	}
	public String prependPop(String key) throws BusinessException {
		Jedis jedis=null;
		try {
			jedis=this.getRedisInstance();
			String value=jedis.lpop(key);
			return value;
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}finally{
			this.returnRedis(jedis);
		}
	}
	public Long getListSize(String key, Jedis jedis) throws BusinessException {
		if(jedis==null){
    		throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
    	}
		try {
			Long value=jedis.llen(key);
			return value;
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}
	}
	public Long getListSize(String key) throws BusinessException {
		Jedis jedis=null;
		try {
			jedis=this.getRedisInstance();
			Long value=jedis.llen(key);
			return value;
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}finally{
			this.returnRedis(jedis);
		}
	}
	
	public boolean checkIdIsLockedByRedis(String prefix,String id) throws BusinessException{
		Jedis jedis=null;
		try {
			jedis=this.getRedisInstance();
			String key=prefix+"_"+id;
			if(jedis.exists(key)){
				return true;
			}
			return false;
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}finally{
			this.returnRedis(jedis);
		}
	}
	
	
    public void releaseIdByRedis(String prefix,String id,Jedis jedis) throws BusinessException {
    	if(jedis==null){
    		throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
    	}
    	try {
			String key=prefix+"_"+id;
			logger.info("请求释放key=["+key+"]");
			jedis.del(key);
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}
	}
    
    public void releaseIdByRedis(String prefix,String id) throws BusinessException {
    	Jedis jedis=null;
    	try {
    		jedis=this.getRedisInstance();
			String key=prefix+"_"+id;
			logger.info("请求释放key=["+key+"]");
			jedis.del(key);
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}finally{
			this.returnRedis(jedis);
		}
	}
    public void releaseIdsByRedis(String[] keys) throws BusinessException {
    	Jedis jedis=null;
    	try {
    		jedis=this.getRedisInstance();
    		for(String key:keys){
    			logger.info("请求释放key=["+key+"]");
    			jedis.del(key);
    		}
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}finally{
			this.returnRedis(jedis);
		}
	}
//	
//	
//	
//	public Jedis lockPackageIdByRedis(Long packageId, TrackerExceptionEnum enums, int expire,Jedis jedis) throws BusinessException {
//		Long returnValue = 1L;
//		try {
//			if(jedis==null)
//			jedis = jedisPool.getResource();
//			String key=CommonConstant.REDIS_LOCK_PACKAGE_KEY+"_"+packageId.toString();
//			returnValue = jedis.sadd(key, packageId.toString());
//			jedis.expire(key, expire);
//			if (returnValue == 0) {
//				// 给当前taskId加锁
//				// 注意：旧版本的jedis失败是返回-1，目前使用的2.4.2失败返回0，成功返回1
//				// 并发重复，保存失败
//				throw new TrackerException(enums);
//			}
//		} catch (Exception e) {
//			if (jedis != null) {
//				jedisPool.returnBrokenResource(jedis);
//				jedis=null;
//			}
//			if(e instanceof BusinessException){
//				throw (BusinessException)e;
//			}else{
//				logger.error(e.getMessage(),e);
//				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
//			}
//		}
//		return jedis;
//	}
//	
//	public void releasePackageIdByRedis(Long packageId,Jedis jedis) throws BusinessException {
//		try {
//			String key=CommonConstant.REDIS_LOCK_PACKAGE_KEY+"_"+packageId.toString();
//			jedis.del(key);
//		} catch (Exception e) {
//			if (jedis != null) {
//				jedisPool.returnBrokenResource(jedis);
//				jedis=null;
//			}
//			if(e instanceof BusinessException){
//				throw (BusinessException)e;
//			}else{
//				logger.error(e.getMessage(),e);
//				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
//			}
//		}
//	}
//	public Jedis lockTaskIdByRedis(Long passiveId, TrackerExceptionEnum enums, int expire,Jedis jedis) throws BusinessException {
//		Long returnValue = 1L;
//		try {
//			if(jedis==null)
//			jedis = jedisPool.getResource();
//			String key=CommonConstant.REDIS_LOCK_TASK_KEY+"_"+passiveId.toString();
//			returnValue = jedis.sadd(key, passiveId.toString());
//			/*
//			 * 在expire（秒）时间内不允许对相同的任务进行操作， 另外因为目前新框架的切面逻辑是在service结束后才会提交，
//			 * 所以如果在finally里释放当前的taskId，还是会出现并发的问题，
//			 * 最终采用直接设置过期时间10秒，最后也不释放，完全等待10秒的过期时间。
//			 */
//			jedis.expire(key, expire);
//			if (returnValue == 0) {
//				// 给当前taskId加锁
//				// 注意：旧版本的jedis失败是返回-1，目前使用的2.4.2失败返回0，成功返回1
//				// 并发重复，保存失败
//				throw new TrackerException(enums);
//				
//			}
//		} catch (Exception e) {
//			if (jedis != null) {
//				jedisPool.returnBrokenResource(jedis);
//				jedis=null;
//			}
//			if(e instanceof BusinessException){
//				throw (BusinessException)e;
//			}else{
//				logger.error(e.getMessage(),e);
//				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
//			}
//		}
//		return jedis;
//	}
//	public void releaseTaskIdByRedis(Long passiveId,Jedis jedis) throws BusinessException {
//		try {
//			String key=CommonConstant.REDIS_LOCK_TASK_KEY+"_"+passiveId.toString();
//			jedis.del(key);
//		} catch (Exception e) {
//			if (jedis != null) {
//				jedisPool.returnBrokenResource(jedis);
//				jedis=null;
//			}
//			if(e instanceof BusinessException){
//				throw (BusinessException)e;
//			}else{
//				logger.error(e.getMessage(),e);
//				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
//			}
//		}
//	}
	public String getRedisStringCache(String key,Jedis jedis)throws BusinessException{
		if(jedis==null){
    		throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
    	}
		try {
			return jedis.get(key);
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}
	}
	public String getRedisStringCache(String key)throws BusinessException{
		Jedis jedis=null;
		try {
			jedis=this.getRedisInstance();
			return jedis.get(key);
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}finally{
			this.returnRedis(jedis);
		}
	}
	
	public Long increament(String key,Jedis jedis)throws BusinessException{
		if(jedis==null){
    		throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
    	}
		try {
			return jedis.incr(key);
		} catch (Exception e) {
			if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				logger.error(e.getMessage(),e);
				throw new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
			}
		}
	}
}
