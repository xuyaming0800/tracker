package cn.com.leador.mapapi.tracker.proxy;

import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import cn.com.leador.mapapi.common.bean.ClientInfo;
import cn.com.leador.mapapi.common.proxy.CommonOutputProxy;
import cn.com.leador.mapapi.common.util.json.JsonBinder;

public abstract class DefaultOutput<T> extends CommonOutputProxy<T> {

	protected final String transferCallbackData(String result,
			ClientInfo clientInfo) {
		if (clientInfo != null) {
			Map<String, String> map = clientInfo.getInfoTable();
			String callback = "";
			if (map != null) {
				callback = map.get("callback");
			}
			if (callback != null && !callback.equals("")) {
				result = callback + "(" + result + ")";
			}
		}
		return result;
	}

	@Override
	protected final String transferErrorData(String message, String status,
			ClientInfo clientInfo, String xmlRoot) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("status", status);
		map.put("message", message);
		return writeToOutput(map, clientInfo, xmlRoot);
	}

	protected final String writeToOutput(Map<String, Object> map,
			ClientInfo clientInfo, String xmlRoot) throws Exception {
		if (clientInfo.getInfoTable().get("output") != null
				&& clientInfo.getInfoTable().get("output").toUpperCase()
						.equals("XML")) {
			Document document = DocumentHelper.createDocument();
			Element nodeElement = document.addElement(xmlRoot);
			return doc2String(maptoXml(nodeElement, map).getDocument(), "utf-8");
		}
		return JsonBinder.buildNonNullBinder(false).toJson(map);
	}

}
