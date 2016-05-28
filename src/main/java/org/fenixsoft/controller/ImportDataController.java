package org.fenixsoft.controller;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IcyFenix on 2016-05-20.
 */
@RestController
@RequestMapping("/import")
public class ImportDataController {

    @Autowired
    private Mongo mongo;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportDataController.class);

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Value("${org.fenixsoft.importPath}")
    private String importPath;

    @Value("${org.fenixsoft.informationCount}")
    private int informationCount;

    private static String getEmail(String line) {
        Pattern pattern = Pattern.compile("([a-z0-9]+[a-z0-9._%+-]*)@[a-z0-9.-]+\\.[a-z]{2,4}");
        Matcher matcher = pattern.matcher(line.toLowerCase());
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    private static int getLineNumber(File file) {
        try {
            LineNumberReader reader = new LineNumberReader(new FileReader(file));
            reader.skip(Long.MAX_VALUE);
            reader.close();
            return reader.getLineNumber();
        } catch (Exception e) {
            return -1;
        }
    }

    private void importFromFile(File file) {
        if (file.getName().endsWith(".done")) {
            LOGGER.info("文件：" + file + "已被导入过，跳过该文件");
            return;
        }
        int lineNumber = getLineNumber(file);
        LOGGER.info("开始导入文件：" + file + "，总行数：" + lineNumber);

        DB db = mongo.getDB(databaseName);

        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            int count = 0, total = 0;
            long start = System.currentTimeMillis();
            for (String line = in.readLine(); line != null; line = in.readLine()) {

                count++;
                total++;
                if (count >= informationCount) {
                    count = 0;
                    long now = System.currentTimeMillis();
                    long time = now - start;
                    long remainTime = (long) (time * ((lineNumber - total - 0.0f) / total));

                    String pecent = (((float) total) / lineNumber * 100) + "%";
                    LOGGER.info("进度：" + total + "/" + lineNumber + " （" + pecent + "）， 预计剩余时间：" + remainTime + "ms");

                }
                String email = getEmail(line);
                if (email != null && email.length() >= 2) {
                    String collectionMark = email.substring(0, 2);
                    char startChar = collectionMark.charAt(0);
                    if ((startChar > 'z' || startChar < 'a') && (startChar > '9' || startChar < '0')) {
                        // 首字符是特殊字符的email属于无效数据
                        LOGGER.warn("忽略非法数据：[email:" + email + ",content:" + line + "]");
                        continue;
                    }
                    try {
                        BasicDBObject obj = new BasicDBObject();
                        obj.put("n", email);
                        obj.put("c", line);
                        db.getCollection(collectionMark).insert(obj);
                    } catch (Exception e) {
                        LOGGER.error("出现异常数据：[email:" + email + ",content:" + line + ",message:" + e.getMessage() + "]");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            file.renameTo(new File(file.getAbsolutePath() + ".error"));
        }
        file.renameTo(new File(file.getAbsolutePath() + ".done"));
    }

    private void importFromDir(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                importFromDir(file);
            } else {
                importFromFile(file);
            }
        }
    }

    private void setupIndex() {
        LOGGER.info("开始创建索引");
        DB db = mongo.getDB(databaseName);
        db.getCollectionNames().stream().
                filter(collectionMark -> collectionMark.indexOf("system.") == -1).
                forEach(collectionMark -> db.getCollection(collectionMark).createIndex("n"));
        LOGGER.info("索引创建完毕");
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public String startPoint() {
        importFromDir(new File(importPath));
        setupIndex();
        return "导入完成";
    }
}
