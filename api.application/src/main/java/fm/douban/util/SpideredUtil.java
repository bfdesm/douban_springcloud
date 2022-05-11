package fm.douban.util;

import com.alibaba.fastjson.JSON;
import fm.douban.model.Singer;
import fm.douban.model.Song;
import fm.douban.model.Subject;
import fm.douban.model.User;
import fm.douban.service.SingerService;
import fm.douban.service.SongService;
import fm.douban.service.SubjectService;
import fm.douban.service.UserService;
import okhttp3.Request;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SpideredUtil {
    private static final Logger logger = LoggerFactory.getLogger(SpideredUtil.class);

    @DubboReference()
    private SongService songService;

    @DubboReference()
    private SingerService singerService;

    @DubboReference()
    private UserService userService;

    @DubboReference()
    private SubjectService subjectService;

    @Autowired
    private ResourceLoader loader;

    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter df2 = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private static final DateTimeFormatter df3 = DateTimeFormatter.ofPattern("yyyy");

    public void addSingers(List<Map> artists) {
        if (artists == null || artists.isEmpty()) {
            return;
        }

        for (Map artistObj : artists) {
            buildAndSaveSingerBySimilarArtistData(artistObj, new Singer());
        }
    }

    public void saveSong(Song song) {
        try {
            // 没有相同的记录才插入
            Song oldSong = songService.get(song.getId());
            if (oldSong == null) {
                songService.add(song);
            } else {
                songService.modify(song);
            }
        } catch (Exception e) {
            logger.error("operate song error. new song=" + JSON.toJSONString(song), e);
        }
    }

    public void saveSinger(Singer singer) {
        try {
            // 没有相同的记录才插入
            Singer oldSinger = singerService.get(singer.getId());
            if (oldSinger == null) {
                singerService.addSinger(singer);
            } else {
                singerService.modify(singer);
            }
        } catch (Exception e) {
            logger.error("operate singer error. new singer=" + JSON.toJSONString(singer), e);
        }
    }

    public void saveSubject(Subject subject) {
        try {
            // 没有相同的记录才插入
            Subject oldSubject = subjectService.get(subject.getId());
            if (oldSubject == null) {
                subjectService.addSubject(subject);
            }else{
                subjectService.modify(subject);
            }
        } catch (Exception e) {
            logger.error("operate subject error. new subject=" + JSON.toJSONString(subject), e);
        }
    }

    public void saveUser(User user) {
        try {
            // 没有相同的记录才插入
            User oldUser = userService.get(user.getId());
            if (oldUser == null) {
                userService.add(user);
            }else{
                userService.modify(user);
            }
        } catch (Exception e) {
            logger.error("operate subject error. new subject=" + JSON.toJSONString(user), e);
        }
    }

    public Subject buildSubject(Map sourceData,Subject subject) {
        if(sourceDataIsNull(sourceData)){
            logger.info("sourceDataIsNull");
            return subject;
        }
        if(sourceData.get("id") != null)
            subject.setId(sourceData.get("id").toString());
        else
            return subject;
        subject.setCover((String) sourceData.get("cover"));
        if(sourceData.get("description") != null){
            subject.setDescription(String.valueOf(sourceData.get("description")));
        }else if(sourceData.get("intro") != null){
            subject.setDescription((String) sourceData.get("intro"));
        }

        if (sourceData.get("title") != null) {
            subject.setName((String) sourceData.get("title"));
        } else if (sourceData.get("name") != null) {
            subject.setName((String) sourceData.get("name"));
        } else {
            subject.setName("");
        }
        if (sourceData.get("created_time") != null) {
            subject.setPublishedDate(LocalDate.parse(sourceData.get("created_time").toString(), df));
        } else {
            subject.setPublishedDate(LocalDate.now());
        }

        if (SubjectUtil.TYPE_SUB_ARTIST.equals(subject.getSubjectSubType()) && sourceData.get("artist_id") != null) {
            subject.setMaster(sourceData.get("artist_id").toString());
        } else if (SubjectUtil.TYPE_COLLECTION.equals(subject.getSubjectType()) && sourceData.get("creator") != null) {
            Map creator = (Map) sourceData.get("creator");
            if (creator != null && creator.get("id") != null) {
                subject.setMaster(creator.get("id").toString());
            }
        }
        if(sourceData.get("liked_count")!=null){
            subject.setLiked_count(Long.valueOf(sourceData.get("liked_count").toString()));
        }

        List<String> songIds = new ArrayList<>();
        List<Map> songsData = (List<Map>) sourceData.get("songs");
        if (songsData != null) {
            for (Map songData : songsData) {
                Song song = new Song();
                buildSong(songData, song);
                songIds.add(song.getId());
            }
            subject.setSongIds(songIds);
        }
        return subject;
    }

    public Singer buildSinger(Map source, Singer singer) {
        if(!sourceDataIsNull(source) && source.get("id") != null){
            singer.setId(source.get("id").toString());
        }
        singer.setName(source.get("name") == null ? null : source.get("name").toString());
        singer.setHomepage("/artist?singerId="+singer.getId());
        if (source.get("cover") != null && StringUtils.hasText(source.get("cover").toString())) {
            singer.setAvatar(source.get("cover").toString());
        } else if (source.get("picture") != null && StringUtils.hasText(source.get("picture").toString())) {
            singer.setAvatar(source.get("picture").toString());
        } else if (source.get("avatar") != null && StringUtils.hasText(source.get("avatar").toString())) {
            singer.setAvatar(source.get("avatar").toString());
        }
        if(source.get("like_count") != null)
            singer.setLike(Long.valueOf(source.get("like_count").toString()));

        if (source.get("create_time") != null && StringUtils.hasText(source.get("create_time").toString())) {
            LocalDate ld = LocalDate.parse(source.get("create_time").toString(), df2);
            LocalTime lt = LocalTime.of(0, 0, 0);
            singer.setGmtCreated(LocalDateTime.of(ld, lt));
            singer.setGmtModified(singer.getGmtCreated());
        }

        if (source.get("url") != null && StringUtils.hasText(source.get("url").toString())) {
            singer.setHomepage(source.get("url").toString());
        }
        return singer;
    }

    public Song buildSong(Map source, Song song) {
        if(!sourceDataIsNull(source) && source.get("sid") != null && StringUtils.hasText(source.get("sid").toString())){
            song.setId("" + source.get("sid"));
            if (source.get("url") != null && StringUtils.hasText(source.get("url").toString()))
                song.setUrl("" + source.get("url"));
            if (source.get("cover") != null && StringUtils.hasText(source.get("cover").toString()))
                song.setCover("" + source.get("picture"));
            if (source.get("title") != null && StringUtils.hasText(source.get("title").toString()))
                song.setName("" + source.get("title"));
            if (source.get("public_time") != null && StringUtils.hasText(source.get("public_time").toString()))
                song.setPublishedDate((String) source.get("public_time"));
            if (source.get("like") != null && StringUtils.hasText(source.get("like").toString()))
                song.setLike(Long.valueOf(source.get("like").toString()));
            List<String> singerIds = new ArrayList<>();
            List<Map> singerSources = (List<Map>) source.get("singers");
            if(singerSources!=null && singerSources.size() != 0) {
                for (Map singerObj : singerSources) {
                    Singer singer = new Singer();
                    buildSinger(singerObj, singer);
                    singerIds.add(singer.getId());
                    saveSinger(singer);
                }
                song.setSingerIds(singerIds);
            }
        }
        return song;
    }

    public User buildUser(Map source, User user) {
        if(sourceDataIsNull(source))
            return user;
        if (source != null && source.get("id") != null && StringUtils.hasText(source.get("id").toString())) {
            user.setUrl("/user?userId="+source.get("id"));
            user.setId(""+source.get("id"));
        }
        if (source.get("picture") != null && StringUtils.hasText(source.get("picture").toString()))
            user.setLogo(""+ source.get("picture"));
        if (source.get("name") != null && StringUtils.hasText(source.get("name").toString()))
            user.setLoginName(""+source.get("name"));
        saveUser(user);
        return user;
    }

    public User buildCreator(Map sourceData, User user) {
        Map creator = (Map) sourceData.get("creator");
        return buildUser(creator, user);
    }

    public List<String> getSongListIds(Map sourceData, List<String> songIds) {
        if (sourceDataIsNull(sourceData))
            return songIds;
        List<Map> songsData = (List<Map>) sourceData.get("songs");
        if (sourceDataIsNull(songsData))
            return songIds;
        for (Map songObj : songsData) {
            Song song = new Song();
            buildSong(songObj, song);
            saveSong(song);
            songIds.add(song.getId());
        }
        return songIds;
    }

    public Subject getRelatedChannel(Map sourceData, Subject subject) {
        if (sourceDataIsNull(sourceData)) {
            return subject;
        }
        buildSubject(sourceData, subject);
        return subject;
    }

    public List<String> getRelatedChannelSimilarArtistsId(Map sourceData, List<String> singerIds) {
        if (sourceDataIsNull(sourceData))
            return singerIds;
        List<Map> similarArtistsData = (List<Map>) sourceData.get("similar_artists");
        if (!sourceDataIsNull(similarArtistsData))
            for (Map sArtistObj : similarArtistsData) {
                Singer singer = new Singer();
                buildSinger(sArtistObj, singer);
                saveSinger(singer);
                singerIds.add(singer.getId());
            }
        return singerIds;
    }

    public void buildAndSaveSingerBySimilarArtistsData(List<Map> similarArtistsData){
        if (!sourceDataIsNull(similarArtistsData))
            for (Map sArtistObj : similarArtistsData) {
                Singer singer = new Singer();
                buildSinger(sArtistObj, singer);
                saveSinger(singer);
            }
    }

    public boolean sourceDataIsNull(Map sourceData) {
        if (sourceData == null || sourceData.isEmpty())
            return true;
        return false;
    }

    public boolean sourceDataIsNull(List sourceData) {
        if (sourceData == null || sourceData.isEmpty())
            return true;
        return false;
    }

    public Song buildAndSaveSongBySongData(Map songData, Song song){
        buildSong(songData, song);
        saveSong(song);
        return song;
    }

    public List<Song> getSongListSongs(Map sourceData, List<Song> songs) {
        if (sourceDataIsNull(sourceData))
            return songs;
        List<Map> songsData = (List<Map>) sourceData.get("songs");
        if (sourceDataIsNull(songsData))
            return songs;
        for (Map songObj : songsData) {
            Song song = new Song();
            buildAndSaveSongBySongData(songObj, song);
            songs.add(song);
        }
        return songs;
    }

    public Singer buildAndSaveSingerBySimilarArtistData(Map similarArtistData, Singer singer){
        buildSinger(similarArtistData, singer);
        saveSinger(singer);
        return singer;
    }

    public List<Singer> getRelatedChannelSimilarArtists(Map sourceData, List<Singer> singers) {
        if (sourceDataIsNull(sourceData)) {
            return singers;
        }
        List<Map> similarArtistsData = (List<Map>) sourceData.get("similar_artists");
        if (similarArtistsData == null || similarArtistsData.isEmpty()) {
            return singers;
        }

        for (Map sArtistObj : similarArtistsData) {
            Singer singer = new Singer();
            buildAndSaveSingerBySimilarArtistData(sArtistObj, singer);
            singers.add(singer);
        }
        return singers;
    }

    public List<Subject> channel_info(String sourceData, List<Subject> subjects) {
        if (!StringUtils.hasText(sourceData)) {
            return subjects;
        }
        Map dataObj = null;
        try {
            dataObj = JSON.parseObject(sourceData, Map.class);
        } catch (Exception e) {
            // 抛异常表示返回的内容不正确，不是正常的 json 格式，可能是网络或服务器出错了。
            logger.error("parse content to map error. ", e);
        }
        // 可能格式错误
        if (sourceDataIsNull(dataObj))
            return subjects;
        Map data = (Map) dataObj.get("data");
        List<Map> channelsData = (List<Map>) data.get("channels");
        if (channelsData == null || channelsData.isEmpty()) {
            return subjects;
        }
        for (Map channel : channelsData) {
            Subject subject = new Subject();
            buildSubject(channel,subject);
            subject.setSubjectType(SubjectUtil.TYPE_MHZ);
            subjects.add(subject);
        }
        return subjects;
    }

    public List<String> getRelatedSingersId(Map sourceData, List<String> similarIds) {
        if (sourceDataIsNull(sourceData)) {
            return similarIds;
        }

        List<Map> similarArtistsData = (List<Map>) sourceData.get("similar_artists");
        if (sourceDataIsNull(similarArtistsData))
            return similarIds;

        for (Map sArtistObj : similarArtistsData) {
            Singer singer = new Singer();
            buildAndSaveSingerBySimilarArtistData(sArtistObj, singer);
            similarIds.add(singer.getId());
        }
        return similarIds;
    }



    private static void buildHeader(Request.Builder builder, Map<String, String> headers) {

    }
}
