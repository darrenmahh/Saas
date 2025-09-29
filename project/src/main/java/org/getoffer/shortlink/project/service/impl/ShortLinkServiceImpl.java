package org.getoffer.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.getoffer.shortlink.project.dao.entity.ShortLinkDO;
import org.getoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import org.getoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.getoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.getoffer.shortlink.project.service.ShortLinkService;
import org.getoffer.shortlink.project.toolkit.HashUtil;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO reqDTO) {
        String shortLinkSuffix = generateSuffix(reqDTO);
        ShortLinkDO shortLinkDO = BeanUtil.toBean(reqDTO, ShortLinkDO.class);
        shortLinkDO.setShortUri(shortLinkSuffix);
        shortLinkDO.setEnableStatus(0);
        shortLinkDO.setFullShortUrl(reqDTO.getDomain() + "/" + shortLinkSuffix);
        baseMapper.insert(shortLinkDO);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(reqDTO.getOriginUrl())
                .gid(reqDTO.getGid())
                .build();
    }

    private String generateSuffix(ShortLinkCreateReqDTO reqDTO) {
        String originUrl = reqDTO.getOriginUrl();
        return HashUtil.hashToBase62(originUrl);
    }
}
