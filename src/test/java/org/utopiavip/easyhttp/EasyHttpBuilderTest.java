package org.utopiavip.easyhttp;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class EasyHttpBuilderTest {

    private final static String url = "http://localhost:8080/Test/hi/index";

    @Test
    public void postJSON() {

        Map map = new HashMap<String, Object>();
        map.put("orderCode", "170303W2KQ7M");
        map.put("statementCode", "2394810870");

        Response response = EasyHttpBuilder.build()
                .contentType(MediaType.APPLICATION_JSON)
                .post(url, map);
        System.out.println(response.json());
    }

    @Test
    public void postXML() {
        String xml =
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<note>\n" +
                        "\t<to>George</to>\n" +
                        "\t<from>John</from>\n" +
                        "\t<heading>Reminder</heading>\n" +
                        "\t<body>Don't forget the meeting!</body>\n" +
                        "</note>";

        Response response = EasyHttpBuilder.build()
                .contentType(MediaType.APPLICATION_XML)
                .post(url, xml);
        System.out.println(response.json());
    }

    @Test
    public void get() {
        Map map = new HashMap<String, Object>();
        map.put("name", "kitty");
        Response response = EasyHttpBuilder.build().get(url, map);
        System.out.println(response.json());

        response = EasyHttpBuilder.build().get(url);
        System.out.println(response.json());

        response = EasyHttpBuilder.build().get(url + "?name=kitty");
        System.out.println(response.json());
    }

    @Test
    public void delete() {
        Map map = new HashMap<String, Object>();
        map.put("name", "kitty");
        Response response = EasyHttpBuilder.build().delete(url, map);
        System.out.println(response.json());

        response = EasyHttpBuilder.build().delete(url);
        System.out.println(response.json());

        response = EasyHttpBuilder.build().delete(url + "?name=kitty");
        System.out.println(response.json());
    }

    @Test
    public void setRequestConfig() {
        Response response = EasyHttpBuilder.build()
                .setSocketTimeout(10000)
                .get(url);
        System.out.println(response.json());
    }

}
