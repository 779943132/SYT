# SYT
预约挂号统一平台<br>
后端技术:springBoot+Mybatis-plus+SpringCloud+Redis+MabbitMq等技术<br>
持久化技术：mysql+mongodb<br>

>完成功能:<br>
>>service-hosp模块：排班管理，医院信息管理，与医院对接等<br>
>>service-cmn模块：数据字典管理<br>
>>service-user模块：用户管理，就诊人管理，邮件发送等<br>
>>service-order模块：订单管理，生成订单，取消订单等<br>
>>service-task模块：定时就医提醒<br>
>>service-oss模块：用户身份信息上传阿里oss功能<br>
>>service-gateway模块：网关功能，和解决跨域等问题，还有过滤器<br>

前端技术：vue<br>
后台管理：element-ui-admin框架+element-ui组件<br>
前台技术：nuxt框架+element-ui组件<br>

## 前端代码在WEB目录，Utils目录存放的是nacos本体，启动项目前要把nacos启动
## 这个项目已经去除掉了所有敏感信息，如数据库地址密码等，想要运行起来请先使用Utils中的sql把数据库搭建好，和在service-user中设置好邮箱信息
## 再把user模块中mycode中信息进行修改，就差不多能运行了，如果遇见什么问题，可以留言给我
