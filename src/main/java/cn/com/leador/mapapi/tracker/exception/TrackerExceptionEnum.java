package cn.com.leador.mapapi.tracker.exception;


public enum TrackerExceptionEnum {
	REDIS_EXCEPTION("t_1001","分布式缓存服务出现异常"),
	
	HTTP_METHOD_EXCEPTION("t_1002","HTTP请求方式错误"),
	
	MONGONDB_INSERT_ERROR("t_1003","插入MONGONDB异常"),
	MONGONDB_QUERY_ERROR("t_1004","查询MONGONDB异常"),
	MONGONDB_UPDATE_ERROR("t_1005","更新MONGONDB异常"),
	MONGONDB_DELETE_ERROR("t_1006","删除MONGONDB异常"),
	
	REDIS_NO_INIT("t_1007","分布式缓存服务初始化未完成,请稍后重试"),
	REDIS_APP_NAME_DUP("t_1008","服务名称已经存在"),
	REDIS_APP_ID_DUP("t_1009","服务ID已经存在"),
	REDIS_ENTITY_COLUMN_DUP("t_1011","实体字段已经存在"),
	REDIS_ENTITY_COLUMN_MAX("t_1012","实体字段达到最大数目"),
	REDIS_APP_ID_NOT_FOUND("t_1013","服务ID不存在"),
	REDIS_ENTITY_COLUMN_INDEX_MAX("t_1014","实体字段索引达到最大数目"),
	REDIS_ENTITY_NAME_DUP("t_1015","实体名称已经存在"),
	REDIS_ENTITY_MAX("t_1016","实体达到最大数目"),
	REDIS_ENTITY_COLUMN_INDEX_NOT_FOUND("t_1017","自定义检索字段不存在或者非索引字段"),
	
	;
	private String code;
	private String message;

	public String getCode() {
		return this.code;
	}

//	public void setCode(String code) {
//		this.code = code;
//	}

	public String getMessage() {
		return this.message;
	}

//	public void setMessage(String message) {
//		this.message = message;
//	}

	private TrackerExceptionEnum(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public static String getMessage(String code) {
		for (TrackerExceptionEnum exp : values()) {
			if (exp.getCode().equals(code)) {
				return exp.getMessage();
			}
		}
		return null;
	}

}
