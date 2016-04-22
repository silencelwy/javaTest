package com.ecmoho.sycm.schq.processor;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ecmoho.base.Util.StringUtil;
import com.ecmoho.base.bean.HeaderBean;
import com.ecmoho.sycm.schq.dao.SchqDbcom;
import com.ecmoho.sycm.schq.exploration.SchqHydpExploration;
import com.ecmoho.sycm.schq.exploration.SchqHyrcbExploration;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
/**
 * 
 * @author gusy
 * 行业直播schqhyzbprocessor
 */
@Component("schqHyrcbProcessor")
public class SchqHyrcbProcessor extends  SchqProcessor{
//	@Resource(name="schqDbcom")
//	private  SchqDbcom schqDbcom;
//	@Resource(name="schqHeaderBean")
//	private  HeaderBean schqHeaderBean;
	@Resource(name="schqHyrcbExploration")
	private SchqHyrcbExploration schqHyrcbExploration;
//	@Resource(name="schqHydpProcessor")
//    private SchqHyrcbProcessor schqHyrcbProcessor;
	public  void start(SchqHyrcbProcessor schqHyrcbProcessor){
		
		
		 //获取店铺列表
		 List<Map<String, Object>> taskList=schqDbcom.getSpidersTaskList("sycm");
		 for(int i=0;taskList!=null&&i<taskList.size();i++){
			 Map<String, Object> taskMap=taskList.get(i);
			 String id=StringUtil.objectVerString(taskMap.get("id"));
			 if(id.equals("11")){
				 String account=StringUtil.objectVerString(taskMap.get("account"));
				 String refer_cookie=StringUtil.objectVerString(taskMap.get("reffer_cookie"));
				 schqHeaderBean.setCookie(refer_cookie);
				 //'sscfx-hyrcb-rmssc','sscfx-hyrcb-rmcwc','sscfx-hyrcb-rmhxc','sscfx-hyrcb-rmppc','sscfx-hyrcb-rmxsc'
				 List<HashMap<String,String>> urlHyrcbList=schqHyrcbExploration.getHyrcbUrlList(account,"'sscfx-hyrcb-rmxsc'");
				 System.out.println(urlHyrcbList.size());
			     for(int j=0;j<urlHyrcbList.size();j++){
			    	 Map<String,String> map=urlHyrcbList.get(j);
			    	 schqHeaderBean.setUrlMap(map);
//			    	 System.out.println(map.get("targetUrl"));
			    	 Spider.create(schqHyrcbProcessor).addUrl(map.get("targetUrl")).run();
			     }
			 }
		 }
	}
	public static void main(String[] args) {
		ApplicationContext ac =new ClassPathXmlApplicationContext("conf/applicationContext.xml");
		SchqHyrcbProcessor schqHyrcbProcessor=(SchqHyrcbProcessor) ac.getBean("schqHyrcbProcessor");
		schqHyrcbProcessor.start(schqHyrcbProcessor);
	}
	@Override
	public Site getSite() {
		return super.getSite();
	}
    @Override
	public void process(Page page) {
    	
	   String jsonStr=page.getJson().toString();
//       System.out.println(jsonStr);
       Map<String,String> urlMap=schqHeaderBean.getUrlMap();
	   String childAccount=urlMap.get("childAccount");
       JSONObject jsonHotObject=null;
       JSONObject jsonSoarObject=null;
       Map<String,String> dataMap=null;
       List<Map<String, String>>  dataHotList=new ArrayList<Map<String,String>>();
       List<Map<String, String>>  dataSoarList=new ArrayList<Map<String,String>>();
       JSONArray jsonHotArray=JSON.parseObject(jsonStr).getJSONObject("content").getJSONObject("data").getJSONArray("hotList");
       JSONArray jsonSoarArray=JSON.parseObject(jsonStr).getJSONObject("content").getJSONObject("data").getJSONArray("soarList");
       for(int i=0;jsonHotArray!=null&&i<(jsonHotArray.size()>20?20:jsonHotArray.size());i++){
    	   jsonHotObject=jsonHotArray.getJSONObject(i);
    	   dataMap=getDataMap(urlMap);
    	   for(String  key:jsonHotObject.keySet()){
    		   dataMap.put(key,jsonHotObject.getString(key));
    	   }
    	   dataHotList.add(dataMap);
       }
       for(int j=0;jsonSoarArray!=null&&j<(jsonSoarArray.size()>20?20:jsonSoarArray.size());j++){
    	   jsonSoarObject=jsonHotArray.getJSONObject(j);
    	   dataMap=getDataMap(urlMap);
    	   for(String  key:jsonSoarObject.keySet()){
    		   dataMap.put(key,jsonSoarObject.getString(key));
    	   }
    	   schqDbcom.add(dataMap, "industryhotwords_hotattrwordssoarranking");
    	   dataSoarList.add(dataMap);
       }	   
     //'sscfx-hyrcb-rmssc','sscfx-hyrcb-rmcwc','sscfx-hyrcb-rmhxc','sscfx-hyrcb-rmppc','sscfx-hyrcb-rmxsc'
        switch (childAccount) {
			case "sscfx-hyrcb-rmssc"://搜索词分析_行业热词榜_热门搜索词
				 schqDbcom.addList(dataHotList, "industryhotwords_hotsearchwordsranking");
			     schqDbcom.addList(dataSoarList, "industryhotwords_hotsearchwordssoarranking");
				break;
			case "sscfx-hyrcb-rmcwc"://搜索词分析_行业热词榜_热门长尾词
				 schqDbcom.addList(dataHotList, "industryhotwords_hottailwordsranking");
			     schqDbcom.addList(dataSoarList, "industryhotwords_hottailwordssoarranking");
				break;
			case "sscfx-hyrcb-rmhxc"://搜索词分析_行业热词榜_热门核心词
				 schqDbcom.addList(dataHotList, "industryhotwords_hotcorewordsranking");
			     schqDbcom.addList(dataSoarList, "industryhotwords_hotcorewordssoarranking");
				break;
			case "sscfx-hyrcb-rmppc"://搜索词分析_行业热词榜_热门品牌词
				 schqDbcom.addList(dataHotList, "industryhotwords_hotbrandwordsranking");
			     schqDbcom.addList(dataSoarList, "industryhotwords_hotbrandwordssoarranking");
				break;
			case "sscfx-hyrcb-rmxsc"://搜索词分析_行业热词榜_热门修饰词
				 schqDbcom.addList(dataHotList, "industryhotwords_hotattrwordsranking");
			     schqDbcom.addList(dataSoarList, "industryhotwords_hotattrwordssoarranking");
				break;		
			default:
				break;
		}
      
	}
  //获取数据附加信息
    public static Map<String,String> getDataMap(Map<String,String> urlMap){
 	       HashMap<String,String> dataMap=new HashMap<String,String>();

 	       dataMap.put("accountid",urlMap.get("accountid"));
 		   dataMap.put("create_at", urlMap.get("create_at"));
 		   dataMap.put("level", urlMap.get("level"));
 		   dataMap.put("item1", urlMap.get("item1"));
 		   dataMap.put("item2", urlMap.get("item2"));
 		   dataMap.put("item3", urlMap.get("item3"));
 		   dataMap.put("device", urlMap.get("device"));
 		   dataMap.put("log_at", urlMap.get("log_at"));
		   return dataMap;
     }
}
