#生成key
ssh-keygen -t rsa -C "peixere"
# gateway

# 注册配置中心 nacos

# 用户授权
>Y 用户登录<br>
>Y 用户退出<br>
>Y 未登录请求<br>
>Y 微服务会话同步<br>
>Y 会话保存<br>
>Y 会话过期刷新<br>
>N 修改密码<br>
>N 忘记密码<br>

# 用户鉴权
>Y 用户权限控制<br>
>Y 用户权限缓存<br>

# 数据库
>Y 读写分离(flux框架原因，事务处理要移动dao，后续再考虑优化)<br>
>Y 表基础数据封装<br>
>Y CRUD 基础封装<br>
>Y 乐观锁封装<br>
>Y 分页参数封装<br>
>Y 统一异常处理<br>


# 动态路由配置
>Y 配置中心实现<br>
>N 缓存实现<br>

# 用户权限管理

# 功能菜单管理

# 接口资源管理

# 租户管理


