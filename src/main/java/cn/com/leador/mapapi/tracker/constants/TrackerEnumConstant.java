package cn.com.leador.mapapi.tracker.constants;

public class TrackerEnumConstant {
	public enum HTTP_METHOD {
		GET("GET"),POST("POST");
		private String method;

		public String getMethod() {
			return method;
		}

		private HTTP_METHOD(String method) {
			this.method = method;
		}
	}
	
	public enum IS_SEARCH {
		YES(1),NO(0);
		private int status;

		public int getStatus() {
			return status;
		}

		private IS_SEARCH(int status) {
			this.status = status;
		}
	}
	
	public enum ENTITY_RETURN_TYPE{
		FULL(0),SIMPLE(1);
		private int status;

		public int getStatus() {
			return status;
		}

		private ENTITY_RETURN_TYPE(int status) {
			this.status = status;
		}
	}
	
	public enum TRACK_RETURN_TYPE{
		FULL(0),SIMPLE(1);
		private int status;

		public int getStatus() {
			return status;
		}

		private TRACK_RETURN_TYPE(int status) {
			this.status = status;
		}
	}
	
	public enum TRACK_COLUMN_TYPE{
		INT64(1),DOUBLE(2),STRING(3);
		private int status;

		public int getStatus() {
			return status;
		}

		private TRACK_COLUMN_TYPE(int status) {
			this.status = status;
		}
	}
	
	public enum TRACK_GPS_TYPE{
		GPS(1),GCJ02(2),BAIDU(3);
		private int type;

		public int getType() {
			return type;
		}

		private TRACK_GPS_TYPE(int type) {
			this.type = type;
		}
	}
	
	public enum FENCE_CYCLE_TYPE{
		NO_REPEAT(1),WORKDAY(2),WEEKEND(3),EVERYDAY(4),CUSTOMER(5);
		private int type;

		public int getType() {
			return type;
		}

		private FENCE_CYCLE_TYPE(int type) {
			this.type = type;
		}
	}
	
	public enum FENCE_ENTITY_STATUS{
		NO_STATUS(0),IN(1),OUT(2);
		private int status;

		public int getStatus() {
			return status;
		}

		private FENCE_ENTITY_STATUS(int status) {
			this.status = status;
		}
	}
	
	public enum FENCE_ALARM_CONDITION_TYPE{
		ENTRY(1),EXIT(2),ALL(3);
		private int type;

		public int getType() {
			return type;
		}

		private FENCE_ALARM_CONDITION_TYPE(int type) {
			this.type = type;
		}
	}
	
	public enum FENCE_SHAPE_TYPE{
		CYCLE(1),POLYGON(2);
		private int type;

		public int getType() {
			return type;
		}

		private FENCE_SHAPE_TYPE(int type) {
			this.type = type;
		}
	}

	public enum TRACKER_SERVICE_TYPE {
		// CAR 车辆管理行业 MTK MTK位置穿戴 O2O O2O配送行业 OTHER 其他
		CAR("CAR"), MTK("MTK"), O2O("O2O"), OTHER("OTHER");
		private String code;

		public String getCode() {
			return code;
		}

		private TRACKER_SERVICE_TYPE(String code) {
			this.code = code;
		}
	}
	public enum IS_PROCESSED {
		YES(1),NO(0);
		private int type;

		public int getType() {
			return type;
		}

		private IS_PROCESSED(int type) {
			this.type = type;
		}
	}
	
	public enum TRACK_TIME_SORT_TYPE {
		ascending(1),descending(0);
		private int type;

		public int getType() {
			return type;
		}

		private TRACK_TIME_SORT_TYPE(int type) {
			this.type = type;
		}
	}
	

}
