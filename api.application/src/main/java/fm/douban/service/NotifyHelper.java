package fm.douban.service;

import fm.douban.model.Notify;

public interface NotifyHelper {
    String sendDingDingNotify(Notify notify);

    String sendWechatNotify(Notify notify);
}
