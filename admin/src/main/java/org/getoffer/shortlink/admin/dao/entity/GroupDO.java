package org.getoffer.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("t_group")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDO {
    /** ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 分组标识 */
    private String gid;

    /** 分组名称 */
    private String name;

    /** 创建分组用户名 */
    private String username;

    /** 分组排序 */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 修改时间 */
    private LocalDateTime updateTime;

    /** 删除标识 0：未删除 1：已删除 */
    private Integer delFlag;
}
