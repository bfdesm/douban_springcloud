# douban_springcloud

# 运行配置
1. java.version=11
2. vm=--add-opens java.base/java.lang=ALL-UNNAMED
3. 本地配置mysql, mongodb, redis, kafka, nacos, zookeeper

## mysql
1. username:root
2. password:root

## mongodb
1. username:mongo
2. password:mongo

## redis
1. password:redis

## kafka
1. port:9092

## nacos
1. port:8848
2. admin-url:http://192.168.179.1:8848/nacos/index.html

## zookeeper
1. username:root
2. password:root

# 启动顺序
api.application 最后启动,其余无顺序要求

# 网页
1. 使用端口8088: http://127.0.0.1:8088/index

# 项目模块介绍
## api.application
1. 整合所有模块的control网页
2. 有爬虫模块
## comment.application
1. 用于评论
## favorite.application
1. 用于收藏，红心
## main.application
1. 用于主页控制（尚未想到业务）
## singer.application
1. 用于歌手的CRUD
## song.application
1. 用于歌曲的CRUD
## subject.application
1. 用于专辑的CRUD
## user.application
1. 用于用户的CRUD
2. 用户登陆以及限制访问
3. 验证码邮箱发送

# 其他项目
1. 豆瓣web  单机项目: https://github.com/bfdesm/douban_single
2. 豆瓣web  dubbo分布式项目: https://github.com/bfdesm/duoban_dubbo
3. 豆瓣web  springcloud分布式项目: https://github.com/bfdesm/douban_springcloud(待加入)
4. 豆瓣web  springcloud-alibaba分布式项目: https://github.com/bfdesm/douban_springcloud_alibaba
