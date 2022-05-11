package fm.douban.app.api;

import fm.douban.model.Result;
import fm.douban.service.SendMailService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @author zeyu
 * 发送验证码到邮箱
 */
@Controller
public class SMSCodeApi {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisTemplate redisTemplate;
    @DubboReference()
    private SendMailService sendMailService;

    @PostConstruct
    public void init() {
        logger.info("SMSCodeApi 启动啦");
    }

    @GetMapping("/code")
    @ResponseBody
    public Result send(@RequestParam("mail") String mail) {
        Result result = new Result<>();
        result.setSuccess(true);

        if (StringUtils.isEmpty(mail)) {
            result.setSuccess(false);
            result.setMessage("邮件信息不能为空！");
            return result;
        }

        // 将对应验证码存储到redis里
        //每次先去redis校验
        Object code = redisTemplate.opsForValue().get(mail);
        //当redis没有数据的时候再去数据库查询
        if (code == null) {
            //redis缓存没有，为了安全起见
            double code1 = (Math.random() * 9 + 1) * 10000;
            Boolean sendResult = sendMailService.sendMail(mail, (int)code1+"");
            if(!sendResult) {
                result.setSuccess(false);
                result.setMessage("发送验证码失败！");
                return result;
            }
            redisTemplate.opsForValue().set(mail, (int)code1, 5, TimeUnit.MINUTES);
        }
        result.setSuccess(false);
        result.setMessage("请等待1min，验证码有效时间为5min，请注意您的邮箱");
        return result;
    }

    @GetMapping("/verificate")
    @ResponseBody
    public Result verificate(@RequestParam("mail") String mail, @RequestParam("code") int code) {
        Result result = new Result();
        result.setSuccess(true);

        if (StringUtils.isEmpty(mail) || StringUtils.isEmpty(code)) {
            result.setSuccess(false);
            result.setMessage("邮箱和验证码不能为空");
            return result;
        }
        Object existCode = redisTemplate.opsForValue().get(mail);

        // 取出的验证码不能为空
        if (existCode == null) {
            result.setSuccess(false);
            result.setMessage("没有发现对应验证码，请重新申请");
            return result;
        }

        // 校验取出的验证码与参数码是否一致
        if ((int)existCode != code) {
            result.setSuccess(false);
            result.setMessage("验证码校验失败！");
            return result;
        }
        result.setMessage("验证码校验成功！");
        return result;
    }

}
