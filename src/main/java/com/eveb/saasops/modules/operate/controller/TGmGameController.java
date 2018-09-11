package com.eveb.saasops.modules.operate.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.eveb.saasops.modules.operate.entity.TGameLogo;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.eveb.saasops.common.utils.R;
import com.eveb.saasops.modules.operate.entity.TGmDepot;
import com.eveb.saasops.modules.operate.entity.TGmGame;
import com.eveb.saasops.modules.operate.service.TGmGameService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/bkapi/operate/tgmgame")
@Api(value = "TGmGame", description = "")
public class TGmGameController {
    @Autowired
    private TGmGameService tGmGameService;

    @GetMapping("/info/{id}")
    @RequiresPermissions("operate:tgmgame:info")
    @ApiOperation(value = "信息", notes = "信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R info(@PathVariable("id") Integer id) {
        TGmGame tGmGame = tGmGameService.queryObjectOne(id);
        return R.ok().put("tGmGame", tGmGame);
    }

    @PostMapping("/available")
    @RequiresPermissions("operate:tgmcat:updateOrExport")
    @ApiOperation(value = "修改", notes = "修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R available(@RequestBody TGmGame tGmGame) {
        TGmGame record = new TGmGame();
        record.setId(tGmGame.getId());
        record.setAvailable(tGmGame.getAvailable());
        tGmGameService.update(record);
        return R.ok();
    }

    @PostMapping("/enablePc")
    @RequiresPermissions("operate:tgmcat:updateOrExport")
    @ApiOperation(value = "修改", notes = "修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R enablePc(@RequestBody TGmGame tGmGame) {
        TGmGame record = new TGmGame();
        record.setId(tGmGame.getId());
        record.setEnablePc(tGmGame.getEnablePc());
        tGmGameService.update(record);
        return R.ok();
    }

    @PostMapping("/enableMb")
    @RequiresPermissions("operate:tgmcat:updateOrExport")
    @ApiOperation(value = "修改", notes = "修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R enableMb(@RequestBody TGmGame tGmGame) {
        TGmGame record = new TGmGame();
        record.setId(tGmGame.getId());
        record.setEnableMb(tGmGame.getEnableMb());
        tGmGameService.update(record);
        return R.ok();
    }

    @PostMapping("/enableTest")
    @RequiresPermissions("operate:tgmcat:updateOrExport")
    @ApiOperation(value = "修改", notes = "修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R enableTest(@RequestBody TGmGame tGmGame) {
        TGmGame record = new TGmGame();
        record.setId(tGmGame.getId());
        record.setEnableTest(tGmGame.getEnableTest());
        tGmGameService.update(record);
        return R.ok();
    }

    @GetMapping("/gameCatList")
    @RequiresPermissions("operate:tgmcat:gameList")
    @ApiOperation(value = "游戏分类列表", notes = "游戏分类列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R gameCatList(@ModelAttribute TGmDepot tGmDepot, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, String orderBy) {
        return R.ok().put("page", tGmGameService.queryGmCatList(tGmDepot, pageNo, pageSize, orderBy));
    }

    @GetMapping("/exportGameCatExcel")
    @RequiresPermissions("operate:tgmcat:updateOrExport")
    @ApiOperation(value = "导出游戏分类列表", notes = "导出游戏分类列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void exportGameCatExcel(@ModelAttribute TGmDepot tGmDepot, HttpServletResponse response) {
        tGmGameService.exportGameCatExcel(tGmDepot, response);
    }

    @GetMapping("/gameList")
    @RequiresPermissions("operate:tgmcat:gameList")
    @ApiOperation(value = "游戏列表", notes = "游戏列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R gameList(@ModelAttribute TGmGame tGmGame, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize, String orderBy) {
        return R.ok().put("page", tGmGameService.queryTGmGameList(tGmGame, pageNo, pageSize, orderBy));
    }

    @GetMapping("/exportGameExcel")
    @RequiresPermissions("operate:tgmcat:updateOrExport")
    @ApiOperation(value = "导出游戏列表", notes = "导出游戏列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public void exportGameExcel(@ModelAttribute TGmGame tGmGame, HttpServletResponse response) {
        tGmGameService.exportGameExcel(tGmGame, response);
    }

    @GetMapping("/listGame")
    @ApiOperation(value = "查询分类下平台信息（统计）", notes = "查询分类下平台信息（统计）")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R listGame(@ModelAttribute TGameLogo tGameLogo, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put("page", tGmGameService.queryListGamePage(tGameLogo, pageNo, pageSize));
    }

    @GetMapping("/findGameList")
    @ApiOperation(value = "根据平台查询游戏列表", notes = "根据平台查询游戏列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R findGameList(@ModelAttribute TGmGame tGmGame, @RequestParam("pageNo") @NotNull Integer pageNo, @RequestParam("pageSize") @NotNull Integer pageSize) {
        return R.ok().put("page", tGmGameService.findGameList(tGmGame, pageNo, pageSize));
    }

    @PostMapping("/update")
    @ApiOperation(value = "单个游戏修改", notes = "单个游戏修改")
    @ApiImplicitParams({@ApiImplicitParam(name = "token", value = "token头部，随便填数字", required = true, dataType = "Integer", paramType = "header")})
    public R update(@RequestBody TGmGame tGmGame) {
        tGmGameService.update(tGmGame);
        return R.ok();
    }

}
