package fm.douban.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import fm.douban.util.IsNullUtil;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Subject implements Serializable {

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

    // 标题
    private String name;

    // 详细说明
    private String description;

    // 封面图
    private String cover;

    // 对 歌单(collection) 来说，指 作者
    // 对 兆赫(mhz) 来说，指 音乐家
    private String master;

    // 发布时间
    private LocalDate publishedDate;

    // 主题的一级分类。兆赫(mhz)、歌单(collection)
    private String subjectType;

    // 主题的二级分类。兆赫(mhz) 下的细分分类：
    // artist(从艺术家出发)、mood(心情 / 场景)、age(语言 / 年代)、style(风格 / 流派)
    private String subjectSubType;

    // 关联的歌曲列表
    private List<String> songIds;

    private Long liked_count;

    public boolean equals(Object obj) {
        Subject u = (Subject) obj;
        return name.equals(u.name);
    }

    public int hashCode() {
        String in = name;
        return in.hashCode();
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

    public Long getLiked_count() {
        return liked_count;
    }

    public void setLiked_count(Long liked_count) {
        this.liked_count = liked_count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
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

    public List<String> getSongIds() {
        return songIds;
    }

    public void setSongIds(List<String> songIds) {
        this.songIds = songIds;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getSubjectSubType() {
        return subjectSubType;
    }

    public void setSubjectSubType(String subjectSubType) {
        this.subjectSubType = subjectSubType;
    }

    public boolean haveNull() {
        if (IsNullUtil.isNull(master))
            return true;
        if (IsNullUtil.isNull(name))
            return true;
        if (IsNullUtil.isNull(subjectType))
            return true;
        if (IsNullUtil.isNull(subjectSubType))
            return true;
        if (IsNullUtil.isNull(cover))
            return true;
        if (IsNullUtil.isNull(description))
            return true;
        if (IsNullUtil.isNull(songIds))
            return true;
        if (liked_count!=null)
            return true;
        return false;
    }

    public void getAllByNewSubject(Subject newSubject){
        if(newSubject.getId() != null)
            id = newSubject.getId();
        if(newSubject.getSubjectSubType() != null)
            subjectSubType = newSubject.getSubjectSubType();
        if(newSubject.getSubjectType() != null)
            subjectType = newSubject.getSubjectType();
        if(newSubject.getDescription() != null)
            description = newSubject.getDescription();
        if(newSubject.getName() != null)
            name = newSubject.getName();
        if(newSubject.getCover() != null)
            cover = newSubject.getCover();
        if(newSubject.getMaster()!=null)
            master = newSubject.getMaster();
        if(newSubject.getSongIds()!=null)
            songIds = newSubject.getSongIds();
        if(newSubject.getLiked_count()!=null)
            liked_count = newSubject.getLiked_count();
        if(newSubject.getPublishedDate()!=null)
            publishedDate = newSubject.getPublishedDate();
    }
}