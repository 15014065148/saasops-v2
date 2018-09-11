INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (1, 0, '系统管理', null, null, 0, 'iconfont icon-systemManage', 8);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (2, 65, '用户管理', '/main/systemManage/roleManage', null, 1, 'fa fa-user', 2);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (3, 65, '角色权限', '/main/systemManage/roleAuth', null, 1, 'fa fa-user-secret', 1);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (4, 69, '流程发起', '/main/flowManage/createFlow', null, 1, 'fa fa-th-list', 1);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (15, 2, '查看', null, 'sys:user:list,sys:user:info', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (16, 2, '新增', null, 'sys:user:save,sys:role:select', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (17, 2, '修改', null, 'sys:user:update,sys:role:select', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (18, 2, '删除', null, 'sys:user:delete', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (19, 3, '查看', null, 'sys:role:list,sys:role:info', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (20, 3, '新增', null, 'sys:role:save,sys:menu:perms', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (21, 3, '修改', null, 'sys:role:update,sys:menu:perms', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (22, 3, '删除', null, 'sys:role:delete', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (23, 4, '查看', null, 'sys:menu:list,sys:menu:info', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (24, 4, '新增', null, 'sys:menu:save,sys:menu:select', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (25, 4, '修改', null, 'sys:menu:update,sys:menu:select', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (26, 4, '删除', null, 'sys:menu:delete', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (51, 0, '会员管理', '', null, 0, 'iconfont icon-memberManage', 3);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (52, 0, '代理管理', '', null, 0, 'iconfont icon-agentManage', 4);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (53, 0, '资金管理', '', null, 0, 'iconfont icon-wageManage', 5);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (54, 0, '运营管理', '', null, 0, 'iconfont icon-activityManage', 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (55, 0, '经营分析', '', null, 0, 'iconfont icon-businessAnalyze', 7);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (59, 1, '基本设置', null, null, 0, '', 1);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (65, 1, '权限设置', null, null, 0, '', 2);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (69, 0, '流程管理', null, null, 0, 'iconfont icon-agentManage', 2);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (70, 53, '线上入款', '/main/wageManage/onlineDeposit', null, 1, 'fa fa-file-code-o', 1);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (71, 70, '查看', null, 'fund:onLine:list,fund:onLine:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (73, 70, '修改', null, 'fund:onLine:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (80, 59, '系统设置', '/main/systemManage/systemSetting', null, 1, 'fa fa-file-code-o', 1);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (81, 80, '查看', null, 'setting:syssetting:list,setting:syssetting:info', 2, '', 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (82, 80, '新增', null, 'setting:syssetting:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (83, 80, '修改', null, 'setting:syssetting:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (84, 80, '删除', null, 'setting:syssetting:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (85, 59, '域名管理', '/main/systemManage/domainManage', null, 1, 'fa fa-file-code-o', 2);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (86, 85, '查看', null, 'system:systemdomain:list,system:systemdomain:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (87, 85, '新增', null, 'system:systemdomain:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (88, 85, '修改', null, 'system:systemdomain:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (89, 85, '删除', null, 'system:systemdomain:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (91, 51, '会员组', '/main/memberManage/memberGroup', null, 1, 'fa fa-file-code-o', 2);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (92, 91, '会员组-查看', null, 'member:mbrgroup:list,member:mbrgroup:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (93, 91, '会员组-新增', null, 'member:mbrgroup:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (94, 91, '会员组-修改', null, 'member:mbrgroup:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (95, 91, '会员组-删除', null, 'member:mbrgroup:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (96, 91, '取款条件-查看', null, 'member:mbrwithdrawalcond:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (97, 91, '取款条件-新增', null, 'member:mbrwithdrawalcond:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (98, 91, '取款条件-修改', null, 'member:mbrwithdrawalcond:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (99, 91, '存款条件-查看', null, 'member:mbrdepositcond:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (100, 91, '存款条件-新增', null, 'member:mbrdepositcond:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (101, 91, '存款条件-修改', null, 'member:mbrdepositcond:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (102, 53, '公司入款', '/main/wageManage/companyDeposit', null, 1, 'fa fa-file-code-o', 2);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (103, 102, '查看', null, 'fund:company:list,fund:company:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (104, 102, '修改', null, 'fund:company:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (105, 70, '导出', null, 'fund:company:exportExecl', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (106, 51, '会员列表', '/main/memberManage/memberList', null, 1, 'fa fa-file-code-o', 1);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (107, 106, '会员账号-查看', null, 'member:mbraccount:list,member:mbraccount:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (108, 106, '会员账号-新增', null, 'member:mbraccount:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (109, 106, '会员账号-修改', null, 'member:mbraccount:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (110, 106, '会员账号-删除', '', 'member:mbraccount:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (112, 111, '查看', null, 'log:logmbrlogin:list,log:logmbrlogin:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (113, 111, '新增', null, 'log:logmbrlogin:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (114, 111, '修改', null, 'log:logmbrlogin:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (115, 111, '删除', null, 'log:logmbrlogin:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (117, 116, '查看', null, 'log:logmbrregister:list,log:logmbrregister:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (118, 116, '新增', null, 'log:logmbrregister:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (119, 116, '修改', null, 'log:logmbrregister:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (120, 116, '删除', null, 'log:logmbrregister:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (122, 121, '查看', null, 'log:logsystem:list,log:logsystem:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (123, 121, '新增', null, 'log:logsystem:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (124, 121, '修改', null, 'log:logsystem:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (125, 121, '删除', null, 'log:logsystem:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (128, 106, '会员银行卡-查看', null, 'member:mbrbankcard:list,member:mbrbankcard:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (129, 106, '会员银行卡-新增', null, 'member:mbrbankcard:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (130, 106, '会员银行卡-修改', null, 'member:mbrbankcard:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (131, 106, '会员银行卡-删除', null, 'member:mbrbankcard:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (138, 106, '会员备注-查看', null, 'member:mbrmemo:list,member:mbrmemo:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (139, 106, '会员备注-新增', null, 'member:mbrmemo:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (140, 106, '会员备注-修改', null, 'member:mbrmemo:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (141, 106, '会员备注-删除', null, 'member:mbrmemo:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (143, 106, '会员钱包-查看', null, 'member:mbrwallet:list,member:mbrwallet:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (144, 106, '会员钱包-新增', null, 'member:mbrwallet:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (145, 106, '会员钱包-资金操作', null, 'member:mbrwallet:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (146, 106, '会员钱包-删除', null, 'member:mbrwallet:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (147, 53, '会员提款', '/main/wageManage/memberWithdraw', '', 1, 'fa fa-file-code-o', 3);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (148, 147, '查看', null, 'fund:accWithdraw:list,fund:accWithdraw:info', null, null, null);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (149, 147, '修改', null, 'fund:accWithdraw:update', null, null, null);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (150, 147, '导出', null, 'fund:accWithdraw:exportExecl', null, null, null);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (151, 52, '佣金方案', '/main/agentManage/commissionPlan', null, 1, null, 1);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (152, 151, '查看', null, 'agent:commission:list,agent:commission:info', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (153, 151, '修改', null, 'agent:commission:update', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (154, 151, '删除', null, 'agent:commission:delete', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (155, 151, '导出', null, 'agent:commission:exportExecl', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (156, 151, '新增', null, 'agent:commission:save', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (157, 151, '费用分摊比例设置', null, 'agent:chargeRate:all', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (158, 52, '总代设置', '/main/agentManage/mainAgentSetting', null, 1, null, 2);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (159, 158, '查看', null, 'agent:account:list,agent:account:info', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (160, 158, '新增', null, 'agent:account:save', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (161, 158, '修改', null, 'agent:account:update', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (162, 158, '删除', null, 'agent:account:delete', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (163, 158, '导出', null, 'agent:account:exportExecl', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (164, 52, '代理设置', '/main/agentManage/agentSetting', null, 1, null, 3);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (165, 164, '查看', null, 'agent:subordinate:list,agent:subordinate:info', null, null, null);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (166, 164, '新增', null, 'agent:subordinate:save', null, null, null);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (167, 164, '修改', null, 'agent:subordinate:update', null, null, null);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (168, 164, '删除', null, 'agent:subordinate:delete', null, null, null);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (169, 164, '导出', null, 'agent:subordinate:exportExecl', null, null, null);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (170, 59, '信息模板', '/main/systemManage/messageTemplate', null, 1, 'fa fa-file-code-o', 3);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (171, 170, '查看', null, 'msgtemple:msgtemple:list,msgtemple:msgtemple:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (172, 170, '新增', null, 'msgtemple:msgtemple:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (173, 170, '修改', null, 'msgtemple:msgtemple:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (174, 170, '删除', null, 'msgtemple:msgtemple:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (185, 53, '代理提款', '/main/wageManage/agentWithdraw', null, 1, 'fa fa-file-code-o', 4);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (186, 185, '查看', null, 'fund:agyWithdraw:list,fund:agyWithdraw:info', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (187, 185, '新增', null, 'fund:agyWithdraw:save', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (188, 185, '修改', null, 'fund:agyWithdraw:update', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (189, 185, '删除', null, 'fund:agyWithdraw:delete', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (190, 185, '导出', null, 'fund:agyWithdraw:exportExecl', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (191, 180, '导出', null, 'setting:sysdeposit:exportExcel', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (192, 54, '入款管理', '/main/operateManage/companyDepositSetting', null, 1, 'fa fa-file-code-o', 2);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (193, 192, '查看', null, 'setting:sysdeposit:list,setting:sysdeposit:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (194, 192, '新增', null, 'setting:sysdeposit:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (195, 192, '修改', null, 'setting:sysdeposit:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (196, 192, '删除', null, 'setting:sysdeposit:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (197, 192, '导出报表', null, 'setting:sysdeposit:export', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (198, 53, '转账报表', '/main/wageManage/transformSheet', null, 1, 'fa fa-file-code-o', 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (199, 198, '查看', null, 'fund:billReport:list,fund:billReport:info', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (200, 198, '新增', null, 'fund:billReport:save', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (201, 198, '修改', null, 'fund:billReport:update', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (202, 198, '删除', null, 'fund:billReport:delete', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (203, 198, '导出', null, 'fund:billReport:exportExecl', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (209, 53, '调整报表', '/main/wageManage/adjustSheet', null, 1, 'fa fa-file-code-o', 5);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (210, 209, '查看', null, 'fund:audit:list,fund:audit:info', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (211, 209, '新增', null, 'fund:audit:save', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (212, 209, '修改', null, 'fund:audit:update', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (213, 209, '删除', null, 'fund:audit:delete', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (214, 209, '导出', null, 'fund:audit:exportExecl', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (215, 54, '线上支付', '/main/operateManage/onlinePay', null, 1, 'fa fa-file-code-o', 1);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (216, 215, '查看', null, 'onlinepay:setbaciconlinepay:list,onlinepay:setbaciconlinepay:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (217, 215, '新增', null, 'onlinepay:setbaciconlinepay:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (218, 215, '修改', null, 'onlinepay:setbaciconlinepay:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (219, 215, '删除', null, 'onlinepay:setbaciconlinepay:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (220, 106, '重置登陆密码', '', 'member:mbraccount:pwdUpdate', 2, '', 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (221, 106, '重置安全密码', '', 'member:mbraccount:secPwdUpdate', 2, '', 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (222, 106, '同步游戏密码', '', 'member:mbraccount:syncPwdUpdate', 2, '', 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (223, 106, '会员代理修改', '', 'member:mbraccount:agentUpdate', 2, '', 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (224, 106, '会员组修改', '', 'member:mbraccount:groupIdUpdate', 2, '', 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (225, 106, '会员组状态修改', '', 'member:mbraccount:avlUpdate', 2, '', 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (227, 51, '在线会员', '/main/memberManage/onlineMember', '', 1, 'fa fa-file-code-o', 3);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (228, 227, '查看', '', 'member:mbraccount:listOnline', 2, '', 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (229, 70, '导出', null, 'fund:onLine:exportExecl', 2, null, 0);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (230, 55, '运营报表', '/main/operateAnalyze/runSheet', '', 1, 'fa fa-file-code-o', 5);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (231, 54, '游戏列表', '/main/operateManage/gameList', null, 1, 'fa fa-file-code-o', 5);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (232, 231, '查看', null, 'operate:tgmcat:gameList,operate:tgmcat:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (233, 54, '公告通知', '/main/operateManage/noticeMessage', null, 1, 'fa fa-file-code-o', 7);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (234, 233, '查看', null, 'operate:oprnotice:list,operate:oprnotice:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (235, 233, '新增', null, 'operate:oprnotice:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (236, 233, '修改', null, 'operate:oprnotice:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (237, 233, '删除', null, 'operate:oprnotice:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (238, 54, '站内信', '/main/operateManage/memberMessage', null, 1, 'fa fa-file-code-o', 8);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (239, 238, '查看', null, 'operate:oprmsg:list,operate:oprmsg:info,operate:oprrecmbr:list', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (240, 238, '新增', null, 'operate:oprmsg:save,operate:oprrecmbr:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (241, 238, '修改', null, 'operate:oprmsg:update,operate:oprrecmbr:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (242, 238, '删除', null, 'operate:oprmsg:delete,operate:oprrecmbr:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (243, 54, '活动设置', '/main/operateManage/activitySetting', null, 1, 'fa fa-file-code-o', 4);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (244, 243, '查看', null, 'operate:activity:list,operate:activity:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (245, 243, '新增', null, 'operate:activity:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (246, 243, '修改', null, 'operate:activity:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (247, 243, '删除', null, 'operate:activity:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (248, 231, '导出', null, 'operate:activity:exportExcel', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (255, 254, '查看', null, 'operate:oprrecmbr:list,operate:oprrecmbr:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (256, 254, '新增', null, 'operate:oprrecmbr:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (257, 254, '修改', null, 'operate:oprrecmbr:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (258, 254, '删除', null, 'operate:oprrecmbr:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (259, 254, '导出', null, 'operate:oprrecmbr:exportExcel', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (260, 231, '操作', null, 'operate:tgmcat:updateOrExport', 2, '2', null);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (261, 54, '广告管理', '/main/operateManage/adManage', null, 1, 'fa fa-file-code-o', 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (262, 261, '查看', null, 'operate:opradv:list,operate:opradv:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (263, 261, '新增', null, 'operate:opradv:save', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (264, 261, '修改', null, 'operate:opradv:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (265, 261, '删除', null, 'operate:opradv:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (267, 91, '即时稽核', null, 'member:audit:list', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (268, 91, '修改稽核', null, 'member:audit:update', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (269, 91, '清除稽核', null, 'member:audit:clear', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (270, 243, '活动审核列表', null, 'operate:activity:activityAuditList', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (271, 243, '活动审核', null, 'operate:activity:activityAudit', 2, null, null);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (273, 55, '活动报表', '/main/operateAnalyze/activitySheet', null, 1, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (274, 273, '查看', null, 'operate:activity:findBonusReportList,operate:activity:findBonusReport', 2, null, 1);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (287, 52, '佣金结算', '/main/agentManage/commissionClean', null, 1, null, 4);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (288, 55, '经营报表', '/main/operateAnalyze/operateSheet', null, 1, null, 4);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (289, 55, '投注记录', '/main/operateAnalyze/betRecord', null, 1, null, 7);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (290, 55, '财务费用', '/main/operateAnalyze/financeCost', null, 1, null, 8);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (291, 55, '余额报表', '/main/operateAnalyze/balanceSheet', null, 1, null, 9);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (293, 69, '我的申请', '/main/flowManage/myApplication', null, 1, null, 2);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (294, 69, '代办流程', '/main/flowManage/todoFlow', null, 1, null, 3);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (295, 69, '代办事物', '/main/flowManage/todoThings', null, 1, null, 4);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (296, 55, '输赢报表', '/main/operateAnalyze/loseWinSheet', null, 1, null, 1);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (297, 55, '红利报表', '/main/operateAnalyze/dividendSheet', null, 1, null, 2);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (298, 55, '游戏数据', '/main/operateAnalyze/gameDataSheet', null, 1, null, 3);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (300, 106, '会员账号-查询员白名单信息', null, 'member:fundWhiteList:info', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (301, 106, '会员账号-新增会员白名单', null, 'member:fundWhiteList:add', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (302, 106, '会员账号-移除会员白名单', null, 'member:fundWhiteList:delete', 2, null, 6);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (303, 54, '出款管理', '/main/operateManage/withdrawManage', null, 1, 'fa fa-file-code-o', 3);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (304, 303, '查看', null, 'merchant:fundMerchantPay:list,merchant:fundMerchantPay:info', 2, null, 1);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (305, 303, '新增', null, 'merchant:fundMerchantPay:save', 2, null, 2);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (306, 303, '修改', null, 'merchant:fundMerchantPay:update', 2, null, 3);
INSERT INTO saasops_test.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (307, 303, '删除', null, 'merchant:fundMerchantPay:delete', 2, null, 4);
INSERT INTO saasops_a001.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (309, 297, '查看', null, 'analysis:betDetails:finalBetDetailsAll', 2, null, 1);
INSERT INTO saasops_a001.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (310, 298, '查看', null, 'analysis:betDetails:finalBetDetailsAll', 2, null, 1);
INSERT INTO saasops_a001.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (311, 296, '查看', null, 'analysis:betDetails:finalBetDetailsAll', 2, null, 1);
INSERT INTO saasops_a001.sys_menu (menu_id, parent_id, name, url, perms, type, icon, order_num) VALUES (312, 289, '查看', null, 'analysis:betDetails:finalBetDetailsAll', 2, null, 1);