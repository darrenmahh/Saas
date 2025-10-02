package org.getoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.getoffer.shortlink.project.dao.entity.ShortLinkDO;
import org.getoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.getoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.getoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.getoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;

public interface ShortLinkService extends IService<ShortLinkDO> {


    /**
     * 创建短链接
     * @param reqDTO 短链接创建前端请求实体
     * @return 返回
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO reqDTO);

    /**
     * 短链接分页实现
     * @param reqDTO 分页请求实体
     * @return 返回信息
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO reqDTO);
}
