package com.tao.aop;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import com.tao.anno.RepeatSubmit;
import com.tao.exception.BusinessException;
import com.tao.utils.Result;
import com.tao.utils.ServletUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author tao
 */
@Aspect
@Component
public class RepeatSubmitAspect {

    private static final ThreadLocal<String> KEY_CACHE = new ThreadLocal<>();

    @Resource
    RedissonClient redissonClient;

    /**
     * 进入方法前，判断是否重复提交
     */
    @Before("@annotation(repeatSubmit)")
    public void doBefore(JoinPoint point, RepeatSubmit repeatSubmit) throws BusinessException {

        long interval = 0;
        if (repeatSubmit.interval() > 0) {
            interval = repeatSubmit.timeUnit().toMillis(repeatSubmit.interval());
        }

        // 重复提交间隔时间应该大于1秒
        if (interval < 1000) {
            throw BusinessException.createException("500", "重复提交时间不小于1秒");
        }

        HttpServletRequest request = ServletUtils.getRequest();
        String nowParams = argsArrayToString(point.getArgs());

        // 请求地址
        String url = request.getRequestURI();
        // 唯一值
        String submitKey = request.getHeader("Token");

        submitKey = SecureUtil.md5(submitKey + ":" + nowParams);
        String cacheRepeatKey = "repeatSubmitKey:" + url + ":" + submitKey;

        RBucket<Object> bucket = redissonClient.getBucket(cacheRepeatKey);
        String key = (String) bucket.get();
        // 使用setIfAbsent 保证多个线程之间同步
        if (key == null && bucket.setIfAbsent("", Duration.ofMillis(interval))) {
            KEY_CACHE.set(cacheRepeatKey);
        } else {
            String message = repeatSubmit.message();
            throw BusinessException.createException("400", message);
        }
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(repeatSubmit)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, RepeatSubmit repeatSubmit, Object jsonResult) {
        if (jsonResult instanceof Result) {
            try {
                Result<?> r = (Result<?>) jsonResult;
                // 成功则不删除redis数据 保证在有效时间内无法重复提交
                if (Objects.equals(r.getCode(), "200")) {
                    return;
                }
                // 使用ThreadLocal，为了在同一线程中，方便获取redis key，如不使用ThreadLocal，这里还需要重新构造一次key值
                redissonClient.getBucket(KEY_CACHE.get()).delete();
            } finally {
                // 将ThreadLocal进行移除，防止内存泄露
                KEY_CACHE.remove();
            }
        }
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "@annotation(repeatSubmit)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, RepeatSubmit repeatSubmit, Exception e) {
        redissonClient.getBucket(KEY_CACHE.get()).delete();
        KEY_CACHE.remove();
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null) {
            for (Object o : paramsArray) {
                if (ObjectUtil.isNotNull(o) && !isFilterObject(o)) {
                    try {
                        params.append(JSONUtil.toJsonStr(o)).append(" ");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return params.toString().trim();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.entrySet()) {
                Map.Entry entry = (Map.Entry) value;
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }


}
