package org.fenixsoft.controller;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

/**
 * Created by IcyFenix on 2016-05-26.
 */

@RestController
@RequestMapping("/query")
public class QueryController {

    @Autowired
    private Mongo mongo;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @RequestMapping(value = "/{query}", method = RequestMethod.GET)
    @ResponseBody
    public String startPoint(@PathVariable String query) {
        DB db = mongo.getDB(databaseName);

        if (query == null || query.length() < 2) {
            return "非法的查询参数：" + query;
        }
        char startChar = query.charAt(0);
        if ((startChar > 'z' || startChar < 'a') && (startChar > '9' || startChar < '0')) {
            return "非法的查询参数：" + query;
        }

        String ret = "";
        BasicDBObject obj = new BasicDBObject();
        String collectionMark = query.substring(0, 2);
        if (query.indexOf("*") == -1) {
            // 精确匹配
            obj.put("n", query.toLowerCase());

        } else {
            // 模糊匹配
            Pattern pattern = Pattern.compile(query.replaceAll("\\*", "\\.\\*"));
            obj.put("n", pattern);
        }
        DBCursor cursor = db.getCollection(collectionMark).find(obj);
        while (cursor.hasNext()) {
            ret += cursor.next().get("c") + "<br>\n";
        }
        if (ret.length() > 0) {
            return ret;
        } else {
            return "查无此人";
        }
    }

}
