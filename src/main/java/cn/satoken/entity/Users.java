package cn.satoken.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author
 * @since 2025-05-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class Users implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    /**
     * 用户名
     */
    @NotBlank
    @Size(min = 6, max = 16)
    private String username;

    /**
     * 密码
     */
    //@NotBlank 不加notblank, 这样这个值没传不会检测@Size(min = 6, max = 16)
    // 但空字符串会检测长度，可以把@size去掉，在改密码的时候手动校验
    @Size(min = 6, max = 16)
    @JsonIgnore
    private String password;

    private LocalDateTime birthday;

    @TableField(exist = false)
    private String token;

    @TableField(exist = false)
    private List<String> roles;
    @TableField(exist = false)
    private List<String> permissions;

}

