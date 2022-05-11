package fm.douban.spider;

import com.alibaba.fastjson.JSON;
import fm.douban.model.Song;
import fm.douban.model.Subject;
import fm.douban.model.User;
import fm.douban.service.SubjectService;
import fm.douban.util.HttpUtil;
import fm.douban.util.IsNullUtil;
import fm.douban.util.SpideredUtil;
import fm.douban.util.SubjectUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SubjectSpider {
    private static final Logger logger = LoggerFactory.getLogger(SubjectSpider.class);

    @Autowired
    private HttpUtil httpUtil;

    @DubboReference()
    private SubjectService subjectService;

    @Autowired
    private SpideredUtil spideredUtil;

    private static final String MHZ_REFERER = "https://fm.douban.com/";
    private static final String HOST = "fm.douban.com";
    private static final String COOKIE =
            "viewed=\"26259017\"; bid=Ixdu8ZtzEXw; gr_user_id=68d4e16d-24f6-45a1-bad5-e354cbb2dd56; __utma=30149280.1510717963.1638194508.1638194508.1638194508.1; __utmz=30149280.1638194508.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); _pk_ref.100001.f71f=%5B%22%22%2C%22%22%2C1642772724%2C%22https%3A%2F%2Fwww.baidu.com%2Fs%3Fie%3Dutf-8%26f%3D8%26rsv_bp%3D1%26ch%3D%26tn%3Dbaidu%26bar%3D%26wd%3D%25E8%25B1%2586%25E7%2593%25A3FM%26oq%3D%2525E8%2525B1%252586%2525E7%252593%2525A3%26rsv_pq%3Df6837070000030a9%26rsv_t%3D120bl0XlmRxmiiPJ5LNEasCYTUA1uuAvMsQlYVXaFCFAgnog74w3NrEvAZc%26rqlang%3Dcn%26rsv_enter%3D1%26rsv_dl%3Dtb%26rsv_sug3%3D3%26rsv_sug1%3D3%26rsv_sug7%3D100%22%5D; _ga=GA1.2.1510717963.1638194508; _gid=GA1.2.82405320.1642772725; _pk_id.100001.f71f=97dee38d0a3f8eaf.1642772724.1.1642773049.1642772724.";
    private static final String SL_REFERER = "https://fm.douban.com/explore/songlists";

    private static final String HOT_MHZ_URL = "https://fm.douban.com/j/v2/rec_channels?specific=all";

    private static final String HOT_COLLECTION_URL =
            "https://fm.douban.com/j/v2/songlist/explore?type=hot&genre=0&limit=20&sample_cnt=5";

    private static final String MHZ_URL = "https://fm.douban.com/j/v2/channel_info?id={0}";

    private static final String CHANNEL_URL = "https://fm.douban.com/j/v2/channel_info?id={0}";

    private static final String SINGER_URL = "https://fm.douban.com/j/v2/artist/{0}/";

    private static final String PLAY_SUBJECT_SONGS_URL =
            "https://fm.douban.com/j/v2/playlist?channel={0}&kbps=128&client=s%3Amainsite%7Cy%3A3.0&app_name=radio_website&version=100&type=n";

    private static final String COLLECTION_URL = "https://fm.douban.com/j/v2/songlist/{0}/";

    //@PostConstruct
    public void init() {
        MinuteTask minuteTask = new MinuteTask("MinuteSubjectDataSpiderTask");
        minuteTask.setLoopMinute(minuteTask.getLoopMinute() * 3);
        minuteTask.setWaitMinute(5000);
        Thread thread1 = new Thread(minuteTask);
        thread1.start();


        HourTask hourTask = new HourTask("HourSubjectDataSpiderTask");
        hourTask.setLoopHour(hourTask.getLoopHour() * 24);
        hourTask.setWaitHour(15000);
        Thread thread2 = new Thread(hourTask);
        thread2.start();


//        HourTask hourTask1 = new HourTask("spideRandomrSpideredMHzToUpdateTask");
//        hourTask1.setLoopHour(hourTask1.getLoopHour() * 1);
//        hourTask1.setWaitHour(40000);
//        Thread thread3 = new Thread(hourTask1);
//        thread3.start();


//        HourTask hourTask2 = new HourTask("SpiderAllCollectionToUpdateTask");
//        hourTask2.setLoopHour(hourTask2.getLoopHour() * 24);
//        hourTask2.setWaitHour(50000);
//        Thread thread4 = new Thread(hourTask2);
//        thread4.start();
    }

    @PostConstruct
    public void doExcute() {

    }

    public void doTreadTask(String taskName) {
        switch (taskName) {
            case "MinuteSubjectDataSpiderTask":
                doPrimarySprider();
                break;
            case "HourSubjectDataSpiderTask":
                doHourSprider();
                break;
            case "DaySubjectDataSpiderTask":
                doDaySprider();
                break;
            case "spiderAllMHzToUpdateTask":
                spiderAllMHzToUpdate();
                break;
            case "SpiderAllCollectionToUpdateTask":
                spiderAllCollectionToUpdate();
                break;
            case "spiderRandomSpideredMHzToUpdateTask":
                spiderRandomSpideredMHzToUpdate();
                break;
            default:
                break;
        }
    }

    public void spiderAllMHzToUpdate() {
        logger.info("SpiderAllMHzToUpdate start");
        Subject subject = new Subject();
        try {
            List<Subject> subjects = subjectService.getSubjects(SubjectUtil.TYPE_MHZ);
            logger.info("we need spider " + subjects.size() + " subject");
            for (int i = 0; i < subjects.size(); i++) {
                subject = subjects.get(i);
                if(subject.getSubjectSubType().equals(SubjectUtil.TYPE_SUB_ARTIST)) {
                    if (subject.getMaster() != null & !subject.getMaster().equals("")) {
                        Subject subject1 = getMHzArtistBysingerId(subject.getMaster(), subject);
                        subject1.setSubjectSubType(SubjectUtil.TYPE_SUB_ARTIST);
                        subject1.setId(subject.getId());
                        subjectService.modify(subject1);
                    }
                }else{
                    Subject subject1 = getMHzBySubjectId(subject.getId(), subject);
                    subjectService.modify(subject1);
                }
            }
        } catch (Exception e) {
            logger.error("SpiderAllMHzToUpdate is error");
            JSON.toJSONString(subject);
            logger.info(String.valueOf(e));
        }
        logger.info("SpiderAllMHzToUpdate end");
    }

    public void spiderAllCollectionToUpdate() {
        logger.info("spiderAllCollectionToUpdate start");
        try {
            List<Subject> subjects = subjectService.getSubjects(SubjectUtil.TYPE_COLLECTION);
            for (Subject s : subjects)
                getCollectionByColletionId(s.getId(), s);
        } catch (Exception e) {
            logger.info("spiderAllCollectionToUpdate is error");
            logger.info(String.valueOf(e));
        }
        logger.info("spiderAllCollectionToUpdate end");
    }

    public void spiderRandomSpideredMHzToUpdate(){

    }

    public void doPrimarySprider() {
        getHotArtistData();
        getRandomCollectionData();
    }

    public void doHourSprider() {
        getHotSubjectData();
        getHotCollectionsData();
    }

    public void doDaySprider() {

    }

    private void getPlaySubjectSongsData(Subject subject) {
        String subjectId = subject.getId();
        String songDataUrl = MessageFormat.format(PLAY_SUBJECT_SONGS_URL, subjectId);

        Map<String, String> headerData = httpUtil.buildHeaderData(MHZ_REFERER, HOST, COOKIE);
        String content = httpUtil.getContent(songDataUrl, headerData);

        if (!StringUtils.hasText(content)) {
            return;
        }

        Map dataObj = null;

        try {
            dataObj = JSON.parseObject(content, Map.class);
        } catch (Exception e) {
            // 抛异常表示返回的内容不正确，不是正常的 json 格式，可能是网络或服务器出错了。
            logger.error("parse content to map error. ");
        }

        // 可能格式错误
        if (dataObj == null) {
            return;
        }

        List<Map> songsData = (List<Map>) dataObj.get("song");

        if (songsData == null || songsData.isEmpty()) {
            return;
        }
        List<String> songIdList = new ArrayList<>();
        for (Map songData : songsData) {
            Song song = new Song();
            spideredUtil.buildSong(songData, song);
            spideredUtil.saveSong(song);
            songIdList.add(song.getId());
        }

        // 有元素则进行修改
        if (!songIdList.isEmpty()) {
            subject.setSongIds(songIdList);
            subjectService.modify(subject);
        }
    }

    private void addMHZSubject(List<Map> channels, String subjectSubType) {
        if (IsNullUtil.isNull(channels))
            return;
        for (Map channelObj : channels) {
            Subject subject = new Subject();
            spideredUtil.buildSubject(channelObj, subject);
            subject.setSubjectType(SubjectUtil.TYPE_MHZ);
            subject.setSubjectSubType(subjectSubType);
            if (SubjectUtil.TYPE_SUB_ARTIST.equals(subjectSubType)) {
                // 记录关联的歌手
                List relatedArtists = (List) channelObj.get("related_artists");
                spideredUtil.addSingers(relatedArtists);
            }
            // 保存MHZ数据
            spideredUtil.saveSubject(subject);
            getPlaySubjectSongsData(subject);
        }
    }

    private void getHotSubjectData() {
        Map<String, String> headerData = httpUtil.buildHeaderData(MHZ_REFERER, HOST, COOKIE);
        String content = httpUtil.getContent(HOT_MHZ_URL, headerData);

        if (!StringUtils.hasText(content)) {
            return;
        }

        Map dataObj = null;

        try {
            dataObj = JSON.parseObject(content, Map.class);
        } catch (Exception e) {
            // 抛异常表示返回的内容不正确，不是正常的 json 格式，可能是网络或服务器出错了。
            logger.error("parse content to map error. ", e);
        }

        // 可能格式错误
        if (dataObj == null) {
            return;
        }

        Map subDataObj = (Map) dataObj.get("data");

        Map channelsObj = null;
        if (subDataObj != null && !subDataObj.isEmpty()) {
            channelsObj = (Map) subDataObj.get("channels");
        }

        if (channelsObj == null) {
            return;
        }

        // 语言/年代 数据
        List languages = (List) channelsObj.get("language");

        // 风格/流派 数据
        List genres = (List) channelsObj.get("genre");

        // 心情/场景 数据
        List scenarios = (List) channelsObj.get("scenario");

        addMHZSubject(languages, SubjectUtil.TYPE_SUB_AGE);
        addMHZSubject(genres, SubjectUtil.TYPE_SUB_STYLE);
        addMHZSubject(scenarios, SubjectUtil.TYPE_SUB_MOOD);
    }

    private void getHotCollectionsData() {
        // 替换为自己使用浏览器开发者工具观察到的值
        Map<String, String> headerData = httpUtil.buildHeaderData(SL_REFERER, HOST, COOKIE);
        String content = httpUtil.getContent(HOT_COLLECTION_URL, headerData);

        if (!StringUtils.hasText(content)) {
            return;
        }

        List<Map> dataList = null;

        try {
            dataList = JSON.parseObject(content, List.class);
        } catch (Exception e) {
            // 抛异常表示返回的内容不正确，不是正常的 json 格式，可能是网络或服务器出错了。
            logger.error("parse content to map error. ", e);
        }

        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        for (Map slObj : dataList) {
            Subject collection = new Subject();
            spideredUtil.buildSubject(slObj, collection);
            collection.setSubjectType(SubjectUtil.TYPE_COLLECTION);
            // 处理歌单的作者，存为 user
            Map creatorData = (Map) slObj.get("creator");

            if (creatorData != null && creatorData.get("id") != null && StringUtils.hasText(creatorData.get("id").toString())) {
                User user = new User();
                spideredUtil.buildUser(creatorData, user);
                spideredUtil.saveUser(user);
            }
            // 保存 歌单 数据
            spideredUtil.saveSubject(collection);
        }
    }

    private void getHotArtistData() {
        Map<String, String> headerData = httpUtil.buildHeaderData(MHZ_REFERER, HOST, COOKIE);
        String content = httpUtil.getContent(HOT_MHZ_URL, headerData);

        if (!StringUtils.hasText(content)) {
            return;
        }

        Map dataObj = null;

        try {
            dataObj = JSON.parseObject(content, Map.class);
        } catch (Exception e) {
            // 抛异常表示返回的内容不正确，不是正常的 json 格式，可能是网络或服务器出错了。
            logger.error("parse content to map error. ", e);
        }

        // 可能格式错误
        if (dataObj == null) {
            return;
        }

        Map subDataObj = (Map) dataObj.get("data");

        Map channelsObj = null;
        if (subDataObj != null && !subDataObj.isEmpty()) {
            channelsObj = (Map) subDataObj.get("channels");
        }

        if (channelsObj == null) {
            return;
        }

        // 从艺术家出发 数据
        List artists = (List) channelsObj.get("artist");

        addMHZSubject(artists, SubjectUtil.TYPE_SUB_ARTIST);
    }

    private void getRandomCollectionData() {
        List<Subject> colletions = subjectService.getRandomSubject(SubjectUtil.TYPE_COLLECTION, null, 5);
        for (Subject colletion : colletions)
            getCollectionByColletionId(colletion.getId(),colletion);
    }

    public Subject getCollectionByColletionId(String collectionId, Subject collection) {
        String colletionUrl = MessageFormat.format(COLLECTION_URL, collectionId);
        Map<String, String> headerData = httpUtil.buildHeaderData(SL_REFERER, HOST, COOKIE);
        String content = httpUtil.getContent(colletionUrl, headerData);
        if (!StringUtils.hasText(content)) {
            return collection;
        }
        Map dataMap = null;
        try {
            dataMap = JSON.parseObject(content, Map.class);
        } catch (Exception e) {
            // 抛异常表示返回的内容不正确，不是正常的 json 格式，可能是网络或服务器出错了。
            logger.error("parse content to map error. ", e);
        }
        if (IsNullUtil.isNull(dataMap))
            return collection;
        User user = new User();
        spideredUtil.buildCreator(dataMap,user);
        spideredUtil.buildSubject(dataMap,collection);
        collection.setSubjectType(SubjectUtil.TYPE_COLLECTION);
        // 保存 歌单 数据
        spideredUtil.saveSubject(collection);
        return collection;
    }

    public Subject getMHzArtistBysingerId(String singerId, Subject subject) {
        String url = MessageFormat.format(SINGER_URL, singerId);
        Map<String, String> headerData = httpUtil.buildHeaderData(MHZ_REFERER, HOST, COOKIE);
        String content = httpUtil.getContent(url, headerData);
        if (!StringUtils.hasText(content)) {
            return subject;
        }

        Map dataObj = null;

        try {
            dataObj = JSON.parseObject(content, Map.class);
        } catch (Exception e) {
            // 抛异常表示返回的内容不正确，不是正常的 json 格式，可能是网络或服务器出错了。
            logger.error("parse content to map error. ", e);
        }

        // 可能格式错误
        if (spideredUtil.sourceDataIsNull(dataObj))
            return subject;

        Map related_channel_Data = (Map) dataObj.get("related_channel");
        Map songlist_Data = (Map) dataObj.get("songlist");
        spideredUtil.getSongListSongs(songlist_Data, new ArrayList<>());
        subject.setSubjectType(SubjectUtil.TYPE_MHZ);
        subject.setSubjectSubType(SubjectUtil.TYPE_SUB_ARTIST);
        spideredUtil.getRelatedChannel(related_channel_Data, subject);
        spideredUtil.saveSubject(subject);
        return subject;
    }

    public Subject getMHzBySubjectId(String subjectId, Subject subject) {
        return getMhzByUrl(MessageFormat.format(MHZ_URL, subjectId), subject);
    }

    public Subject getMhzByUrl(String url, Subject subject) {
        Map<String, String> headerData = httpUtil.buildHeaderData(MHZ_REFERER, HOST, COOKIE);
        String content = httpUtil.getContent(url, headerData);
        if (!StringUtils.hasText(content)) {
            return subject;
        }

        Map dataObj = null;

        try {
            dataObj = JSON.parseObject(content, Map.class);
        } catch (Exception e) {
            // 抛异常表示返回的内容不正确，不是正常的 json 格式，可能是网络或服务器出错了。
            logger.error("parse content to map error. ", e);
        }

        // 可能格式错误
        if (spideredUtil.sourceDataIsNull(dataObj))
            return subject;

        Map related_channel_Data = (Map) dataObj.get("related_channel");
        Map songlist_Data = (Map) dataObj.get("songlist");
        spideredUtil.getSongListSongs(songlist_Data, new ArrayList<>());
        spideredUtil.getRelatedChannel(related_channel_Data, subject);
        spideredUtil.saveSubject(subject);
        return subject;
    }

    public class MinuteTask implements Runnable {
        private String taskName;

        private int loopMinute = 1000 * 60;

        private int waitMinute = 1000 * 60;

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public int getLoopMinute() {
            return loopMinute;
        }

        public void setLoopMinute(int loopMinute) {
            this.loopMinute = loopMinute;
        }

        public int getWaitMinute() {
            return waitMinute;
        }

        public void setWaitMinute(int waitMinute) {
            this.waitMinute = waitMinute;
        }

        public MinuteTask(String taskName) {
            this.taskName = taskName;
        }

        public MinuteTask(String taskName, int loopMinute) {
            this.taskName = taskName;
            this.loopMinute = loopMinute;
        }

        public void run() {
            try {
                Thread.sleep(waitMinute);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (true) {
                logger.info(taskName + " start at " + LocalDateTime.now());
                doTreadTask(taskName);
                logger.info(taskName + " end at " + LocalDateTime.now());
                try {
                    Thread.sleep(loopMinute);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class HourTask implements Runnable {
        private String taskName;

        private int loopHour = 1000 * 60 * 60;

        private int waitHour = 1000 * 60 * 60;

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public int getLoopHour() {
            return loopHour;
        }

        public void setLoopHour(int loopHour) {
            this.loopHour = loopHour;
        }

        public int getWaitHour() {
            return waitHour;
        }

        public void setWaitHour(int waitHour) {
            this.waitHour = waitHour;
        }

        public HourTask(String taskName) {
            this.taskName = taskName;
        }

        public HourTask(String taskName, int hour) {
            this.taskName = taskName;
            this.loopHour = hour;
        }

        public void run() {
            try {
                Thread.sleep(waitHour);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (true) {
                logger.info(taskName + " start at " + LocalDateTime.now());
                doTreadTask(taskName);
                logger.info(taskName + " end at " + LocalDateTime.now());
                try {
                    Thread.sleep(loopHour);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    //    public Song getSubjectRandomSongBysubjectId(String subjectId){
//        String songDataUrl = MessageFormat.format(RANDOM_CHANNEL_SONG_URL, subjectId);
//        Map<String, String> headerData = httpUtil.buildHeaderData(MHZ_REFERER, HOST, COOKIE);
//        String content = httpUtil.getContent(songDataUrl, headerData);
//
//        if (!StringUtils.hasText(content)) {
//            return null;
//        }
//
//        Map dataObj = null;
//
//        try {
//            dataObj = JSON.parseObject(content, Map.class);
//        } catch (Exception e) {
//            // 抛异常表示返回的内容不正确，不是正常的 json 格式，可能是网络或服务器出错了。
//            logger.error("parse content to map error. ", e);
//        }
//
//        // 可能格式错误
//        if (dataObj == null) {
//            return null;
//        }
//
//        List<Map> songsData = (List<Map>)dataObj.get("song");
//        if (songsData == null || songsData.isEmpty()) {
//            return null;
//        }
//        Song song = null;
//        for (Map songObj : songsData) {
//            song = buildSong(songObj);
//            saveSong(song);
//        }
//        return song;
//    }
}
