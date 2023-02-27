package ru.netology;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Main {
    public static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build();

        HttpGet request = new HttpGet("https://api.nasa.gov/planetary/apod?api_key=T4N9rHbBjvGT2eduiOOJdSBVITuA94mW8CIN6ABS");
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        CloseableHttpResponse response = httpClient.execute(request);

        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        List<NasaData> nasaList = mapper.readValue(
                response.getEntity()
                        .getContent(),
                new TypeReference<>() {
                });

        nasaList.stream().forEach(System.out::println);

        String targetFile = nasaList.get(0).getUrl();
        int position = targetFile.lastIndexOf('/');
        String fileName = targetFile.substring(position + 1);

        request = new HttpGet(targetFile);
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        response = httpClient.execute(request);

        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            byte[] inStream = response.getEntity().getContent().readAllBytes();
            fos.write(inStream);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        response.close();
        httpClient.close();
    }
}