/*
 * Copyright 2020-2099 sa-token.cc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.satoken.util;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.util.SaFoxUtil;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 对请求接口返回 Json 格式数据的简易封装。
 *
 * <p>
 * 所有预留字段：<br>
 * code = 状态码 <br>
 * msg  = 描述信息 <br>
 * data = 携带对象 <br>
 * </p>
 *
 * @author click33
 * @since 1.22.0
 */
public class Result extends LinkedHashMap<String, Object> implements Serializable {

    // 预定的状态码
    public static final int CODE_SUCCESS = 200;
    public static final int CODE_ERROR = 500;
    public static final int CODE_NOT_PERMISSION = 403;
    public static final int CODE_NOT_LOGIN = 401;
    // 序列化版本号
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构建
     */
    public Result() {
    }

    /**
     * 构建
     *
     * @param code 状态码
     * @param msg  信息
     * @param data 数据
     */
    public Result(int code, String msg, Object data) {
        this.setCode(code);
        this.setMsg(msg);
        this.setData(data);
    }

    /**
     * 根据 Map 快速构建
     *
     * @param map /
     */
    public Result(Map<String, ?> map) {
        this.setMap(map);
    }

    // 构建成功
    public static Result ok() {
        return new Result(CODE_SUCCESS, "ok", null);
    }

    public static Result ok(String msg) {
        return new Result(CODE_SUCCESS, msg, null);
    }

    public static Result code(int code) {
        return new Result(code, null, null);
    }

    public static Result data(Object data) {
        return new Result(CODE_SUCCESS, "ok", data);
    }

    // 构建失败
    public static Result error() {
        return new Result(CODE_ERROR, "error", null);
    }

    public static Result error(String msg) {
        //如果msg超过50个字符截断
        msg = msg.length() > 100 ? msg.substring(0, 100) + "..." : msg;
        return new Result(CODE_ERROR, msg, null);
    }

    // 构建未登录
    public static Result notLogin() {
        return new Result(CODE_NOT_LOGIN, "not login", null);
    }

    // 构建无权限
    public static Result notPermission() {
        return new Result(CODE_NOT_PERMISSION, "not permission", null);
    }

    // 构建指定状态码
    public static Result get(int code, String msg, Object data) {
        return new Result(code, msg, data);
    }

    // 构建一个空的
    public static Result empty() {
        return new Result();
    }

    /**
     * 获取code
     *
     * @return code
     */
    public Integer getCode() {
        return (Integer) this.get("code");
    }

    /**
     * 给code赋值，连缀风格
     *
     * @param code code
     * @return 对象自身
     */
    public Result setCode(int code) {
        this.put("code", code);
        return this;
    }


    // ============================  静态方法快速构建  ==================================

    /**
     * 获取msg
     *
     * @return msg
     */
    public String getMsg() {
        return (String) this.get("msg");
    }

    /**
     * 给msg赋值，连缀风格
     *
     * @param msg msg
     * @return 对象自身
     */
    public Result setMsg(String msg) {
        this.put("msg", msg);
        return this;
    }

    /**
     * 获取data
     *
     * @return data
     */
    public Object getData() {
        return this.get("data");
    }

    /**
     * 给data赋值，连缀风格
     *
     * @param data data
     * @return 对象自身
     */
    public Result setData(Object data) {
        this.put("data", data);
        return this;
    }

    /**
     * 写入一个值 自定义key, 连缀风格
     *
     * @param key  key
     * @param data data
     * @return 对象自身
     */
    public Result set(String key, Object data) {
        this.put(key, data);
        return this;
    }

    /**
     * 获取一个值 根据自定义key
     *
     * @param <T> 要转换为的类型
     * @param key key
     * @param cs  要转换为的类型
     * @return 值
     */
    public <T> T get(String key, Class<T> cs) {
        return SaFoxUtil.getValueByType(get(key), cs);
    }

    /**
     * 写入一个Map, 连缀风格
     *
     * @param map map
     * @return 对象自身
     */
    public Result setMap(Map<String, ?> map) {
        if (map != null) {
            for (String key : map.keySet()) {
                this.put(key, map.get(key));
            }
        }
        return this;
    }

    /**
     * 写入一个 json 字符串, 连缀风格
     *
     * @param jsonString json 字符串
     * @return 对象自身
     */
    public Result setJsonString(String jsonString) {
        Map<String, Object> map = SaManager.getSaJsonTemplate().jsonToMap(jsonString);
        return setMap(map);
    }

    /**
     * 移除默认属性（code、msg、data）, 连缀风格
     *
     * @return 对象自身
     */
    public Result removeDefaultFields() {
        this.remove("code");
        this.remove("msg");
        this.remove("data");
        return this;
    }

    /**
     * 移除非默认属性（code、msg、data）, 连缀风格
     *
     * @return 对象自身
     */
    public Result removeNonDefaultFields() {
        for (String key : this.keySet()) {
            if ("code".equals(key) || "msg".equals(key) || "data".equals(key)) {
                continue;
            }
            this.remove(key);
        }
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{"
                + "\"code\": " + this.getCode()
                + ", \"msg\": " + transValue(this.getMsg())
                + ", \"data\": " + transValue(this.getData())
                + "}";
    }

    /**
     * 转换 value 值：
     * 如果 value 值属于 String 类型，则在前后补上引号
     * 如果 value 值属于其它类型，则原样返回
     *
     * @param value 具体要操作的值
     * @return 转换后的值
     */
    private String transValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        return String.valueOf(value);
    }

}
