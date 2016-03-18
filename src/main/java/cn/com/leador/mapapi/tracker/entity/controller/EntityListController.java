package cn.com.leador.mapapi.tracker.entity.controller;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cn.com.leador.mapapi.common.annotation.UriMapping;
import cn.com.leador.mapapi.common.annotation.WebController;
import cn.com.leador.mapapi.common.bean.ClientInfo;
import cn.com.leador.mapapi.common.bean.ResultBean;
import cn.com.leador.mapapi.common.helper.SpringContextHelper;
import cn.com.leador.mapapi.common.proxy.CommonInputProxy;
import cn.com.leador.mapapi.common.proxy.CommonOutputProxy;
import cn.com.leador.mapapi.common.proxy.HttpServletProxy;
import cn.com.leador.mapapi.tracker.entity.bean.EntityBean;
import cn.com.leador.mapapi.tracker.entity.input.EntityListInput;
import cn.com.leador.mapapi.tracker.entity.output.EntityListOutput;
@WebController
public class EntityListController extends HttpServletProxy {
	private Logger logger = LogManager.getLogger(this.getClass());
	
	private CommonInputProxy<List<EntityBean>> input=null;
	private CommonOutputProxy<List<EntityBean>> trans=null;
	@PostConstruct
	private void init(){
		if(logger.isDebugEnabled()){
			logger.debug("初始化"+this.getClass());
		}
		input=SpringContextHelper.getBeanByType(EntityListInput.class);
		trans=SpringContextHelper.getBeanByType(EntityListOutput.class);
	}

	@Override
	@UriMapping(value="/entity/list",sid="100005",name="查询实体")
	public void commonProceed(HttpServletRequest request,
			HttpServletResponse response, ClientInfo clientInfo) {
		ResultBean<List<EntityBean>> bean=null;
		try {
			bean=input.inputAndProcess(clientInfo, request);
			clientInfo.getResultMap().put("total", bean.getTotal());
			trans.printOut(response, bean, clientInfo);
		} catch (Exception e) {
			this.writeErrorMessage(response, trans, bean, clientInfo, e);
		}

	}

	@Override
	public void streamProceed(HttpServletResponse response, byte[] bytes) {
		// TODO Auto-generated method stub

	}

}
