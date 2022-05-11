package fm.douban.spider;

import com.alibaba.fastjson.JSON;
import fm.douban.model.Singer;
import fm.douban.service.SingerService;
import fm.douban.service.SongService;
import fm.douban.util.HttpUtil;
import fm.douban.util.SpideredUtil;
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
public class SingerSpider {

    private static final Logger logger = LoggerFactory.getLogger(SingerSpider.class);

    private static final String HOST = "fm.douban.com";
    private static final String REFERER = "https://fm.douban.com/";
    private static final String COOKIE = "viewed=\"26259017\"; bid=Ixdu8ZtzEXw; gr_user_id=68d4e16d-24f6-45a1-bad5-e354cbb2dd56; __utma=30149280.1510717963.1638194508.1638194508.1638194508.1; __utmz=30149280.1638194508.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); _pk_ref.100001.f71f=%5B%22%22%2C%22%22%2C1642772724%2C%22https%3A%2F%2Fwww.baidu.com%2Fs%3Fie%3Dutf-8%26f%3D8%26rsv_bp%3D1%26ch%3D%26tn%3Dbaidu%26bar%3D%26wd%3D%25E8%25B1%2586%25E7%2593%25A3FM%26oq%3D%2525E8%2525B1%252586%2525E7%252593%2525A3%26rsv_pq%3Df6837070000030a9%26rsv_t%3D120bl0XlmRxmiiPJ5LNEasCYTUA1uuAvMsQlYVXaFCFAgnog74w3NrEvAZc%26rqlang%3Dcn%26rsv_enter%3D1%26rsv_dl%3Dtb%26rsv_sug3%3D3%26rsv_sug1%3D3%26rsv_sug7%3D100%22%5D; _ga=GA1.2.1510717963.1638194508; _gid=GA1.2.82405320.1642772725; _pk_id.100001.f71f=97dee38d0a3f8eaf.1642772724.1.1642773049.1642772724.";

    private static final String SINGER_URL = "https://fm.douban.com/j/v2/artist/{0}/";

    @Autowired
    private HttpUtil httpUtil;

    @DubboReference()
    private SingerService singerService;

    @Autowired
    private SpideredUtil spideredUtil;

    @DubboReference()
    private SongService songService;

    //@PostConstruct
    public void init() {
        MinuteTask minuteTask = new MinuteTask("MinuteSingerDataSpiderTask");
        minuteTask.setLoopMinute(minuteTask.getLoopMinute() * 3);
        minuteTask.setWaitMinute(10000);
        Thread thread = new Thread(minuteTask);
        thread.start();


        HourTask hourTask = new HourTask("SpiderAllSingerToUpdateTask");
        hourTask.setLoopHour(hourTask.getLoopHour() * 24 * 30);
        hourTask.setWaitHour(20000);
//      Thread thread2 = new Thread(hourTask);
//      thread2.start();
    }

    @PostConstruct
    public void doExcute() {

    }

    public void doTreadTask(String taskName) {
        switch (taskName) {
            case "MinuteSingerDataSpiderTask":
                doPrimarySprider();
                break;
            case "SpiderAllSingerToUpdateTask":
                spiderAllSingerToUpdate();
                break;
            default:
                break;
        }
    }

    public void doPrimarySprider() {
        updateSingerDatabase();
    }

    public void spiderAllSingerToUpdate() {
        logger.info("SpiderAllSingerToUpdate start");
        try {
            List<Singer> singers = singerService.getAll();
            for (Singer s : singers)
                updateSingerBysingerId(s.getId(), s);
        } catch (Exception e) {
            logger.info("spiderAllSinger is error");
            logger.info(String.valueOf(e));
        }
        logger.info("SpiderAllSingerToUpdate end");
    }

    public Singer updateSingerBysingerId(String singerId, Singer singer) {
        singer.setId(singerId);
        String url = MessageFormat.format(SINGER_URL, singerId);

        Map<String, String> headerData = httpUtil.buildHeaderData(REFERER, HOST, COOKIE);
        String content = httpUtil.getContent(url, headerData);

        if (!StringUtils.hasText(content)) {
            return singer;
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
            return singer;

        // 解析关联的歌手
        Map relatedChannelData = (Map) dataObj.get("related_channel");

        // 保存关联的歌手后，收集关联歌手的 id
        List<String> similarIds = new ArrayList<>();
        spideredUtil.getRelatedChannelSimilarArtistsId(relatedChannelData, similarIds);
        // 设置给主歌曲
        singer.setSimilarSingerIds(similarIds);
        // 保存主歌手数据，主要为了修改关联歌手 id 字段
        singerService.modify(singer);
        return singer;
    }

    public Singer getSingerBySingerId(String singerId, Singer singer){
        singer.setId(singerId);
        String url = MessageFormat.format(SINGER_URL, singerId);

        Map<String, String> headerData = httpUtil.buildHeaderData(REFERER, HOST, COOKIE);
        String content = httpUtil.getContent(url, headerData);

        if (!StringUtils.hasText(content)) {
            return singer;
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
            return singer;

        // 解析关联的歌手
        Map relatedChannelData = (Map) dataObj.get("related_channel");

        // 保存关联的歌手后，收集关联歌手的 id
        List<String> similarIds = new ArrayList<>();
        spideredUtil.getRelatedChannelSimilarArtistsId(relatedChannelData, similarIds);
        // 设置给主歌曲
        singer.setSimilarSingerIds(similarIds);
        // 保存主歌手数据，主要为了修改关联歌手 id 字段
        spideredUtil.saveSinger(singer);
        return singer;
    }

    private void updateSingerDatabase(){
        List<Singer> singers = singerService.getNotBeSpideredSinger(10);
        logger.info("we need catch singers's size is " + singers.size());
        if (singers == null || singers.isEmpty()) {
            logger.info("null singers");
            return;
        }
        // 遍历每个歌手
        for (Singer singer : singers)
            updateSingerBysingerId(singer.getId(), singer);
    }

    private class MinuteTask implements Runnable {
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

    private class HourTask implements Runnable {
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

//    public List<Singer> getSimilarSingerBySingerId(String singerId){
//        List<String> ids = singerService.get(singerId).getSimilarSingerIds();
//        List<Singer> singers = new ArrayList<>();
//        for(String id:ids){
//            singers.add(getSingerById(id));
//        }
//        return singers;
//    }


}
