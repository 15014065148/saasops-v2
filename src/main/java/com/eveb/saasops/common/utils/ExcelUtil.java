package com.eveb.saasops.common.utils;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import org.apache.poi.ss.usermodel.Workbook;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtil {

    public static Workbook commonExcelExportList(String mapKey,String templatePath, List<Map<String, Object>> list) {
        TemplateExportParams params = new TemplateExportParams(templatePath, true);
        Map<String, Object> map = new HashMap<>();
        map.put(mapKey, list);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Workbook workbook = ExcelExportUtil.exportExcel(params, map);
        try {
            workbook.write(output);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workbook;
    }

    public static void writeExcel(HttpServletResponse response, Workbook work, String fileName) throws IOException {
        OutputStream out = null;
        try {
            out = response.getOutputStream();
            response.setContentType("application/ms-excel;charset=UTF-8");
            response.setHeader("Content-Disposition","attachment;filename=".concat(String.valueOf(URLEncoder.encode(fileName + ".xls", "UTF-8"))));
            work.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }
}
