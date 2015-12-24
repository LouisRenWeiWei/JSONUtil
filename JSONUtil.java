package com.wwren.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Created by Ren Weiwei on 2015/10/21.
 * Email: rww_2010@126.com
 */
public class JSONUtil {
    public static HashMap<String,HashMap<String,Method>> setterCache = new HashMap<>();
    public static HashMap<String,HashMap<String,Method>> getterCache = new HashMap<>();
    public static Object toBean(JSONObject jsonObject,Class clazz){
        Object result = null;
        try {
            result = clazz.newInstance();
            Method[] methods = clazz.getMethods();
            HashMap<String,Method> setters = setterCache.get(clazz.getName());
            if(setters==null){
                setters = new HashMap<>();
                String ss = "set(\\w+)";
                Pattern setM = Pattern.compile(ss);
                // 把方法中的"set" 或者 "get" 去掉
                String rapl = "$1";
                for (Method m :methods){
                    if(Pattern.matches(ss,m.getName())){
                        String param = setM.matcher(m.getName()).replaceAll(rapl);
                        param = param.substring(0,1).toLowerCase()+param.substring(1);
                        setters.put(param,m);
                    }
                }
                setterCache.put(clazz.getName(),setters);
            }
            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()){
                try {
                    String key = iterator.next();
                    Method method = setters.get(key);
                    Class paramType = method.getParameterTypes()[0];
                    if(paramType.getName().equals(Integer.class.getName())){
                        method.invoke(result, jsonObject.getInt(key));
                    }else if(paramType.getName().equals(Long.class.getName())){
                        method.invoke(result,jsonObject.getLong(key));
                    }else if(paramType.getName().equals(Double.class.getName())){
                        method.invoke(result,jsonObject.getDouble(key));
                    }else if(paramType.getName().equals(String.class.getName())){
                        method.invoke(result,jsonObject.getString(key).equalsIgnoreCase("null")?null:jsonObject.getString(key));
                    }else if(paramType.getName().equals(Float.class.getName())){
                        method.invoke(result,jsonObject.getDouble(key));
                    }else if(paramType.getName().equals(Boolean.class.getName())){
                        method.invoke(result,jsonObject.getBoolean(key));
                    }else if(paramType.getName().equals(JSONArray.class.getName())){
                        method.invoke(result,jsonObject.getJSONArray(key));
                    }
                    //暂时不支持list的字段
//                    else if(paramType.getName().equals(List.class.getName())){
//                        method.invoke(result,toBeans(jsonObject.getJSONArray(key),));//jsonObject.getJSONArray(key));
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ArrayList toBeans(JSONArray jsonArray,Class clzz){
        ArrayList result = new ArrayList<>();
        for(int i=0;i<jsonArray.length();i++){
            try {
                result.add(toBean(jsonArray.getJSONObject(i),clzz));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static JSONArray toJSONArray(List data){
        JSONArray result = new JSONArray();
        for(Object obj : data){
            try {
                result.put(toJSONObject(obj));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static JSONObject toJSONObject(Object data){
        JSONObject result = new JSONObject();
        Class clazz = data.getClass();
        HashMap<String,Method> getters = getterCache.get(clazz.getName());
        if(getters==null){
            getters = new HashMap<>();
            Method[] methods = clazz.getMethods();
            String ss = "get(\\w+)";
            Pattern setM = Pattern.compile(ss);
            // 把方法中的"set" 或者 "get" 去掉
            String rapl = "$1";
            for (Method m :methods){
                if(Pattern.matches(ss,m.getName())){
                    String param = setM.matcher(m.getName()).replaceAll(rapl);
                    param = param.substring(0,1).toLowerCase()+param.substring(1);
                    getters.put(param,m);
                }
            }
            setterCache.put(clazz.getName(),getters);
        }
        Field[] fields = clazz.getDeclaredFields();
        for(int i=0;i<fields.length;i++){
            try {
                Field field = fields[i];
                Method method = getters.get(field.getName());
                Object fieldValue = method.invoke(data);
                result.put(field.getName(),fieldValue!=null?fieldValue:"");//防止过滤没有值的字段
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
