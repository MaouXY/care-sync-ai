package com.caresync.ai.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 阿里云OSS工具类
 */
@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {
    /**
     * 阿里云OSS服务端地址
     */
    private String endpoint;
    /**
     * 阿里云OSS访问密钥ID
     */
    private String accessKeyId;
    /**
     * 阿里云OSS访问密钥Secret
     */
    private String accessKeySecret;
    /**
     * 阿里云OSS存储桶名称
     */
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes 文件字节数组
     * @param objectName 文件在OSS中的存储名称
     * @return 文件访问URL
     */
    public String upload(byte[] bytes, String objectName) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        // 拼接构建文件访问路径
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }

    /**
     * 将OSS URL内容转换为字符串
     *
     * @param ossUrl OSS文件的URL地址
     * @return 文件内容字符串
     * @throws IOException 当网络连接或读取文件失败时抛出
     */
    public static String ossUrlToString(String ossUrl) throws IOException {
        if (ossUrl == null || ossUrl.isEmpty()) {
            throw new IllegalArgumentException("OSS URL不能为空");
        }

        URL url = new URL(ossUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            // 设置连接参数
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5秒连接超时
            connection.setReadTimeout(10000);   // 10秒读取超时

            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("获取OSS文件失败，响应码: " + responseCode);
            }

            // 读取内容
            InputStream inputStream = connection.getInputStream();
            StringBuilder content = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            } finally {
                inputStream.close();
            }

            return content.toString();
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 将OSS URL内容转换为字符串（带重试机制）
     *
     * @param ossUrl OSS文件的URL地址
     * @param retryTimes 重试次数
     * @return 文件内容字符串
     * @throws IOException 当网络连接或读取文件失败时抛出
     */
    public static String ossUrlToStringWithRetry(String ossUrl, int retryTimes) throws IOException {
        IOException lastException = null;

        for (int i = 0; i <= retryTimes; i++) {
            try {
                return ossUrlToString(ossUrl);
            } catch (IOException e) {
                lastException = e;
                if (i < retryTimes) {
                    try {
                        Thread.sleep(1000 * (i + 1)); // 递增延迟重试
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("重试被中断", ie);
                    }
                }
            }
        }

        throw new IOException("经过" + retryTimes + "次重试后仍然失败", lastException);
    }
}
