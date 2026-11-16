# 启动列表与空数据

## 已验证的后端行为
2026-11-16 在 K70 观察到 goods/latestUsed 返回 code=0、msg=成功、data=null；message/index/list 返回 code=0、data=[]。最近使用为空是合法状态，不表示网络、登录或洗衣机硬件故障。

## 处理规则
- LatestUsedResponse.data 必须可空，与实际 JSON 一致。
- 仅 getLatestUsed 允许将成功响应的 null 列表转换成空列表；其他列表接口保留原有 null 数据错误检查。
- 非成功状态先处理，不能因 data=null 吞掉登录或业务错误。
- WashListViewModel 收到成功空列表时清空最近使用数据，不弹异常；界面显示暂无记录与扫码指引。
- ListResponseStateTest 使用实际响应形式回归 null、空数组、有记录、登录错误和其他接口严格模式。
