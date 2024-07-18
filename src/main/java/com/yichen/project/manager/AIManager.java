package com.yichen.project.manager;

import com.yichen.project.common.ErrorCode;
import com.yichen.project.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Component
public class AIManager {
    // 我发现了一个 AI 对话助手，点击链接进行聊天：https://www.yucongming.com/model/1811774637469532161?inviteUser=1813174634224222209
    @Resource
    private YuCongMingClient yuCongMingClient;
    public String doChat(String message) {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(1811774637469532161L);
        devChatRequest.setMessage(message);

        BaseResponse<DevChatResponse> AiResponse = yuCongMingClient.doChat(devChatRequest);
        if (AiResponse == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI响应错误");
        }
        return AiResponse.getData().getContent();
    }
}
