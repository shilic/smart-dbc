package io.github.shilic.smartDbc.dbc.io.reader

import io.github.shilic.smartDbc.dbc.dataModel.models.*
import io.github.shilic.smartGrid.core.*
import io.github.shilic.smartGrid.utils.*
import org.apache.poi.ss.usermodel.*
import java.io.File
import java.io.InputStream

// TODO ： 需要完善excel表对枚举类型的适配，现在excel表识别自定义属性完全依赖于枚举，无法序列化成DBC文件中自定义属性。
/** DBC协议解析器。通过DBC的excel协议文件, 解析为DBC对象。 */
class DbcGridReader(val workbook: Workbook) {
    companion object {
        operator fun invoke(filePath: String) = DbcGridReader(filePath.workbook())
        operator fun invoke(file: File) = DbcGridReader(file.workbook())
        operator fun invoke(inputStream: InputStream) = DbcGridReader(inputStream.workbook())
        operator fun invoke(provider: () -> InputStream) = DbcGridReader(provider().workbook())
    }
    /** 获取可编辑的整车协议 [CanProtocolImp] 。
     *
     * 需要在excel表格中使用 'CanProtocol_Info' 和 'DbcList' 标注需要解析的协议sheet。
     * */
    fun readProtocol(): CanProtocolImp =
        GridReader(workbook).read(CanProtocolImp::class).values.first().also { result ->
            result.dbcMap.values.forEach { it.propagateLongIdCode() }
        }
    /** 从多个DBC sheet 页面, 获取多个可编辑的 [DataBaseCanImp];
     *
     * 需要在excel表格中使用 'DbcList' 标注需要解析的协议sheet。
     * */
    fun read(): MutableMap<String, DataBaseCanImp> =
        GridReader(workbook).read(DataBaseCanImp::class).also { result ->
            result.values.forEach { it.propagateLongIdCode() }
        }
    /** 从单个指定的 DBC sheet 页面, 获取可编辑的 [DataBaseCanImp] ;
     *
     * 使用此方法可以将任意的协议页面解析成 [DataBaseCanImp]
     * */
    fun read(sheetName: String, dbcBaseInfo: DbcBaseInfo? = null): DataBaseCanImp {
        val sheet: Sheet = workbook.getSheet(sheetName) ?: error("没有找到名为 '${sheetName}' 的DBC协议")
        val rowIndex = Ref(0)
        return DataBaseCanImp().apply {
            dbcTag = sheetName
            dbcComment = sheetName
            dbcBaseInfo?.let { this.setDbcBaseInfo(it) }
            // 使用 smart-grid 组件, 从名为 sheetName 的 sheet 读取 CanMessage
            msgMap =  GridReader(workbook).readBySheet(sheet, CanMessageImp::class, GridSheetType.Dictionary, rowIndex, null)
        }.also { it.propagateLongIdCode() }
    }
    /** 将父报文的 longIdCode 传播给其下所有信号
     *
     * TODO : 这种写法不优雅，需要后续完善
     * */
    private fun DataBaseCanImp.propagateLongIdCode() {
        msgMap.values.forEach { msg ->
            msg.signalMap.values.forEach { signal ->
                signal.longIdCode = msg.longIdCode
            }
        }
    }
}