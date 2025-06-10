package cn.satoken.util;

import java.util.HashMap;
import java.util.Map;

public class MyContext {
    private static final ThreadLocal<Map<String, Object>> context = ThreadLocal.withInitial(HashMap::new);

    public static void set(String key, Object value) {
        context.get().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) context.get().get(key);
    }

    public static void clear() {
        context.remove(); // 防止内存泄漏
    }
}
