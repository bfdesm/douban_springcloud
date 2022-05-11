package fm.douban.app.control;

import fm.douban.model.*;
import fm.douban.param.SongQueryParam;
import fm.douban.service.FavoriteService;
import fm.douban.service.SingerService;
import fm.douban.service.SongService;
import fm.douban.service.SubjectService;
import fm.douban.util.FavoriteUtil;
import fm.douban.util.SubjectUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class MainControl {
    private static final Logger LOG = LoggerFactory.getLogger(MainControl.class);

    @DubboReference(timeout = 6000)
    private SubjectService subjectService;

    @DubboReference()
    private SingerService singerService;

    @DubboReference()
    private SongService songService;

    @DubboReference()
    private FavoriteService favoriteService;

    @Autowired
    private KafkaTemplate<String, String> kafkaPageViewTemplate;

    @GetMapping(path = "/index")
    public String index(Model model) {
        // 设置首屏歌曲数据
        setSongData(model);
        // 设置赫兹数据
        setMhzData(model);
        kafkaPageViewTemplate.send("pageView","index");
        return "index";
    }

    @GetMapping(path = "/index/song")
    public String indexSong(Model model) {
        // 设置首屏歌曲数据
        setSongData(model);
        // 设置赫兹数据
        setMhzData(model);
        return "index::player";
    }

    @GetMapping(path = "/index/artists")
    @ResponseBody
    public List<Singer> indexArtist() {
        return singerService.getRandom(10);
    }

    @GetMapping(path = "/index/subjects")
    @ResponseBody
    public List<Subject> indexSubject() {
        return subjectService.getRandomSubject("mhz", "artist", 10);
    }

    @GetMapping(path = "/search")
    public String search(Model model) {
        return "search";
    }

    @GetMapping(path = "/layout")
    public String layout() {
        return "layout";
    }

    @GetMapping(path = "/searchContent")
    @ResponseBody
    public Map searchContent(@RequestParam(name = "keyword") String keyword) {
        SongQueryParam songParam = new SongQueryParam();
        songParam.setName(keyword);
        Page<Song> songs = songService.list(songParam);

        Map result = new HashMap<>();
        result.put("songs", songs);
        //kafkaPageViewTemplate.send("pageView", "searchContent");
        return result;
    }

    @GetMapping(path = "/my")
    public String my(Model model, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        UserLoginInfo userLoginInfo = (UserLoginInfo) session.getAttribute("userLoginInfo");

        String userId = userLoginInfo.getUserId();

        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setType(FavoriteUtil.TYPE_RED_HEART);
        List<Favorite> favs = favoriteService.list(fav);

        model.addAttribute("favorites", favs);

        List<Song> favedSongs = new ArrayList<>();
        if (favs != null && !favs.isEmpty()) {
            for (Favorite favorite : favs) {
                if (FavoriteUtil.TYPE_RED_HEART.equals(favorite.getType()) && FavoriteUtil.ITEM_TYPE_SONG.equals(
                        favorite.getItemType())) {
                    Song song = songService.get(favorite.getItemId());
                    if (song != null) {
                        favedSongs.add(song);
                    }
                }
            }
        }
        model.addAttribute("songs", favedSongs);
        //kafkaPageViewTemplate.send("pageView", "my");
        return "my";
    }

    @GetMapping(path = "/fav")
    @ResponseBody
    public Map fav(@RequestParam(name = "itemType") String itemType, @RequestParam(name = "itemId") String itemId,
                     HttpServletRequest request) {
        Map resultData = new HashMap();
        // 取得 HttpSession 对象
        HttpSession session = request.getSession();
        UserLoginInfo userLoginInfo = (UserLoginInfo) session.getAttribute("userLoginInfo");
        String userId = userLoginInfo.getUserId();

        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setType(FavoriteUtil.TYPE_RED_HEART);
        fav.setItemType(itemType);
        fav.setItemId(itemId);
        List<Favorite> favs = favoriteService.list(fav);
        if (favs == null || favs.isEmpty()) {
            favoriteService.add(fav);
        } else {
            for (Favorite f : favs) {
                favoriteService.delete(f);
            }
        }

        resultData.put("message", "successful");
        //kafkaPageViewTemplate.send("pageView", "fav");
        return resultData;
    }

    @GetMapping(path = "/share")
    public String share(Model model) {
        kafkaPageViewTemplate.send("pageView", "share");
        return "share";
    }

    @GetMapping(path = "/error")
    public String error(Model model) {
        kafkaPageViewTemplate.send("pageView", "error");
        return "error";
    }

    @GetMapping(path = "/user")
    public String user(Model model, @RequestParam("userId")String userId) {
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setType(FavoriteUtil.TYPE_RED_HEART);
        List<Favorite> favs = favoriteService.list(fav);

        model.addAttribute("favorites", favs);

        List<Song> favedSongs = new ArrayList<>();
        if (favs != null && !favs.isEmpty()) {
            for (Favorite favorite : favs) {
                if (FavoriteUtil.TYPE_RED_HEART.equals(favorite.getType()) && FavoriteUtil.ITEM_TYPE_SONG.equals(
                        favorite.getItemType())) {
                    Song song = songService.get(favorite.getItemId());
                    if (song != null) {
                        favedSongs.add(song);
                    }
                }
            }
        }
        model.addAttribute("songs", favedSongs);
        //kafkaPageViewTemplate.send("pageView", "user");
        return "my";
    }

    private void setSongData(Model model) {
        SongQueryParam songParam = new SongQueryParam();
        songParam.setPageNum(1);
        songParam.setPageSize(1);
        List<Song> songs = songService.getRandomSong(1);
        if (songs != null && !songs.isEmpty()) {
            Song resultSong = songs.get(0);

            model.addAttribute("song", resultSong);

            List<Singer> singers = singerService.getSingersByIds(resultSong.getSingerIds());

            model.addAttribute("singers", singers);
        }
    }

    private void setMhzData(Model model) {
        //List<Subject> subjectDatas = subjectService.getSubjects(SubjectUtil.TYPE_MHZ);

        List<Subject> artistDatas = subjectService.getSubjects(SubjectUtil.TYPE_MHZ, SubjectUtil.TYPE_SUB_ARTIST);
        List<Subject> moodDatas = subjectService.getSubjects(SubjectUtil.TYPE_MHZ, SubjectUtil.TYPE_SUB_MOOD);
        List<Subject> ageDatas = subjectService.getSubjects(SubjectUtil.TYPE_MHZ, SubjectUtil.TYPE_SUB_AGE);
        List<Subject> styleDatas = subjectService.getSubjects(SubjectUtil.TYPE_MHZ, SubjectUtil.TYPE_SUB_STYLE);

//        if (subjectDatas != null && !subjectDatas.isEmpty()) {
//            for (Subject subject : subjectDatas) {
//                if (SubjectUtil.TYPE_SUB_MOOD.equals(subject.getSubjectSubType())) {
//                    moodDatas.add(subject);
//                } else if (SubjectUtil.TYPE_SUB_AGE.equals(subject.getSubjectSubType())) {
//                    ageDatas.add(subject);
//                } else if (SubjectUtil.TYPE_SUB_STYLE.equals(subject.getSubjectSubType())) {
//                    styleDatas.add(subject);
//                }
//                else {
//                    // 防止数据错误
//                    LOG.error("subject data error. unknown subtype. subject=" + JSON.toJSONString(subject));
//                }
//            }
//        }

        // 按照页面视觉组织数据：艺术家mhz，由于是独立的区块，不放一起
        Collections.shuffle(artistDatas);
        model.addAttribute("artistDatas", artistDatas);

        // 按照页面视觉组织数据：按顺序填入三个赫兹数据
        List<MhzViewModel> mhzViewModels = new ArrayList<>();
        buildMhzViewModel(moodDatas, "心情 / 场景", mhzViewModels);
        buildMhzViewModel(ageDatas, "语言 / 年代", mhzViewModels);
        buildMhzViewModel(styleDatas, "风格 / 流派", mhzViewModels);
        model.addAttribute("mhzViewModels", mhzViewModels);
    }

    private void buildMhzViewModel(List<Subject> subjects, String title, List<MhzViewModel> mhzViewModels) {
        MhzViewModel mhzVO = new MhzViewModel();
        mhzVO.setSubjects(subjects);
        mhzVO.setTitle(title);
        mhzViewModels.add(mhzVO);
    }

}
