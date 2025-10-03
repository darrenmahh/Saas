package org.getoffer.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.getoffer.shortlink.admin.common.databases.BaseDO;

@Data
@TableName("t_group")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDO extends BaseDO {
    /** ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 分组标识 */
    private String gid;

    /** 分组名称 */
    private String name;

    /** 分组排序 */
    private Integer sortOrder;

}
