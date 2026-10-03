package com.ncepuljxx.hmdp.controller;

import com.ncepuljxx.hmdp.aiservice.ConsultantService;
import com.ncepuljxx.hmdp.dto.ChatMessageVO;
import com.ncepuljxx.hmdp.dto.ChatSessionSummary;
import com.ncepuljxx.hmdp.dto.Result;
import com.ncepuljxx.hmdp.dto.UserDTO;
import com.ncepuljxx.hmdp.service.IChatHistoryService;
import com.ncepuljxx.hmdp.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Resource
    private ConsultantService consultantService;

    @Resource
    private IChatHistoryService chatHistoryService;

    // AI聊天(流式返回),记忆按当前登录用户+会话隔离,响应结束后自动归档
    @RequestMapping(produces = "text/html;charset=utf-8")
    public Flux<String> chat(String memoryId, String message) {
        // 登录拦截器保证UserHolder有用户
        UserDTO user = UserHolder.getUser();
        String phone = user.getPhone() == null ? "未知" : user.getPhone();
        // 校验会话归属:防止用户伪造memoryId访问他人会话(兼容旧格式user:{id}与新格式user:{id}:{sid})
        if (memoryId == null
                || (!memoryId.equals("user:" + user.getId()) && !memoryId.startsWith("user:" + user.getId() + ":"))) {
            throw new RuntimeException("会话不属于当前用户");
        }
        Flux<String> result = consultantService.chat(memoryId, message, phone);
        // 边流式输出边收集AI完整回复,完成后归档(永不删除)
        StringBuilder aiText = new StringBuilder();
        return result
                .doOnNext(aiText::append)
                .doOnComplete(() -> chatHistoryService.appendExchange(memoryId, message, aiText.toString()));
    }

    // 为当前用户开启一个新对话,返回新会话标识
    @PostMapping("/session/new")
    public Result newSession() {
        UserDTO user = UserHolder.getUser();
        String sessionId = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return Result.ok("user:" + user.getId() + ":" + sessionId);
    }

    // 当前用户的历史会话列表
    @GetMapping("/history/list")
    public Result historyList() {
        return Result.ok(chatHistoryService.listSessions(UserHolder.getUser().getId()));
    }

    // 当前用户某个会话的完整消息
    @GetMapping("/history/{memoryId}")
    public Result history(@PathVariable String memoryId) {
        return Result.ok(chatHistoryService.getMessages(UserHolder.getUser().getId(), memoryId));
    }
}
