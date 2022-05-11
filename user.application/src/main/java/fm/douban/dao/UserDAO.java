package fm.douban.dao;

import fm.douban.dataobject.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserDAO {

    int batchAdd(@Param("list") List<UserDO> userDOs);

    int add(UserDO userDO);

    int delete(@Param("id") long id);

    int update(UserDO userDO);

    List<UserDO> findByIds(@Param("ids") List<Long> ids);

    List<UserDO> findAll();

    UserDO findByUserName(@Param("userName") String name);

    List<UserDO> query(@Param("keyWord") String keyWord);

    List<UserDO> search(@Param("keyWord") String keyWord, @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);
}
