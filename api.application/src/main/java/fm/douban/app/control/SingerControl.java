package fm.douban.app.control;

import fm.douban.model.PageView;
import fm.douban.model.Singer;
import fm.douban.service.SingerService;
import fm.douban.spider.SingerSpider;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;

@Controller
public class SingerControl {
    private static final Logger LOG = LoggerFactory.getLogger(SingerControl.class);

    @DubboReference()
    private SingerService singerService;

    @Autowired
    private SingerSpider singerSpider;

    @Autowired
    private KafkaTemplate<String, String> kafkaSingerTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaPageViewTemplate;

    @GetMapping(path = "/userguide")
    public String myMhz(Model model) {
        List<Singer> randomSingers = singerService.getRandom(8);
        model.addAttribute("singers", randomSingers);
        kafkaPageViewTemplate.send("pageView", "userguide");
        return "userguide";
    }

    @GetMapping(path = "/singer/random")
    @ResponseBody
    public List<Singer> randomSingers() {
        List<Singer> singers = singerService.getRandom(8);
        return singers;
    }

    @GetMapping(path = "/getSingerName")
    @ResponseBody
    public String getSingerName(@RequestParam("singerId")String singerId){
        Singer singer = singerService.get(singerId);
        if(singer == null)
            singer = singerSpider.getSingerBySingerId(singerId, singer);
        kafkaSingerTemplate.send("singerView", singer.getName());
        return singer.getName();
    }
}
