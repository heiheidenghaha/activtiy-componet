package com.tydic.activiti_component.utils;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Http工具
 *
 * @author 
 */
public class HttpClientUtils {

    /**
     * 日志记录
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpClientUtils.class);

    /**
     * POST请求
     *
     * @param requestUrl  请求URL
     * @param requestData 请求参数
     * @return 请求结果
     */
    public static String post(String requestUrl, String requestData) {
        OutputStream out = null;
        BufferedReader reader = null;
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            out = connection.getOutputStream();
            out.write(requestData.getBytes());
            out.flush();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                LOGGER.error("Http请求失败失败码：" + responseCode);
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            LOGGER.error("HTTP通信失败", e);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(reader);
        }
        return result.toString();
    }

}
