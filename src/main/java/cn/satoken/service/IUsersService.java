package cn.satoken.service;

import cn.satoken.entity.Users;
import cn.satoken.util.Login;
import cn.satoken.util.Result;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2025-05-29
 */
public interface IUsersService extends IService<Users> {

    Result register(Login login);

    Result login(Login login);

    Result openSafe(String password);
}
