package cn.satoken.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义权限认证接口扩展，Sa-Token 将从此实现类获取每个账号拥有的权限码 
 * 
 * @author click33
 * @since 2022-10-13
 */
@Component	// 打开此注解，保证此类被springboot扫描，即可完成sa-token的自定义权限验证扩展 
public class StpInterfaceImpl implements StpInterface {

	/**
	 * 返回一个账号所拥有的权限码集合 
	 */
	@Override
	public List<String> getPermissionList(Object loginId, String loginType) {
		return (List<String>) StpUtil.getSession().get("permissions");
	}

	/**
	 * 返回一个账号所拥有的角色标识集合
	 */
	@Override
	public List<String> getRoleList(Object loginId, String loginType) {
		return (List<String>) StpUtil.getSession().get("roles");
	}

}
