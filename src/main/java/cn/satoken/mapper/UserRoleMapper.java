package cn.satoken.mapper;

import cn.satoken.entity.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 
 * @since 2025-05-30
 */
public interface UserRoleMapper extends BaseMapper<UserRole> {

    List<String> getRolesByUserId(Long id);

    List<String> getPermissionsByUserId(Long id);
}
