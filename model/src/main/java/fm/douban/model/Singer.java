package fm.douban.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Singer implements Serializable {
    // 主键
    private String id;

    private boolean beSpidered = false;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtCreated;

    /**
     * 修改时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gmtModified;

    // 名称
    private String name;

    // 头像
    private String avatar;

    // 主页
    private String homepage;

    // 相似的歌手 id
    private List<String> similarSingerIds;

    private List<String> songsId;

    private Long like;

    public boolean equals(Object obj) {
        Singer u = (Singer) obj;
        return name.equals(u.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isBeSpidered() {
        return beSpidered;
    }

    public void setBeSpidered(boolean beSpidered) {
        this.beSpidered = beSpidered;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public List<String> getSimilarSingerIds() {
        return similarSingerIds;
    }

    public void setSimilarSingerIds(List<String> similarSingerIds) {
        this.similarSingerIds = similarSingerIds;
    }

    public List<String> getSongsId() {
        return songsId;
    }

    public void setSongsId(List<String> songsId) {
        this.songsId = songsId;
    }

    public Long getLike() {
        return like;
    }

    public void setLike(Long like) {
        this.like = like;
    }

    public boolean equals(Singer singer) {
        if (getAvatar() != null && singer.getAvatar() != null && !getAvatar().equals(singer.getAvatar()))
            if (getName() != null && singer.getName() != null && !getName().equals(singer.getName()))
                if (getHomepage() != null && singer.getHomepage() != null && !getHomepage().equals(singer.getHomepage()))
                    if (getSimilarSingerIds() != null && singer.getSimilarSingerIds() != null && !getSimilarSingerIds().equals(singer.getSimilarSingerIds()))
                        if (isBeSpidered() != singer.isBeSpidered())
                            return true;
        return false;
    }

    public boolean haveNull() {
        if (getName() == null)
            return true;
        if (getAvatar() == null)
            return true;
        if (getHomepage() == null)
            return true;
        if (getSimilarSingerIds().size() == 0)
            return true;
        return false;
    }
}
