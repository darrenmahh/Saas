package org.getoffer.shortlink.admin.dto.req;

import lombok.Data;

@Data
public class ShortLinkGroupUpdateReqDTO {


    /**
     * gid
     */
    private String gid;
    /**
     * 分组名
     */
    private String name;
}
