package fm.douban.service.impl;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import fm.douban.model.Singer;
import fm.douban.service.SingerService;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@DubboService
public class SingerServiceImpl implements SingerService {

    private static final Logger LOG = LoggerFactory.getLogger(SingerServiceImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    private Random random = new Random();

    @Override
    public Singer addSinger(Singer singer) {
        // 作为服务，要对入参进行判断，不能假设被调用时，传入的一定是真正的对象
        if (singer == null) {
            LOG.error("input singer data is null.");
            return null;
        }

        if (singer.getGmtCreated() == null) {
            singer.setGmtCreated(LocalDateTime.now());
        }
        if (singer.getGmtModified() == null) {
            singer.setGmtModified(LocalDateTime.now());
        }

        return mongoTemplate.insert(singer);
    }

    @Override
    public Singer get(String singerId) {
        // 输入的主键 id 必须有文本，不能为空或全空格
        if (!StringUtils.hasText(singerId)) {
            LOG.error("input singerId is blank.");
            return null;
        }
        Singer singer = mongoTemplate.findById(singerId, Singer.class);
        return singer;
    }

    @Override
    public List<Singer> getAll() {
        return mongoTemplate.findAll(Singer.class);
    }

    public Singer getSingerByName(String name) {
        Query query = new Query(Criteria.where("name").is(name)).limit(1);
        return mongoTemplate.find(query, Singer.class).get(0);
    }

    public List<Singer> getSingersByIds(List<String> ids) {
        Criteria criteria = new Criteria();
        // 可能有多个子条件
        List<Criteria> subCris = new ArrayList();
        for (String id : ids) {
            subCris.add(Criteria.where("_id").is(id));
        }
        if (!subCris.isEmpty()) {
            criteria.orOperator(subCris.toArray(new Criteria[]{}));
        }
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Singer.class);
    }

    @Override
    public boolean modify(Singer singer) {
        // 作为服务，要对入参进行判断，不能假设被调用时，入参一定正确
        if (singer == null || !StringUtils.hasText(singer.getId())) {
            LOG.error("input singer data is not correct.");
            return false;
        }

        // 主键不能修改，作为查询条件
        Query query = new Query(Criteria.where("id").is(singer.getId()));

        Update updateData = buildupdateData(singer);

        // 把一条符合条件的记录，修改其字段
        UpdateResult result = mongoTemplate.updateFirst(query, updateData, Singer.class);

        return result != null && result.getModifiedCount() > 0;
    }

    @Override
    public boolean delete(String singerId) {
        // 输入的主键 id 必须有文本，不能为空或全空格
        if (!StringUtils.hasText(singerId)) {
            LOG.error("input singerId is blank.");
            return false;
        }

        Singer singer = new Singer();
        singer.setId(singerId);

        DeleteResult result = mongoTemplate.remove(singer);
        return result != null && result.getDeletedCount() > 0;
    }

    public List<Singer> getNotBeSpideredSinger(int num) {
        // 总条件
        Criteria criteria = new Criteria();
        // 可能有多个子条件
        List<Criteria> subCris = new ArrayList();
        subCris.add(Criteria.where("beSpidered").is(null));
        subCris.add(Criteria.where("beSpidered").is(false));
        if (!subCris.isEmpty()) {
            criteria.orOperator(subCris.toArray(new Criteria[]{}));
        }
        Query query = new Query(criteria).limit(num);
        return mongoTemplate.find(query, Singer.class);
    }

    public List<Singer> getRandom(int limit) {
        long total = getCount();
        long startNum = Math.abs(random.nextInt((int) total));
        while (startNum > total - limit)
            startNum = Math.abs(random.nextInt((int) total));
        Query query = new Query().skip(startNum).limit(limit);
        return mongoTemplate.find(query, Singer.class);
    }

    public int getCount() {
        return (int) mongoTemplate.count(new Query(), Singer.class);
    }

    private List<Criteria> buildCriteria(Singer singer){
        List<Criteria> subCris = new ArrayList();
        if (StringUtils.hasText(singer.getName())) {
            subCris.add(Criteria.where("name").is(singer.getName()));
        }
        if (StringUtils.hasText(singer.getHomepage())) {
            subCris.add(Criteria.where("homePage").is(singer.getHomepage()));
        }
        if (StringUtils.hasText(singer.getAvatar())) {
            subCris.add(Criteria.where("avatar").is(singer.getAvatar()));
        }
        return subCris;
    }

    private Update buildupdateData(Singer singer) {
        Update updateData = new Update();
        // 每次修改都更新 gmtModified 为当前时间
        updateData.set("gmtModified", LocalDateTime.now());
        // 值为 null 表示不修改。值为长度为 0 的字符串 "" 表示清空此字段
        if (singer.getName() != null) {
            updateData.set("name", singer.getName());
        }
        if (singer.getAvatar() != null) {
            updateData.set("avatar", singer.getAvatar());
        }
        if (singer.getHomepage() != null) {
            updateData.set("homepage", singer.getHomepage());
        }
        if (singer.getSimilarSingerIds() != null) {
            updateData.set("similarSingerIds", singer.getSimilarSingerIds());
        }
        if(singer.getLike()!=null &&singer.getLike()!=0)
            updateData.set("like", singer.getLike());
        if(singer.getSongsId() != null &&singer.getSongsId().size() != 0)
            updateData.set("songsId", singer.getSongsId());
        updateData.set("beSpidered", true);
        return updateData;
    }
}