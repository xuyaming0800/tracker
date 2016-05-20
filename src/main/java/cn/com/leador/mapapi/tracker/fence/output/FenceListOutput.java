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
public class FenceListOutput extends DefaultOutput<List<FenceBean>> {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Override
	protected String transferSuccessData(List<FenceBean> t, ClientInfo clientInfo)
			throws Exception {
		try {
			Map<String,Object> map=new HashMap<String,Object>();
			map.put("status", "0");
			map.put("message", "成功");
			if(t!=null&&t.size()>0){
				for(FenceBean bean:t){
					bean.setCenterSphere(null);
					bean.setCoords(null);
					bean.setAk(null);
					bean.setService_id(null);
					bean.setCreate_timestamp(null);
					bean.setModify_timestamp(null);
					bean.setMonitored_type(null);
				}
				map.put("size", t.size());
				map.put("fences", t);
			}else{
				map.put("size", "0");
				map.put("fences", new ArrayList<Object>());
			}
			
			return writeToOutput(map, clientInfo, "app");
		} catch (Exception e) {
			BusinessException exception=new BusinessException(BusinessExceptionEnum.OUTPUT_TRANSFORM_ERROR,e);
			logger.error(exception.getMessage(),exception);
			throw exception;	
		}
	}

}
