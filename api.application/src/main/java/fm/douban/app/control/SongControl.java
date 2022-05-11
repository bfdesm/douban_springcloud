package fm.douban.app.control;

import fm.douban.model.PageView;
import fm.douban.model.Song;
import fm.douban.param.SongQueryParam;
import fm.douban.service.SongService;
import fm.douban.spider.SongSpider;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;

@Controller
public class SongControl {

    @DubboReference()
    private SongService songService;

    @Autowired
    private SongSpider songSpider;

    @Autowired
    private KafkaTemplate<String, String> kafkaSongTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaPageViewTemplate;

    @GetMapping(path = "/song/random")
    @ResponseBody
    public Song randomSong() {
        return songService.getRandomSong(1).get(0);
    }

    @GetMapping(path = "/song")
    @ResponseBody
    public Song getSongById(@RequestParam(name = "songId") String songId) {
        Song song = songService.get(songId);
        if (song == null) {
            song.setId(songId);
            songSpider.getSongBySongId(songId, song);
        }
        kafkaSongTemplate.send("songView", song.getName());
        kafkaPageViewTemplate.send("pageView", "song");
        return song;
    }

    @GetMapping(path = "/songs")
    @ResponseBody
    public List<Song> getSongByKeyWord(@RequestParam(name = "KeyWord") String keyWord) {
        SongQueryParam query = new SongQueryParam();
        query.setName(keyWord);
        query.setLyrics(keyWord);
        List<Song> songs = songService.list(query).getContent();
        for(Song song:songs){
            kafkaSongTemplate.send("songView", song.getName());
            kafkaPageViewTemplate.send("pageView", "song");
        }
        return songs;
    }
}
