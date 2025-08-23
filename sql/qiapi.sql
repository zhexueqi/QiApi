-- 接口信息表
create table if not exists qiapi.`interface_info`
(
    `id` bigint not null auto_increment comment '接口id' primary key,
    `name` varchar(256) not null comment '接口名',
    `description` varchar(256) null comment '接口描述',
    `url` varchar(512) not null comment '接口地址',
    `requestParams` text null comment '请求参数',
    `requestHeader` text null comment '请求头',
    `responseHeader` text null comment '响应头',
    `status` tinyint default 0 not null comment '接口状态 0-关闭 1-开启',
    `method` varchar(256) not null comment '请求类型',
    `userId` bigint not null comment '创建人ID',
    `createTime` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '创建时间',
    `updateTime` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDelete` tinyint default 0 not null comment '用户名'
) comment '接口信息表';


insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (1, '卢绍辉', 'gqu', 'www.devon-glover.biz', 'GxJ', 'Jo', 0, '唐鑫磊', 759, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (2, '丁煜祺', 'TnY1h', 'www.raymon-schultz.info', 'YEdR', 'lGj5Z', 0, '蔡瑞霖', 73051186, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (3, '卢浩轩', 'U21QC', 'www.ricki-paucek.co', 'Ep', 'pCYo', 0, '张胤祥', 9, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (4, '邵晓博', 'AcHP', 'www.donya-tromp.co', 'odqDZ', 'bjwZ', 0, '徐伟泽', 897792, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (5, '姜黎昕', 'HmqF', 'www.sebastian-gislason.biz', 'ti8p', 'US0', 0, '余雪松', 228, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (6, '郑哲瀚', 'xuGY', 'www.hyacinth-okuneva.biz', 'mIVv', 'VT', 0, '孟远航', 397402905, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (7, '范明轩', 'ik', 'www.penelope-little.co', 'g3Y', 'pS4', 0, '方鹏', 29828645, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (8, '石凯瑞', 'wIA8s', 'www.homer-reynolds.net', 'osrDp', 'EZPXv', 0, '贾胤祥', 174556968, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (9, '林修洁', 'Kwgi', 'www.claretta-stiedemann.net', 'NIz', 'Mp', 0, '陆鹭洋', 88, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (10, '高懿轩', 'tuWb', 'www.wilbur-flatley.io', 'RY', 'ZFH', 0, '汪鹏煊', 7990081, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (11, '韦梓晨', 'UTDX6', 'www.mozell-gleichner.info', 'RM', 'OJa', 0, '朱航', 879170, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (12, '傅健雄', 'h3', 'www.julio-crist.biz', 'om', 'HmO6b', 0, '孔语堂', 62613, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (13, '赖绍齐', 'oB', 'www.jerold-kuhn.com', 'cQso', 'P4e', 0, '周思远', 1106890, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (14, '余越泽', 'ycksV', 'www.branden-quitzon.org', 'CwAXG', 'imDsM', 0, '汪致远', 4, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (15, '段嘉懿', 'l8', 'www.shae-boyle.org', 'Z3xEK', 'jTT', 0, '秦聪健', 8, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (16, '蔡思淼', 'Zc3', 'www.filiberto-purdy.io', 'StFJ', '8Tb', 0, '白擎宇', 848, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (17, '秦立辉', 'h2wgq', 'www.timothy-hickle.net', 'WbvB3', 'u3EXE', 0, '方熠彤', 70412, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (18, '叶语堂', '6GX', 'www.melody-parisian.info', 'xSCP', 'rqgZ', 0, '严修杰', 791, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (19, '徐鑫磊', 'sg', 'www.particia-schulist.com', '2t5W', 'Z5', 0, '蔡正豪', 879296040, 0);
insert into qiapi.`interface_info` (`id`, `name`, `description`, `url`, `requestHeader`, `responseHeader`, `status`, `method`, `userId`, `isDelete`) values (20, '丁凯瑞', 'CNUO', 'www.merle-tromp.biz', '1HGq', '8T3m', 0, '林弘文', 41, 0);