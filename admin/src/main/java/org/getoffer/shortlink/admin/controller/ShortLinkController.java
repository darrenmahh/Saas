package org.getoffer.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.getoffer.shortlink.admin.common.convention.result.Result;
import org.getoffer.shortlink.admin.common.convention.result.Results;
import org.getoffer.shortlink.admin.dto.ShortLinkRemoteService;
import org.getoffer.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import org.getoffer.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import org.getoffer.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import org.getoffer.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import org.getoffer.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShortLinkController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO reqDTO) {
        return shortLinkRemoteService.createShortLink(reqDTO);
    }

    /**
     * 短链接分页
     * @param requestParam 请求实体
     * @return 返回成功否信息
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return shortLinkRemoteService.pageShortLink(requestParam);
    }

    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO reqDTO) {
        shortLinkRemoteService.updateShortLink(reqDTO);
        return Results.success();
    }
}
