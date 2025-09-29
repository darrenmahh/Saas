package org.getoffer.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.getoffer.shortlink.project.dao.entity.ShortLinkDO;
import org.getoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.getoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;

public interface ShortLinkService extends IService<ShortLinkDO> {


    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO reqDTO);
}
