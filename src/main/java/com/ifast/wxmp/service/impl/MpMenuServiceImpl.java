package com.ifast.wxmp.service.impl;

import com.ifast.common.base.CoreServiceImpl;
import com.ifast.common.domain.Tree;
import com.ifast.common.exception.IFastException;
import com.ifast.common.type.EnumErrorCode;
import com.ifast.common.utils.BuildTree;
import com.ifast.wxmp.dao.MpMenuDao;
import com.ifast.wxmp.domain.MpMenuDO;
import com.ifast.wxmp.pojo.type.Const;
import com.ifast.wxmp.service.MpMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 
 * <pre>
 * 微信菜单表
 * </pre>
 * <small> 2018-10-19 15:47:16 | Aron</small>
 */
@Service
@Slf4j
public class MpMenuServiceImpl extends CoreServiceImpl<MpMenuDao, MpMenuDO> implements MpMenuService {

    private static final int MAIN_MENU_SIZE = 3;
    private static final int SUB_MENU_SIZE = 5;
    private static final String TEXT_KEY = "TEXT_";

    @Override
    public Tree<MpMenuDO> getTree() {
        List<Tree<MpMenuDO>> nodes = baseMapper.selectList(null).stream().map(menu -> {
            Tree<MpMenuDO> node = new Tree<>();
            node.setId(menu.getId() + "");
            node.setText(menu.getMenuname());
            node.setParentId(menu.getParentidx() + "");
            Map<String, Object> state = new HashMap<>(16);
            state.put("opened", true);
            node.setState(state);
            return node;
        }).collect(Collectors.toList());

        return BuildTree.build(nodes);
    }

    @Override
    public void saveMenu(MpMenuDO mpMenu) {
        Long parentIdx = mpMenu.getParentidx();
        if(Objects.isNull(parentIdx) || parentIdx.equals(0L)){
            int count = this.selectCount(convertToEntityWrapper("parentidx", parentIdx));
            if(count >= MAIN_MENU_SIZE){
                log.info("主菜单不能超过3个");
                throw new IFastException(EnumErrorCode.wxmpMenuSaveMainError.getCodeStr());
            }
        }else{
            int count = this.selectCount(convertToEntityWrapper("parentidx", parentIdx));
            if(count >= SUB_MENU_SIZE){
                log.info("子菜单不能超过5个");
                throw new IFastException(EnumErrorCode.wxmpMenuSaveSubError.getCodeStr());
            }
        }
        insert(mpMenu);
        if(Const.MenuKey.TEXT.equals(mpMenu.getMenutype())){
            mpMenu.setMenukey(TEXT_KEY + mpMenu.getId());
            updateById(mpMenu);
        }

    }
}
