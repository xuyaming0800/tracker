package cn.com.leador.mapapi.tracker.test;

import java.text.DecimalFormat;

public class Test {

	public static void main(String[] args)throws Exception {
//		List<MongoCredential> credentials = new ArrayList<MongoCredential>();
//        credentials.add(MongoCredential.createScramSha1Credential("test",
//                "test", "93f0cb0614".toCharArray()));
//
//		ServerAddress serverAddress = new ServerAddress("10.211.55.8", 27017); 
//		MongoClient mongoClient = new MongoClient(serverAddress,credentials);
//		MongoDatabase db=mongoClient.getDatabase("test");
//		Document doc=new Document(); 
//		doc.put("$currentDate", new Document().append("time", true));
        //insert key  
//        doc.put("name", "hello");  
//        doc.put("age", "24");  
//        doc.put("time", new Date()); 
//        Long time=new Date().getTime();
//        Thread.sleep(10000);
//        doc.put("time1", new Date(time)); 
//		db.getCollection("test").insertOne(doc);
//		BasicDBObject update = 
//		          new BasicDBObject("$set", new     BasicDBObject("name","john")
//		              .append("$currentDate", new BasicDBObject("time",true)));
//      
//		db.getCollection("test").updateOne(new Document("name", "hello"), update);
//		BasicDBObject obj=new BasicDBObject();
//		obj.put("name", "123");
//		obj.put("createTime", null);
//		 BasicDBObject query = new BasicDBObject("obj",obj);
//		 BasicDBObject update = new BasicDBObject("$currentDate",
//		     new BasicDBObject("obj.createTime", true)
//		 );
//		 UpdateOptions opt=new UpdateOptions();
//		 opt.upsert(true);
//		 db.getCollection("test").updateOne(query, update,opt);
//		BasicDBObject obj=new BasicDBObject("obj.name","123");
//		BasicDBObject projection = new BasicDBObject();
//		projection.put("obj.name", 1);
//		projection.put("obj.createTime", 1);
//		projection.put("_id", 0);
//		FindIterable<Document> iterable=db.getCollection("test").find(obj).projection(projection);
//		
//		System.out.println(((Document)iterable.first().get("obj")).get("createTime"));
//		mongoClient.close();
//		BusinessException exception=new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
//		throw exception;
//		Object[] a=new Object[3];
//		a[0]=new Double[]{1D,2D};
//		a[1]=new Double[]{2D,3D};
//		a[2]=new Double[]{3D,4D};
//		Object[] b=new Object[]{a};
//		Map<String,Object> map=new HashMap<String,Object>();
//		map.put("coords", b);
//		JsonBinder binder=JsonBinder.buildNonNullBinder(false);
//		String json=binder.toJson(map);
//		System.out.println(json);
		
		
//		List<List<Double[]>> _list=new ArrayList<List<Double[]>>();
//		List<Double[]> _l=new ArrayList<Double[]>();
//		_l.add(new Double[]{1D,2D});
//		_l.add(new Double[]{1D,2D});
//		_l.add(new Double[]{1D,2D});
//		_list.add(_l);
//		Map<String,Object> map=new HashMap<String,Object>();
//		map.put("coords",_list);
//		JsonBinder binder=JsonBinder.buildNonNullBinder(false);
//		String json=binder.toJson(map);
//		System.out.println(json);
		
//		String s="0830";
//		System.out.println(Integer.valueOf(s));
		
//		System.out.println(new Date().getTime()/1000);
		

        DecimalFormat   df   =new  DecimalFormat("#.000000");  
        String s=df.format(116.1234D);
        System.out.println(Double.valueOf(s));

	}

}
