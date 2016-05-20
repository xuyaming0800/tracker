package cn.com.leador.mapapi.tracker.fence.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import cn.com.leador.mapapi.common.bean.ClientInfo;
import cn.com.leador.mapapi.common.exception.BusinessException;
import cn.com.leador.mapapi.common.exception.BusinessExceptionEnum;
import cn.com.leador.mapapi.tracker.fence.bean.FenceBean;
import cn.com.leador.mapapi.tracker.proxy.DefaultOutput;
@Component
public class FenceQueryStatusOutput extends DefaultOutput<FenceBean> {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Override
	protected String transferSuccessData(FenceBean t, ClientInfo clientInfo)
			throws Exception {
		try {
			Map<String,Object> map=new HashMap<String,Object>();
			map.put("status", "0");
			map.put("message", "成功");
			if(t!=null){
				map.put("size", t.getMonitored_type().size());
				List<Map<String,Object>> l=new ArrayList<Map<String,Object>>();
				for( String name:t.getMonitored_type().keySet()){
					Map<String,Object> _map=new HashMap<String,Object>();
					_map.put("monitored_person", name);
					_map.put("monitored_status", t.getMonitored_type().get(name));
					l.add(_map);
				}
				map.put("monitored_person_statuses", l);
			}else{
				map.put("size", "0");
				map.put("monitored_person_statuses", new ArrayList<Object>());
			}
			
			return writeToOutput(map, clientInfo, "app");
		} catch (Exception e) {
			BusinessException exception=new BusinessException(BusinessExceptionEnum.OUTPUT_TRANSFORM_ERROR,e);
			logger.error(exception.getMessage(),exception);
			throw exception;	
		}
	}

}
