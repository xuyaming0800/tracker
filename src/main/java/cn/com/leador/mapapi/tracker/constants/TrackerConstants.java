package cn.com.leador.mapapi.tracker.constants;

public class TrackerConstants {
	private static final String CACHE_SUFFIX="LEADOR_TRACKER:";
	
	public static final String MAX_APP_ID_KEY=CACHE_SUFFIX+"TRACKER_MAX_APP_ID";
	//cache服务名称锁前置名称
	public static final String APP_NAME_PREFIX=CACHE_SUFFIX+"TRACKER_APP_NAME_PREFIX_";
	//cache实体字段名称锁前置名称
	public static final String ENTITY_COLUMN_KEY_PREFIX=CACHE_SUFFIX+"TRACKER_ENTITY_COLUMN_KEY_PREFIX_";
	//cache实体名称锁前置名称
	public static final String ENTITY_KEY_PREFIX=CACHE_SUFFIX+"TRACKER_ENTITY_KEY_PREFIX_";
	//cache轨迹字段名称锁前置名称
	public static final String TRACK_COLUMN_KEY_PREFIX=CACHE_SUFFIX+"TRACKER_TRACK_COLUMN_KEY_PREFIX_";
	//cache轨迹字段集合锁前置名称
	public static final String TRACK_COLUMN_COLLECT_KEY_PREFIX=CACHE_SUFFIX+"TRACKER_TRACK_COLUMN_COLLECT_KEY_PREFIX_";
	//cache服务最简版信息前置名称
	public static final String APP_INFO_PREFIX=CACHE_SUFFIX+"TRACKER_APP_INFO_PREFIX_";
	//删除的实体自定义对象列表
	public static final String REMOVE_ENTITY_COLUMN_LIST=CACHE_SUFFIX+"TRACKER_REMOVE_ENTITY_COLUMN_LIST";
	//删除的实体对象列表
	public static final String REMOVE_ENTITY_LIST=CACHE_SUFFIX+"TRACKER_REMOVE_ENTITY_LIST";
	//删除的轨迹自定义对象列表
	public static final String REMOVE_TRACK_COLUMN_LIST=CACHE_SUFFIX+"TRACKER_REMOVE_TRACK_COLUMN_LIST";
	

}
