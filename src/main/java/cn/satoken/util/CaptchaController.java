package cn.satoken.util;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
public class CaptchaController {

    @Autowired
    private Redis redis;  // Redis操作类

    @GetMapping("/getCaptcha")
    public Result getCaptcha() {
        // 1. 生成验证码图片
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 100);

        // 2. 生成唯一验证码ID
        String captchaId = UUID.randomUUID().toString();
        //todo 注释掉
        System.out.printf("""
                "captchaId": "%s",
                "captcha": "%s"
                """, captchaId, captcha.getCode());

        // 3. 把验证码文字存到Redis，5分钟过期 todo改成5分钟
        redis.set("captcha:" + captchaId, captcha.getCode(), 5 * 60 *6000);

        // 4. 把图片转Base64字符串
        String base64Img = Base64.getEncoder().encodeToString(captcha.getImageBytes());

        // 5. 返回给前端
        Map<String, String> result = new HashMap<>();
        result.put("captchaId", captchaId);
        result.put("img", "data:image/png;base64," + base64Img);
        return Result.data(result);
    }

    public void verifyCaptcha(String captchaId, String inputCode) {
        String code = redis.get("captcha:" + captchaId);
        if (ObjectUtil.isEmpty(code)) {
            throw new MyException("验证码已过期");
        }
        //todo打开，把验证码删了
//        redis.delete("captcha:" + captchaId);
        if (!code.equalsIgnoreCase(inputCode)) {
            throw new MyException("验证码错误");
        }
    }
}