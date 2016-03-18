package cn.com.leador.mapapi.tracker.test;

import cn.com.leador.mapapi.common.exception.BusinessException;
import cn.com.leador.mapapi.tracker.exception.TrackerException;
import cn.com.leador.mapapi.tracker.exception.TrackerExceptionEnum;

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
		BusinessException exception=new TrackerException(TrackerExceptionEnum.REDIS_EXCEPTION);
		throw exception;
	}

}
