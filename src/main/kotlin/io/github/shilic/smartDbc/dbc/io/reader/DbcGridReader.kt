package io.github.shilic.smartDbc.dbc.io.reader

import io.github.shilic.smartDbc.dbc.dataModel.models.*
import io.github.shilic.smartGrid.core.*
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook

/**
 * DBC协议解析器。通过DBC的excel协议文件, 解析为DBC对象。
 */
class DbcGridReader(private val workbook: Workbook) {
    /** 获取可编辑的整车协议 Protocol 。
     *
     * 需要在excel表格中使用 'CanProtocol_Info' 和 'DbcList' 标注需要解析的协议sheet。
     * */
    fun createMutableProtocolMap(): MutableMap<String, CanProtocolImp> = GridReader(workbook).read(CanProtocolImp::class)
    /** 从多个DBC sheet 页面, 获取多个可编辑的CanDbc;
     *
     * 需要在excel表格中使用 'DbcList' 标注需要解析的协议sheet。
     * */
    fun createMutableDbcMap(): MutableMap<String, CanDbcImp> = GridReader(workbook).read(CanDbcImp::class)
    /** 从单个DBC sheet 页面, 获取可编辑的CanDbc;
     *
     * 使用此方法可以将任意的协议页面解析成
     * */
    fun createMutableDbc(sheetName: String, dbcBaseInfo : DbcBaseInfo): CanDbcImp {
        val sheet: Sheet = workbook.getSheet(sheetName) ?: error("没有找到名为 '${sheetName}' 的DBC协议")
        val rowIndex: Ref<Int> = Ref(0)
        // 使用 smart-grid 组件, 从名为 sheetName 的 sheet 读取 CanMessage
        val canMessages = GridReader(workbook).readBySheet(sheet, CanMessageImp::class, GridSheetType.Dictionary, rowIndex, null)
        return CanDbcImp().apply {
            setDbcBaseInfo(dbcBaseInfo)
            msgMap = canMessages
        }
    }
}