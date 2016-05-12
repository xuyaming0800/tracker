package cn.com.leador.mapapi.tracker.track.output;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import cn.com.leador.mapapi.common.bean.ClientInfo;
import cn.com.leador.mapapi.common.exception.BusinessException;
import cn.com.leador.mapapi.common.exception.BusinessExceptionEnum;
import cn.com.leador.mapapi.tracker.proxy.DefaultOutput;
import cn.com.leador.mapapi.tracker.track.bean.TrackBean;
@Component
public class TrackBatchAddOutput extends DefaultOutput<TrackBean> {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Override
	protected String transferSuccessData(TrackBean t, ClientInfo clientInfo)
			throws Exception {
		try {
			Map<String,Object> map=new HashMap<String,Object>();
			map.put("status", "0");
			if(t.getError_points()==null||t.getError_points().size()==0){
				map.put("message", "成功");
			}else{
				map.put("message", "失败"+t.getError_points().size()+"条, 请检查数据格式，且loc_time不能超过当前时间10分钟");
				map.put("error_points", t.getError_points());
			}
			map.put("total", t.getTotal());
			map.put("time", t.getTime());
			return writeToOutput(map, clientInfo, "app");
		} catch (Exception e) {
			BusinessException exception=new BusinessException(BusinessExceptionEnum.OUTPUT_TRANSFORM_ERROR,e);
			logger.error(exception.getMessage(),exception);
			throw exception;	
		}
	}

}
