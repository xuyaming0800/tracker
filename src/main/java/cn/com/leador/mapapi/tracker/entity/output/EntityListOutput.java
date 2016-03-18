package cn.com.leador.mapapi.tracker.entity.output;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import cn.com.leador.mapapi.common.bean.ClientInfo;
import cn.com.leador.mapapi.common.exception.BusinessException;
import cn.com.leador.mapapi.common.exception.BusinessExceptionEnum;
import cn.com.leador.mapapi.tracker.entity.bean.EntityBean;
import cn.com.leador.mapapi.tracker.proxy.DefaultOutput;
@Component
public class EntityListOutput extends DefaultOutput<List<EntityBean>> {
	private Logger logger = LogManager.getLogger(this.getClass());

	@Override
	protected String transferSuccessData(List<EntityBean> t, ClientInfo clientInfo)
			throws Exception {
		try {
			Map<String,Object> map=new HashMap<String,Object>();
			map.put("status", "0");
			if(t==null||t.size()==0){
				map.put("message", "tracker实体未找到");
			}else{
				map.put("message", "成功");
				map.put("total", clientInfo.getResultMap().get("total"));
				map.put("size", t.size());
				for(EntityBean _bean:t){
					_bean.setCustom_field(new HashMap<String,Object>());
					List<Map<String, Object>> _listIndex=_bean.getCustom_field_index();
					List<Map<String, Object>> _listCommon=_bean.getCustom_field_common();
					if(_listIndex!=null){
						for(Map<String, Object> _map:_listIndex){
							_bean.getCustom_field().putAll(_map);
						}
					}
					if(_listCommon!=null){
						for(Map<String, Object> _map:_listCommon){
							_bean.getCustom_field().putAll(_map);
						}
					}
					_bean.setCustom_field_index(null);
					_bean.setCustom_field_common(null);
					if(_bean.getCustom_field().size()==0){
						_bean.setCustom_field(null);
					}
				}
				map.put("entities", t);
				
			}
			
			return writeToOutput(map, clientInfo, "app");
		} catch (Exception e) {
			BusinessException exception=new BusinessException(BusinessExceptionEnum.OUTPUT_TRANSFORM_ERROR,e);
			logger.error(exception.getMessage(),exception);
			throw exception;	
		}
	}

}
