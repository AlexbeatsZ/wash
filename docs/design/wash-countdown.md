# 洗衣剩余时间

## 订单与计时

- 最近使用列表继续读取 `goods/latestUsed` 的洗衣分类。倒计时读取 `order/list` 已支付订单的 `order/detail`，按商品 ID 关联机器。
- 订单已支付、没有实际完成时间且 `completeTime` 在未来时才显示倒计时。列表按下单时间从新到旧选取每台机器的有效订单。详情请求失败时保留之前仍有效的预计时间。
- 服务端时间字符串按 `Asia/Shanghai` 解析。每次显示时用预计完成时间减去当前时间。进度条以 `markMinutes` 为基准并限制在 0–100%，时长缺失时只显示倒计时。
- 洗衣机页恢复前台时刷新，检测到已支付订单后每 30 秒再核对详情与机器状态。应用退到后台后，通知继续倒数至最近一次取得的预计结束时间。
- K70 的真实已完成订单返回状态 3、`completeTime=null` 和有效的实际 `finishTime`。带 `orderStatus=2` 的查询成功返回空列表。正在运行的实单接口尚待比对。

## Android 通知

- 每台机器各一条通知，更新同一台机器的预计时间时沿用同一通知。正文由系统渲染的 `RemoteViews` 原生倒计时显示「预计剩余」，隐藏普通通知时间，预计到期后由系统移除。
- 新版本启动时清除旧版前台服务留下的无标签 ID 1 通知。
- 参考 [Android 自定义通知文档](https://developer.android.com/develop/ui/views/notifications/custom-notification) 和 [RemoteViews API](https://developer.android.com/reference/android/widget/RemoteViews)。

## 胖乖生活参考

- 从[杭州企鹅科技官网](https://www.qiekj.com)的安卓二维码下载 `com.qiekj.user` 1.81.4（147）。APK 的启动成功页和订单详情页都包含 `tvDownTime`，启动成功页将其标为「剩余时间」，并提示实际洗涤时长受水压和衣物重量影响。
- APK 的可见 DEX 包含加固入口和资源表。布局可以验证预计时间的展示，无法从可见 DEX 确认官方计算公式。项目使用自身订单接口和可验证的 Android 计时行为。
