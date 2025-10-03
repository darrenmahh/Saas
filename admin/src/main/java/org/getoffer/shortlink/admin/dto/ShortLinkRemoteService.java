package org.getoffer.shortlink.admin.dto;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.getoffer.shortlink.admin.common.convention.result.Result;
import org.getoffer.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import org.getoffer.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import org.getoffer.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import org.getoffer.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

import java.util.HashMap;
import java.util.Map;

public interface ShortLinkRemoteService {

    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8003/api/short-link/project/v1/create", JSON.toJSONString(shortLinkCreateReqDTO));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO reqDTO) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid", reqDTO.getGid());
        requestMap.put("current", reqDTO.getCurrent());
        requestMap.put("size", reqDTO.getSize());

        String resultPage = HttpUtil.get("http://127.0.0.1:8003/api/short-link/project/v1/page", requestMap);
        return JSON.parseObject(resultPage, new TypeReference<>() {
        });
    }
}
