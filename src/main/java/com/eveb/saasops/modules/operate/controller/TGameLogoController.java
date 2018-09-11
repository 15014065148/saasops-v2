package com.eveb.saasops.modules.operate.controller;

import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.modules.base.controller.AbstractController;
import com.eveb.saasops.modules.operate.entity.TGameLogo;
import com.eveb.saasops.modules.operate.service.TGameLogoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bkapi/game/tgamelogo")
@Api(value = "TGameLogo", description = "个性图和LOGO")
public class TGameLogoController extends AbstractController {
    @Autowired
    private TGameLogoService tGameLogoService;

    @PostMapping("/update")
    @ApiOperation(value = "修改", notes = "修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody TGameLogo tGameLogo) {
        tGameLogoService.update(tGameLogo);
        return R.ok();
    }
}
