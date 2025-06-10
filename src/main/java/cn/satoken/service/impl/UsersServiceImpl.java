package cn.satoken.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.satoken.entity.Roles;
import cn.satoken.entity.UserRole;
import cn.satoken.entity.Users;
import cn.satoken.mapper.UserRoleMapper;
import cn.satoken.mapper.UsersMapper;
import cn.satoken.service.IRolesService;
import cn.satoken.service.IUserRoleService;
import cn.satoken.service.IUsersService;
import cn.satoken.util.CaptchaController;
import cn.satoken.util.Login;
import cn.satoken.util.MyException;
import cn.satoken.util.Result;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author
 * @since 2025-05-29
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {
    @Autowired
    private CaptchaController captchaController;
    @Autowired
    private IRolesService rolesService;
    @Autowired
    private IUserRoleService userRoleService;
    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public Result register(Login login) {
        // 校验验证码
        captchaController.verifyCaptcha(login.getCaptchaId(), login.getCaptcha());
        //用户名是否存在
        if (lambdaQuery().eq(Users::getUsername, login.getUsername()).exists()) {
            throw new MyException("用户名已存在");
        }
        //注册用户
        Users user = Users.builder().username(login.getUsername())
                .password(BCrypt.hashpw(login.getPassword()))
                .birthday(login.getBirthday())
                .build();
        save(user);
        //保存用户-角色信息
        userRoleService.save(UserRole.builder()
                .userId(user.getId())
                .roleId(rolesService.lambdaQuery().eq(Roles::getRole, "user").one().getId())
                .build());
        return Result.ok();
    }

    @Override
    public Result login(Login login) {
        // 校验验证码
        captchaController.verifyCaptcha(login.getCaptchaId(), login.getCaptcha());
        Users user = lambdaQuery().eq(Users::getUsername, login.getUsername()).one();
        if (ObjUtil.isEmpty(user)) {
            throw new MyException("用户名不存在");
        }
        List<String> roles = userRoleMapper.getRolesByUserId(user.getId());
        List<String> permissions = userRoleMapper.getPermissionsByUserId(user.getId());
        //校验用户名密码
        if (BCrypt.checkpw(login.getPassword(), user.getPassword())) {
            StpUtil.login(user.getId());
            SaSession session = StpUtil.getSession();
            session.set("user", user);
            session.set("password", user.getPassword());
            session.set("roles", roles);
            session.set("permissions", permissions);
            return Result.data(user.setToken(StpUtil.getTokenValue())
                    .setRoles(roles).setPermissions(permissions));
        }
        throw new MyException("密码错误");
    }

    @Override
    public Result openSafe(String password) {
        if (BCrypt.checkpw(password, (String) StpUtil.getSession().get("password"))) {
            // 比对成功，为当前会话打开二级认证，有效期为120秒
            StpUtil.openSafe(120);
            return Result.ok();
        }
        // 如果密码校验失败，则二级认证也会失败
        throw new MyException("二级校验失败");
    }
}
