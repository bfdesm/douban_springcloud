package fm.douban.model;

import java.io.Serializable;
import java.util.Date;

public class PageView implements Serializable {
    private String pageName;

    private String userId;

    private String commentId;

    private Date gmtCreated;

    private static final long serialVersionUID = -5563782578272943999L;

    public PageView(String pageName, Date gmtCreated) {
        this.pageName = pageName;
        this.gmtCreated = gmtCreated;
    }

    public PageView(String pageName, String commentId, Date gmtCreated) {
        this.pageName = pageName;
        this.commentId = commentId;
        this.gmtCreated = gmtCreated;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Date gmtCreated) {
        this.gmtCreated = gmtCreated;
    }
}
