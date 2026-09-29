package com.ncepuljxx.hmdp.aiservice.tools;

import com.ncepuljxx.hmdp.entity.Shop;
import com.ncepuljxx.hmdp.service.IShopService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShopTool {

    @Autowired
    private IShopService shopService;

    //1.工具方法: 查询商家信息(支持关键词模糊查询)
    @Tool("根据商家名称或关键词模糊查询商家信息,返回商家列表")
    public List<Shop> findShop(@P("商家名称或关键词") String shopName) {
        return shopService.findShopsByKeyword(shopName);
    }

}
