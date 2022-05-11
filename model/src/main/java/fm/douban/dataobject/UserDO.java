package fm.douban.dataobject;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import fm.douban.model.User;

import java.io.Serializable;
import java.time.LocalDateTime;

public class UserDO implements Serializable {

    private String id;

    private String loginName;

    private String name;

    @JsonSerialize(using = NullSerializer.class)
    private String password;

    private String mobile;

    private String url;

    private String logo;

    private String email;

    private LocalDateTime gmtCreated;

    private LocalDateTime gmtModified;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String pwd) {
        this.password = pwd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(LocalDateTime gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    public LocalDateTime getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(LocalDateTime gmtModified) {
        this.gmtModified = gmtModified;
    }

    /**
     * DO 转换为 Model
     *
     * @return
     */
    public User toModel() {
        User user = new User();
        user.setId(getId());
        user.setName(getName());
        user.setLoginName(getLoginName());
        user.setLogo(getLogo());

        return user;
    }
}