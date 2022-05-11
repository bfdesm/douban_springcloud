package fm.douban.dao;


import fm.douban.dataobject.CommentDO;
import fm.douban.model.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentDAO {

    int batchAdd(@Param("list") List<CommentDO> userDOs);

    List<CommentDO> findAll();

    int insert(CommentDO commentDO);

    int update(CommentDO commentDO);

    int delete(@Param("id") long id);

    List<Comment> findByRefId(@Param("refId") String refId);

    List<CommentDO> findByUserIds(@Param("userIds") List<Long> ids);
}
