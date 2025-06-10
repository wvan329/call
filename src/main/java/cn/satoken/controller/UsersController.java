package cn.satoken.controller;


import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaCheckSafe;
import cn.satoken.service.IUsersService;
import cn.satoken.util.Login;
import cn.satoken.util.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author
 * @since 2025-05-29
 */
@Validated
@RestController
@RequestMapping
public class UsersController {
    @Autowired
    private IUsersService usersService;


    // 注册
    @PostMapping("/register")
    public Result register(@Valid @RequestBody Login login) {
        return usersService.register(login);
    }

    // 登录
    @PostMapping("/login")
    public Result login(@Valid @RequestBody Login login) {
        return usersService.login(login);
    }

    // 开启二级认证
    @GetMapping("/openSafe")
    public Result openSafe(String password) {
        return usersService.openSafe(password);
    }



    // 测试二级认证
    @SaCheckSafe
    @PostMapping("/important")
    public Result important() {
        return Result.ok();
    }

    // 测试权限
    @GetMapping("/role")
    @SaCheckRole("user")
    public Result role() {
        return Result.ok();
    }
}
