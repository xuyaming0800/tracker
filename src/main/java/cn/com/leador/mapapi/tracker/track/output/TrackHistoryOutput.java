package cn.com.leador.mapapi.tracker.track.output;

import java.util.HashMap;
import java.util.List;
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
public class TrackHistoryOutput extends DefaultOutput<List<TrackBean>> {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Override
	protected String transferSuccessData(List<TrackBean> t, ClientInfo clientInfo)
			throws Exception {
		try {
			Map<String,Object> map=new HashMap<String,Object>();
			map.put("status", "0");
			map.put("message", "成功");
			map.put("entity_name", clientInfo.getResultMap().get("entity_name"));
			map.put("total", clientInfo.getResultMap().get("total"));
			map.put("size", clientInfo.getResultMap().get("size"));
			map.put("distance", clientInfo.getResultMap().get("distance"));
			map.put("points", t);
			return writeToOutput(map, clientInfo, "app");
		} catch (Exception e) {
			BusinessException exception=new BusinessException(BusinessExceptionEnum.OUTPUT_TRANSFORM_ERROR,e);
			logger.error(exception.getMessage(),exception);
			throw exception;	
		}
	}

}
