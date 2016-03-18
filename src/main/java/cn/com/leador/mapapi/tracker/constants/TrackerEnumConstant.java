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

	public enum TRACKER_SERVICE_TYPE {
		// CAR 车辆管理行业 MTK MTK位置穿戴 O2O O2O配送行业 OTHER 其他
		CAR("CAR"), MTK("v2"), O2O("O2O"), OTHER("OTHER");
		private String code;

		public String getCode() {
			return code;
		}

		private TRACKER_SERVICE_TYPE(String code) {
			this.code = code;
		}
	}
	

}
