package fm.douban.service.impl;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import fm.douban.model.Singer;
import fm.douban.model.Subject;
import fm.douban.service.SubjectService;
import fm.douban.util.IsNullUtil;
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
public class SubjectServiceImpl implements SubjectService {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectServiceImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    private Random random = new Random();

    @Override
    public Subject addSubject(Subject subject) {
        // 作为服务，要对入参进行判断，不能假设被调用时，传入的一定是真正的对象
        if (subject == null) {
            LOG.error("input subject data is null.");
            return null;
        }

        if (subject.getGmtCreated() == null) {
            subject.setGmtCreated(LocalDateTime.now());
        }
        if (subject.getGmtModified() == null) {
            subject.setGmtModified(LocalDateTime.now());
        }
        return mongoTemplate.insert(subject);
    }

    @Override
    public boolean modify(Subject subject) {
        if (subject == null || !StringUtils.hasText(subject.getId())) {
            LOG.error("input subject data is not correct.");
            LOG.info(JSON.toJSONString(subject));
            return false;
        }
        // 主键不能修改
        Query query = new Query(Criteria.where("_id").is(subject.getId()));
        Update updateData = buildupdateData(subject);
        UpdateResult result = mongoTemplate.updateFirst(query, updateData, Singer.class);
        return result.getModifiedCount() > 0;
    }

    @Override
    public Subject get(String subjectId) {
        // 输入的主键 id 必须有文本，不能为空或全空格
        if (!StringUtils.hasText(subjectId)) {

            LOG.error("get: input subjectId is blank.");
            return null;
        }
        Subject subject = mongoTemplate.findById(subjectId, Subject.class);
        return subject;
    }

    @Override
    public List<Subject> getSubjects(String type) {
        return getSubjects(type, null);
    }

    @Override
    public List<Subject> getSubjects(String type, String subType) {
        Subject subject = new Subject();
        subject.setSubjectType(type);
        subject.setSubjectSubType(subType);
        return getSubjects(subject);
    }

    @Override
    public List<Subject> getSubjects(Subject subject) {
        // 作为服务，要对入参进行判断，不能假设被调用时，入参一定正确
        if (subject == null) {
            LOG.error("input subjectParam is not correct.");
            return null;
        }

        String type = subject.getSubjectType();
        // 作为服务，要对入参进行判断，不能假设被调用时，入参一定正确
        if (!StringUtils.hasText(type)) {
            LOG.error("input type is not correct.");
            return null;
        }

        // 总条件
        Criteria criteria = new Criteria();
        // 可能有多个子条件
        List<Criteria> subCris = buildCriteria(subject);

        // 三个子条件以 and 关键词连接成总条件对象，相当于 name='' and lyrics='' and subjectId=''
        criteria.andOperator(subCris.toArray(new Criteria[]{}));
        // 条件对象构建查询对象
        Query query = new Query(criteria);

        // 查询结果
        List<Subject> subjects = mongoTemplate.find(query, Subject.class);

        return subjects;
    }

    @Override
    public boolean delete(String subjectId) {
        // 输入的主键 id 必须有文本，不能为空或全空格
        if (!StringUtils.hasText(subjectId)) {
            LOG.error("input subjectId is blank.");
            return false;
        }

        Subject subject = new Subject();
        subject.setId(subjectId);

        DeleteResult result = mongoTemplate.remove(subject);
        return result != null && result.getDeletedCount() > 0;
    }

    public List<Subject> getRandomSubject(int limit) {
        long total = getCount();
        long skip = Math.abs(random.nextInt((int) total));
        while (skip > total - limit)
            skip = Math.abs(random.nextInt((int) total));
        Query query = new Query().skip(skip).limit(limit);
        return mongoTemplate.find(query, Subject.class);
    }

    public List<Subject> getRandomSubject(String type, String subType, int limit) {
        long total = getCount();
        long skip = Math.abs(random.nextInt((int) total));
        while (skip > total - limit)
            skip = Math.abs(random.nextInt((int) total));
        // 总条件
        Criteria criteria = new Criteria();
        // 可能有多个子条件
        List<Criteria> subCris = new ArrayList();
        subCris.add(Criteria.where("subjectType").is(type));
        if (StringUtils.hasText(subType)) {
            subCris.add(Criteria.where("subjectSubType").is(subType));
        }
        if (StringUtils.hasText(type)) {
            subCris.add(Criteria.where("subjectType").is(type));
        }
        // 三个子条件以 and 关键词连接成总条件对象，相当于 name='' and lyrics='' and subjectId=''
        criteria.andOperator(subCris.toArray(new Criteria[]{}));
        Query query = new Query(criteria).skip(skip).limit(limit);
        return mongoTemplate.find(query, Subject.class);
    }

    public Subject getSubjectByName(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        Query query = new Query(Criteria.where("name").is(name));
        List<Subject> subjects = mongoTemplate.find(query, Subject.class);
        if(subjects==null||subjects.size()==0)
            return null;
        return subjects.get(0);
    }

    public long getCount() {
        return mongoTemplate.count(new Query(), Subject.class);
    }

    private List<Criteria> buildCriteria(Subject subject){
        List<Criteria> subCris = new ArrayList();
        if (StringUtils.hasText(subject.getName())) {
            subCris.add(Criteria.where("name").is(subject.getName()));
        }
        if (StringUtils.hasText(subject.getSubjectType())) {
            subCris.add(Criteria.where("subjectType").is(subject.getSubjectType()));
        }
        if (StringUtils.hasText(subject.getSubjectSubType())) {
            subCris.add(Criteria.where("subjectSubType").is(subject.getSubjectSubType()));
        }
        if (StringUtils.hasText(subject.getMaster())) {
            subCris.add(Criteria.where("master").is(subject.getMaster()));
        }
        if (StringUtils.hasText(subject.getCover())) {
            subCris.add(Criteria.where("cover").is(subject.getCover()));
        }
        if (StringUtils.hasText(subject.getDescription())) {
            subCris.add(Criteria.where("description").is(subject.getDescription()));
        }
        if (subject.getLiked_count() != null) {
            subCris.add(Criteria.where("liked_count").is(subject.getLiked_count()));
        }
        if (!IsNullUtil.isNull(subject.getPublishedDate())) {
            subCris.add(Criteria.where("publishedDate").is(subject.getPublishedDate()));
        }
        if (!IsNullUtil.isNull(subject.getSongIds())) {
            subCris.add(Criteria.where("songIds").is(subject.getSongIds()));
        }
        return subCris;
    }

    private Update buildupdateData(Subject subject){
        Update updateData = new Update();
        // 值为 null 表示不修改。值为长度为 0 的字符串 "" 表示清空此字段
        if (subject.getGmtModified() != null) {
            updateData.set("gmtModified", subject.getGmtModified());
        }
        if (subject.getSongIds() != null) {
            updateData.set("songIds", subject.getSongIds());
        }
        if (subject.getCover() != null) {
            updateData.set("cover", subject.getCover());
        }
        if (subject.getName() != null) {
            updateData.set("name", subject.getName());
        }
        if (subject.getDescription() != null) {
            updateData.set("description", subject.getDescription());
        }
        if (subject.getMaster() != null) {
            updateData.set("master", subject.getMaster());
        }
        if (subject.getPublishedDate() != null) {
            updateData.set("publishedDate", subject.getPublishedDate());
        }
        if (subject.getSubjectSubType() != null) {
            updateData.set("subjectSubType", subject.getSubjectSubType());
        }
        if (subject.getSubjectType() != null) {
            updateData.set("subjectType", subject.getSubjectType());
        }
        if(subject.getLiked_count()!=null){
            updateData.set("liked_count", subject.getLiked_count());
        }
        updateData.set("beSpidered", true);
        return updateData;
    }
}
