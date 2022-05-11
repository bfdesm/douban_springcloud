package fm.douban.app.control;

import com.alibaba.fastjson.JSON;
import fm.douban.model.Notify;
import fm.douban.service.NotifyHelper;
import fm.douban.service.impl.DingDingHelper;
import fm.douban.service.impl.EnterpriseWechatHelper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class NotifyConsumer {
    private NotifyHelper dingDingHelper = new DingDingHelper();
    private NotifyHelper enterpriseWechatHelper = new EnterpriseWechatHelper();

    @KafkaListener(topics = {"dingDingNotify"})
    public void dingDinglistener(ConsumerRecord<?, ?> record) {
        // #2. 如果消息存在
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            // #3. 获取消息
            String message = (String) kafkaMessage.get();
            Notify notify = JSON.parseObject(message, Notify.class);
            dingDingHelper.sendDingDingNotify(notify);
        }
    }

    @KafkaListener(topics = {"wechatNotify"})
    public void wechatlistener(ConsumerRecord<?, ?> record) {
        // #2. 如果消息存在
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            // #3. 获取消息
            String message = (String) kafkaMessage.get();
            Notify notify = JSON.parseObject(message, Notify.class);
            enterpriseWechatHelper.sendWechatNotify(notify);
        }
    }
}
