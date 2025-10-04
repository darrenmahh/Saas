package org.getoffer.shortlink.admin.dto;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.getoffer.shortlink.admin.common.convention.result.Result;
import org.getoffer.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import org.getoffer.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import org.getoffer.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import org.getoffer.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import org.getoffer.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ShortLinkRemoteService {

    /**
     * admin端短链接创建
     * @param shortLinkCreateReqDTO 请求实体
     * @return 返回创建短链接信息
     */
    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO shortLinkCreateReqDTO) {
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8003/api/short-link/project/v1/create", JSON.toJSONString(shortLinkCreateReqDTO));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 分页短链接接口
     * @param reqDTO 短链接分页admin端接口
     * @return 返回分页查询结果
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO reqDTO) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid", reqDTO.getGid());
        requestMap.put("current", reqDTO.getCurrent());
        requestMap.put("size", reqDTO.getSize());

        String resultPage = HttpUtil.get("http://127.0.0.1:8003/api/short-link/project/v1/page", requestMap);
        return JSON.parseObject(resultPage, new TypeReference<>() {
        });
    }

    /**
     * 分组数量查询
     * @param requestParam 分组数量查询请求参数  列表
     * @return 返回分组数量查询结果
     */
    default Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(List<String> requestParam) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestParam", requestParam);
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8003/api/short-link/project/v1/count", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<Result<List<ShortLinkGroupCountQueryRespDTO>>>() {
        });
    }
}
