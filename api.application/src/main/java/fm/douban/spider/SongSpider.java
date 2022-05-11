package fm.douban.spider;

import fm.douban.model.Song;
import fm.douban.service.SongService;
import fm.douban.util.HttpUtil;
import fm.douban.util.SpideredUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class SongSpider {
    private static final Logger logger = LoggerFactory.getLogger(SingerSpider.class);

    private static final String HOST = "fm.douban.com";
    private static final String REFERER = "https://fm.douban.com/";
    private static final String COOKIE = "viewed=\"26259017\"; bid=Ixdu8ZtzEXw; gr_user_id=68d4e16d-24f6-45a1-bad5-e354cbb2dd56; __utma=30149280.1510717963.1638194508.1638194508.1638194508.1; __utmz=30149280.1638194508.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); _pk_ref.100001.f71f=%5B%22%22%2C%22%22%2C1642772724%2C%22https%3A%2F%2Fwww.baidu.com%2Fs%3Fie%3Dutf-8%26f%3D8%26rsv_bp%3D1%26ch%3D%26tn%3Dbaidu%26bar%3D%26wd%3D%25E8%25B1%2586%25E7%2593%25A3FM%26oq%3D%2525E8%2525B1%252586%2525E7%252593%2525A3%26rsv_pq%3Df6837070000030a9%26rsv_t%3D120bl0XlmRxmiiPJ5LNEasCYTUA1uuAvMsQlYVXaFCFAgnog74w3NrEvAZc%26rqlang%3Dcn%26rsv_enter%3D1%26rsv_dl%3Dtb%26rsv_sug3%3D3%26rsv_sug1%3D3%26rsv_sug7%3D100%22%5D; _ga=GA1.2.1510717963.1638194508; _gid=GA1.2.82405320.1642772725; _pk_id.100001.f71f=97dee38d0a3f8eaf.1642772724.1.1642773049.1642772724.";

    private static final String SINGER_URL = "https://fm.douban.com/j/v2/artist/{0}/";

    @Autowired
    private HttpUtil httpUtil;

    @Autowired
    private SpideredUtil spideredUtil;

    @DubboReference()
    private SongService songService;

    //@PostConstruct
    public void init() {
        MinuteTask minuteTask = new MinuteTask("MinuteSongDataSpiderTask");
        minuteTask.setLoopMinute(minuteTask.getLoopMinute() * 3);
        minuteTask.setWaitMinute(10000);
        Thread thread = new Thread(minuteTask);
        thread.start();

        HourTask hourTask1 = new HourTask("SpiderAllSongToUpdateTask");
        hourTask1.setLoopHour(hourTask1.getLoopHour() * 24 * 30);
        hourTask1.setWaitHour(30000);
//        Thread thread3 = new Thread(hourTask1);
//        thread3.start();
    }

    public void doTreadTask(String taskName) {
        switch (taskName) {
            case "MinuteSongDataSpiderTask":
                doPrimarySprider();
                break;
            case "HourSongDataSpiderTask":
                doHourSongDataSpiderTask();
                break;
            case "DaySongDataSpiderTask":
                doDaySongDataSpiderTask();
                break;
            case "SpiderAllSongToUpdateTask":
                //spiderAllSongToUpdate();  //还未改完
                break;
            default:
                break;
        }
    }

    public void doPrimarySprider() {
        updateSongDatabase();
    }

    public void doHourSongDataSpiderTask() {

    }

    public void doDaySongDataSpiderTask() {

    }

    public void spiderAllSongToUpdate() {
        logger.info("SpiderAllSongToUpdate start");
        try {
            List<Song> songs = songService.getAll();
        } catch (Exception e) {
            logger.info("spiderAllSong is error");
            logger.info(String.valueOf(e));
        }
        logger.info("SpiderAllSongToUpdate end");
    }

    //还未改完
    private void updateSongDatabase() {
        List<Song> songs = songService.getNotBeSpideredSong(10);
        logger.info("we need catch songs's size is " + songs.size());
        if (songs == null || songs.isEmpty()) {
            logger.info("null singers");
            return;
        }
        for (Song song : songs)
            updateSongBysongId(song.getId(), song);
    }

    public Song updateSongBysongId(String songId, Song song) {






        return song;
    }

    public Song getSongBySongId(String songId,Song song){
        return updateSongBysongId(songId, song);
    }

    public void spiderImg(String url) {
        Map<String, String> headerData = httpUtil.buildHeaderData(REFERER, HOST, COOKIE);
        byte[] content = httpUtil.getBytes(url, headerData);
        logger.error("!!!!!!!!!!!!!!!");
        try {
            Random random = new Random();
            File file = new File("D:"+ File.separator + "hello" + File.separator + Math.abs(random.nextInt()) +".jpg");  //1.指定要操作文件的路径
            if(!file.getParentFile().exists()){   //文件不存在
                file.getParentFile().mkdirs();   //创建父目录
            }
            FileOutputStream fos = new FileOutputStream(file);
            logger.error("!!!!!!!!!!!!!!!");
            fos.write(content);
            //记得及时释放资源
            fos.close();
        } catch (IOException e) {
            logger.error("error");
            logger.error(String.valueOf(e));
        }
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
}
