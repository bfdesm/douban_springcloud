package fm.douban.app.control;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import fm.douban.dataobject.CommentDO;
import fm.douban.model.Comment;
import fm.douban.model.PageView;
import fm.douban.model.Paging;
import fm.douban.service.CommentService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller
public class CommentController {

    @DubboReference()
    private CommentService commentService;

    @Autowired
    private KafkaTemplate<String, String> kafkaPageViewTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaCommentDoTemplate;

    @GetMapping("/comments")
    @ResponseBody
    public Paging<CommentDO> getAll(@RequestParam(value = "pageNum", required = false) Integer pageNum,
                                    @RequestParam(value = "pageSize", required = false) Integer pageSize) {

        if (pageNum == null) {
            pageNum = 1;
        }
        if (pageSize == null) {
            pageSize = 15;
        }

        // 设置当前页数为1，以及每页3条记录
        Page<CommentDO> page = PageHelper.startPage(pageNum, pageSize).doSelectPage(() -> commentService.findAll());

        return new Paging<>(page.getPageNum(), page.getPageSize(), page.getPages(), page.getTotal(), page.getResult());
    }

    @PostMapping("/comment")
    @ResponseBody
    public CommentDO save(@RequestBody CommentDO commentDO) {
        commentService.insert(commentDO);
        kafkaCommentDoTemplate.send("commentView", String.valueOf(commentDO.getId()));
        kafkaPageViewTemplate.send("pageView", "comment");
        return commentDO;
    }

    @PostMapping("/comment/batchAdd")
    @ResponseBody
    public List<CommentDO> batchAdd(@RequestBody List<CommentDO> commentDOS) {
        commentService.batchAdd(commentDOS);
        for(CommentDO commentDO: commentDOS){
            kafkaCommentDoTemplate.send("commentView", String.valueOf(commentDO.getId()));
            kafkaPageViewTemplate.send("pageView", "comment");
        }
        return commentDOS;
    }

    @PostMapping("/comment/update")
    @ResponseBody
    public CommentDO update(@RequestBody CommentDO commentDO) {
        commentService.update(commentDO);
        kafkaCommentDoTemplate.send("commentView", String.valueOf(commentDO.getId()));
        kafkaPageViewTemplate.send("pageView", "comment");
        return commentDO;
    }

    @GetMapping("/comment/del")
    @ResponseBody
    public boolean delete(@RequestParam("id") Long id) {
        return commentService.delete(id) > 0;
    }

    @GetMapping("/comment/findByRefId")
    @ResponseBody
    public List<Comment> findByRefId(@RequestParam("refId") String refId) {
        return commentService.findByRefId(refId);
    }

    @GetMapping("/comment/findByUserIds")
    @ResponseBody
    public List<CommentDO> findByUserIds(@RequestParam("userIds") List<Long> ids) {
        return commentService.findByUserIds(ids);
    }
}
