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
import cn.com.leador.mapapi.tracker.track.bean.TrackColumnBean;
@Component
public class TrackColumnRemoveOutput extends DefaultOutput<TrackColumnBean> {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Override
	protected String transferSuccessData(TrackColumnBean t, ClientInfo clientInfo)
			throws Exception {
		try {
			Map<String,Object> map=new HashMap<String,Object>();
			map.put("status", "0");
			map.put("message", "成功");
			return writeToOutput(map, clientInfo, "app");
		} catch (Exception e) {
			BusinessException exception=new BusinessException(BusinessExceptionEnum.OUTPUT_TRANSFORM_ERROR,e);
			logger.error(exception.getMessage(),exception);
			throw exception;	
		}
	}

}
