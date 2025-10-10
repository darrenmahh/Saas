package org.getoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.getoffer.shortlink.project.dao.entity.ShortLinkDO;
import org.getoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.getoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.getoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import org.getoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.getoffer.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import org.getoffer.shortlink.project.dto.resp.ShortLinkPageRespDTO;

import java.util.List;

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

    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);

    void updateShortLink(ShortLinkUpdateReqDTO reqDTO);

    /**
     * 短链接跳转
     * @param shortUri 短链接后缀
     * @param request Http 请求
     * @param response Http 响应
     */
    void restoreUrl(String shortUri, ServletRequest request, ServletResponse response);
}
