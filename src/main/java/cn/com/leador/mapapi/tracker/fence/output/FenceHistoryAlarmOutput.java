package cn.com.leador.mapapi.tracker.fence.output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import cn.com.leador.mapapi.common.bean.ClientInfo;
import cn.com.leador.mapapi.common.exception.BusinessException;
import cn.com.leador.mapapi.common.exception.BusinessExceptionEnum;
import cn.com.leador.mapapi.tracker.fence.bean.FenceAlarmBean;
import cn.com.leador.mapapi.tracker.proxy.DefaultOutput;
@Component
public class FenceHistoryAlarmOutput extends DefaultOutput<List<FenceAlarmBean>> {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Override
	protected String transferSuccessData(List<FenceAlarmBean> t, ClientInfo clientInfo)
			throws Exception {
		try {
			Map<String,Object> map=new HashMap<String,Object>();
			map.put("status", "0");
			map.put("message", "成功");
			
			if(t!=null&&t.size()>=0){
				List<Map<String,Object>> alarms=new ArrayList<Map<String,Object>>();
				
				Map<String,List<FenceAlarmBean>> map1=new LinkedHashMap<String,List<FenceAlarmBean>>();
				for(FenceAlarmBean bean:t){
					FenceAlarmBean _bean=new FenceAlarmBean();
					_bean.setAction(bean.getAction());
					_bean.setTime(bean.getTime());
					if(!map1.containsKey(bean.getMonitored_person())){
						map1.put(bean.getMonitored_person(), new ArrayList<FenceAlarmBean>());
					}
					map1.get(bean.getMonitored_person()).add(_bean);
				}
				
				for(String name:map1.keySet()){
					Map<String,Object> alarmMap=new LinkedHashMap<String,Object>();
					alarmMap.put("monitored_person", name);
					alarmMap.put("alarm_size", map1.get(name).size());
					alarmMap.put("alarms", map1.get(name));
					alarms.add(alarmMap);
				}
				map.put("size", alarms.size());
				map.put("monitored_person_alarms", alarms);
				
			}else{
				map.put("size", "0");
				map.put("monitored_person_alarms", new ArrayList<Object>());
			}
			
			return writeToOutput(map, clientInfo, "app");
		} catch (Exception e) {
			BusinessException exception=new BusinessException(BusinessExceptionEnum.OUTPUT_TRANSFORM_ERROR,e);
			logger.error(exception.getMessage(),exception);
			throw exception;	
		}
	}

}
