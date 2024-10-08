package com.liu.myblog.util;

import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;


/**
 * @author 22449
 */
public class JackSonUtil {


    public static String encode(Object object) {
        ObjectMapper mapper = new ObjectMapper().setVisibility(
                JsonMethod.FIELD, Visibility.ANY);
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }


    public static <T> T parse(Class<T> clazz, String json) {
        ObjectMapper mapper = new ObjectMapper().setVisibility(
                JsonMethod.FIELD, Visibility.ANY);
        try {
            mapper.configure(
                    DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                    true);
            mapper.configure(
                    DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false);
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
           return null;
        }
    }


}
