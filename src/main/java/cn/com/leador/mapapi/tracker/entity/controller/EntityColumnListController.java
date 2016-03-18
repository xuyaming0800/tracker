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
import cn.com.leador.mapapi.tracker.entity.bean.EntityColumnBean;
import cn.com.leador.mapapi.tracker.entity.input.EntityColumnListInput;
import cn.com.leador.mapapi.tracker.entity.output.EntityColumnListOutput;
@WebController
public class EntityColumnListController extends HttpServletProxy {
	private Logger logger = LogManager.getLogger(this.getClass());
	
	private CommonInputProxy<List<EntityColumnBean>> input=null;
	private CommonOutputProxy<List<EntityColumnBean>> trans=null;
	@PostConstruct
	private void init(){
		if(logger.isDebugEnabled()){
			logger.debug("初始化"+this.getClass());
		}
		input=SpringContextHelper.getBeanByType(EntityColumnListInput.class);
		trans=SpringContextHelper.getBeanByType(EntityColumnListOutput.class);
	}

	@Override
	@UriMapping(value="/entity/listcolumn",sid="100003",name="增加自定义字段")
	public void commonProceed(HttpServletRequest request,
			HttpServletResponse response, ClientInfo clientInfo) {
		ResultBean<List<EntityColumnBean>> bean=null;
		try {
			bean=input.inputAndProcess(clientInfo, request);
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
