package cn.com.leador.mapapi.tracker.track.controller;

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
import cn.com.leador.mapapi.tracker.track.bean.TrackColumnBean;
import cn.com.leador.mapapi.tracker.track.input.TrackColumnRemoveInput;
import cn.com.leador.mapapi.tracker.track.output.TrackColumnRemoveOutput;
@WebController
public class TrackColumnRemoveController extends HttpServletProxy {
	private Logger logger = LogManager.getLogger(this.getClass());
	
	private CommonInputProxy<TrackColumnBean> input=null;
	private CommonOutputProxy<TrackColumnBean> trans=null;
	@PostConstruct
	private void init(){
		if(logger.isDebugEnabled()){
			logger.debug("初始化"+this.getClass());
		}
		input=SpringContextHelper.getBeanByType(TrackColumnRemoveInput.class);
		trans=SpringContextHelper.getBeanByType(TrackColumnRemoveOutput.class);
	}

	@Override
	@UriMapping(value="/track/deletecolumn",sid="100014",name="track自定义字段删除")
	public void commonProceed(HttpServletRequest request,
			HttpServletResponse response, ClientInfo clientInfo) {
		ResultBean<TrackColumnBean> bean=null;
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
