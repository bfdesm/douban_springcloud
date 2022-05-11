package fm.douban.app.control;

import fm.douban.model.*;
import fm.douban.service.SingerService;
import fm.douban.service.SongService;
import fm.douban.service.SubjectService;
import fm.douban.spider.SingerSpider;
import fm.douban.util.SubjectUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Controller
public class SubjectControl {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectControl.class);

    @DubboReference()
    private SubjectService subjectService;

    @DubboReference()
    private SingerService singerService;

    @DubboReference()
    private SongService songService;

    @Autowired
    private SingerSpider singerSpider;

    @Autowired
    private KafkaTemplate<String, String> kafkaSubjectTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaPageViewTemplate;

    @GetMapping(path = "/artist")
    public String artistDetail(Model model, @RequestParam(name = "singerId") String singerId) {
        Singer singer = singerService.get(singerId);
        if (singer == null) {
            singer = new Singer();
            singer.setId(singerId);
            singer = singerSpider.getSingerBySingerId(singerId, singer);
        }
        model.addAttribute("singer", singer);
        Subject subject = subjectService.getSubjectByName(singer.getName() + " 系");
        if (subject == null) {
            subject = new Subject();
        }
        model.addAttribute("subject", subject);
        kafkaSubjectTemplate.send("subjectView", subject.getName());
        kafkaPageViewTemplate.send("pageView", "subject");
        if(subject.getSongIds() != null && subject.getSongIds().size() != 0){
            model.addAttribute("songs", songService.getSongs(subject.getSongIds()));
        }else{
            List<Song> songs = songService.getRandomSong(8);
            model.addAttribute("songs", songs);
        }
        List<Singer> simSingers;
        if(singer.getSimilarSingerIds() == null || singer.getSimilarSingerIds().size() == 0)
            simSingers = singerService.getRandom(8);
        else{
            simSingers = singerService.getSingersByIds(singer.getSimilarSingerIds());
            simSingers = simSingers.size() > 8? simSingers.subList(0,8):simSingers;
        }
        model.addAttribute("simSingers", simSingers);
        return "artist";
    }

    @GetMapping(path = "/collection")
    public String collection(Model model) {
        List<Subject> subjects = subjectService.getSubjects(SubjectUtil.TYPE_COLLECTION);

        List<List<CollectionViewModel>> subjectColumns = new ArrayList<>();
        // 最大行数
        int lineCount = (subjects.size() % 5 == 0) ? subjects.size() / 5 : (subjects.size() / 5) + 1;
        // 列数，最多 5 列
        for (int i = 0; i < 5; i++) {
            // 每列的元素
            List<CollectionViewModel> column = new ArrayList<>();
            // 第一列的元素是 0 5 11
            // j 是行数
            for (int j = 0; j < lineCount; j++) {
                int itemIndex = i + j * 5;
                if (itemIndex < subjects.size()) {
                    Subject subject = subjects.get(itemIndex);
                    CollectionViewModel xvm = new CollectionViewModel();
                    xvm.setSubject(subject);

                    if (subject.getMaster() != null) {
                        Singer singer = singerService.get(subject.getMaster());
                        if(singer==null){
                            singer = new Singer();
                            singer.setId(subject.getMaster());
                            singer = singerSpider.getSingerBySingerId(subject.getMaster(), singer);
                        }
                        xvm.setSinger(singer);
                    }

                    if (subject.getSongIds() != null && !subject.getSongIds().isEmpty()) {
                        List<Song> songs = songService.getSongs(subject.getSongIds());
                        xvm.setSongs(songs);
                    }
                    column.add(xvm);
                }
            }
            subjectColumns.add(column);
        }

        model.addAttribute("subjectColumns", subjectColumns);

        return "collection";
    }

    @GetMapping(path = "/collectiondetail")
    public String collectionDetail(Model model, @RequestParam(name = "subjectId") String subjectId) {
        Subject subject = subjectService.get(subjectId);
        if (subject == null) {
            return "error";
        }

        model.addAttribute("subject", subject);
        kafkaSubjectTemplate.send("subjectView", subject.getName());
        kafkaPageViewTemplate.send("pageView", "subject");

        List<String> songIds = subject.getSongIds();
        List<Song> songs = new ArrayList<>();

        if (songIds != null && !songIds.isEmpty()) {
            for (String songId : songIds) {
                Song song = songService.get(songId);
                if (song != null) {
                    songs.add(song);
                }
            }
        }
        model.addAttribute("songs", songs);
        Singer singer = singerService.get(subject.getMaster());
        model.addAttribute("singer", singer);
        // 查询其它歌单
        Subject subjectParam = new Subject();
        subjectParam.setSubjectType(SubjectUtil.TYPE_COLLECTION);
        subjectParam.setMaster(singer.getId());
        List<Subject> otherSubjects = subjectService.getSubjects(subjectParam);
        model.addAttribute("otherSubjects", otherSubjects);

        return "collectiondetail";
    }

    @GetMapping(path = "/mhzdetail")
    public String mhzDetail(Model model, @RequestParam(name = "subjectId") String subjectId) {
        Subject subject = subjectService.get(subjectId);
        List<Singer> simSingers = new ArrayList<>();
        if (subject == null) {
            return "error";
        }
        model.addAttribute("subject", subject);
        kafkaSubjectTemplate.send("subjectView", subject.getName());
        kafkaPageViewTemplate.send("pageView", "subject");
        List<String> songIds = subject.getSongIds();
        List<Song> songs = new ArrayList<>();
        if (songIds != null && !songIds.isEmpty()) {
            songs = songService.getSongs(subject.getSongIds());
            List<Singer> finalSimSingers = simSingers;
            songs.forEach(song -> finalSimSingers.addAll(singerService.getSingersByIds(song.getSingerIds())));
        }
        if(songs != null && songs.size() >= 5)
            songs = songs.subList(0,5);
        model.addAttribute("songs", songs);
        HashSet set = new HashSet(simSingers);
        //把List集合所有元素清空
        simSingers.clear();
        //把HashSet对象添加至List集合
        simSingers.addAll(set);
        if (simSingers.size() == 0) {
            simSingers = singerService.getRandom(5);
        }
        model.addAttribute("simSingers", simSingers);
        return "mhzdetail";
    }
}
