package fm.douban.app.control;

import fm.douban.model.User;
import fm.douban.model.UserLoginInfo;
import fm.douban.param.UserQueryParam;
import fm.douban.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class UserControl {

    private static final Logger LOG = LoggerFactory.getLogger(UserControl.class);

    @DubboReference()
    private UserService userService;

    @Autowired
    private RedissonClient redissonClient;

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @PostConstruct
    public void init() {
        LOG.info("UserControl 启动啦");
        LOG.info("userService 注入啦");
    }

    @GetMapping(path = "/login")
    public String login() {
        return "login";
    }

    @GetMapping(path = "/sign")
    public String sign() {
        return "sign";
    }

    @PostMapping(path = "/register")
    @ResponseBody
    public Map register(@RequestParam String name, @RequestParam String mobile, @RequestParam String password,
                        @RequestParam String password_r, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map returnData = new HashMap();
        if (!password_r.equals(password)) {
            returnData.put("result", false);
            returnData.put("message", "password is not same");
            return returnData;
        }
        // 判断登录名是否已存在
        User regedUser = getUserByLoginName(name);
        if (regedUser != null) {
            returnData.put("result", false);
            returnData.put("message", "login name already exist");
            return returnData;
        }

        //获取当前时间
        String now = LocalDate.now().format(dateTimeFormatter);
        //通过redis的自增获取序号
        RAtomicLong atomicLong = redissonClient.getAtomicLong(now);
        atomicLong.expire(1, TimeUnit.DAYS);
        long id = atomicLong.incrementAndGet();
        User user = new User();
        user.setId(String.valueOf(id));
        user.setLoginName(name);

        // 密码加自定义盐值，确保密码安全
        String saltPwd = password + "password";
        // 生成md5值，并转大写字母
        String md5Pwd = DigestUtils.md5Hex(saltPwd).toUpperCase();

        user.setPassword(md5Pwd);
        user.setMobile(mobile);
        user.setUrl("/user?userId="+id);
        User newUser = userService.add(user);
        if (newUser != null && StringUtils.hasText(newUser.getId())) {
            returnData.put("result", true);
            returnData.put("message", "register successfule");
        } else {
            returnData.put("result", false);
            returnData.put("message", "register failed");
        }
        UserLoginInfo userLoginInfo = new UserLoginInfo();
        userLoginInfo.setUserId(regedUser.getId());
        userLoginInfo.setUserName(name);
        response.sendRedirect("/index");
        request.getSession().setAttribute("userLoginInfo", userLoginInfo);
        return returnData;
    }

    @PostMapping(path = "/authenticate")
    @ResponseBody
    public Map authenticate(@RequestParam String name, @RequestParam String password, HttpServletRequest request,
                     HttpServletResponse response) throws IOException {
        Map returnData = new HashMap();

        User regedUser = getUserByLoginName(name);
        if (regedUser == null) {
            returnData.put("result", false);
            returnData.put("message", "userName not correct");
            return returnData;
        }

        String saltPwd = password + "password";
        String md5Pwd = DigestUtils.md5Hex(saltPwd).toUpperCase();
        if (!md5Pwd.equals(regedUser.getPassword())) {
            returnData.put("result", false);
            returnData.put("message", "password not correct");
            return returnData;
        }

        UserLoginInfo userLoginInfo = new UserLoginInfo();
        userLoginInfo.setUserId(regedUser.getId());
        userLoginInfo.setUserName(name);
        HttpSession session = request.getSession();
        session.setAttribute("userLoginInfo", userLoginInfo);

        returnData.put("result", true);
        returnData.put("message", "login successfule");
        response.sendRedirect("/index");
        return returnData;
    }

    private User getUserByLoginName(String loginName) {
        User regedUser = null;
        UserQueryParam param = new UserQueryParam();
        param.setLoginName(loginName);
        Page<User> users = userService.list(param);

        if (users != null && users.getContent() != null && users.getContent().size() > 0) {
            regedUser = users.getContent().get(0);
        }

        return regedUser;
    }
}
