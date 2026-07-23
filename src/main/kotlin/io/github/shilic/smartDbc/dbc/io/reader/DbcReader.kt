package io.github.shilic.smartDbc.dbc.io.reader

import io.github.shilic.smartDbc.dbc.dataModel.contract.DataBaseCan
import io.github.shilic.smartDbc.dbc.dataModel.models.DataBaseCanImp
import io.github.shilic.smartDbc.dbc.dataModel.models.DbcBaseInfo
import org.apache.poi.ss.usermodel.Workbook
import java.io.File
import java.io.InputStream

/**
 * 将 DBC 文件的路径解析为[DataBaseCanImp]数据库。
 * 仅支持 `.dbc` 后缀的文件，内部委托给[File.toDbc]。
 * @return 解析后的[DataBaseCanImp]数据库实例
 * @since 1.0.11
 */
fun String.toDbc(): DataBaseCanImp = File(this).toDbc()

/**
 * 将 `.dbc` 格式文件解析为[DataBaseCanImp]数据库。
 * 非 `.dbc` 后缀的文件将抛出异常。
 * @return 解析后的[DataBaseCanImp]数据库实例
 */
fun File.toDbc(): DataBaseCanImp  = if (this.extension.lowercase() == "dbc") {
     DbcFileReader(this).read()
} else {
    error("只支持直接将dbc后缀的文件转换为 ${DataBaseCan::class.simpleName} : $extension")
}

/**
 * 通过输入流提供者延迟读取 `.dbc` 内容，解析为[DataBaseCanImp]数据库。
 * @return 解析后的[DataBaseCanImp]数据库实例
 */
fun (() -> InputStream).toDbc(): DataBaseCanImp = DbcFileReader(provider = this).read()

/** 从 Excel 文件的指定 sheet 中获取[DataBaseCanImp]数据库
 *
 * @param sheetName Excel 中目标工作表的名称
 * @param dbcBaseInfo DBC 基础信息，可选
 * @return 解析后的[DataBaseCanImp]数据库实例 */
fun String.getDbc(sheetName: String, dbcBaseInfo: DbcBaseInfo? = null): DataBaseCanImp = DbcGridReader(this).read(sheetName, dbcBaseInfo)

/**
 * 从 Excel 文件的指定 sheet 中解析 DBC 数据，转换为[DataBaseCanImp]数据库。
 * @param sheetName Excel 中目标工作表的名称
 * @param dbcBaseInfo DBC 基础信息，可选
 * @return 解析后的[DataBaseCanImp]数据库实例
 */
fun File.getDbc(sheetName: String, dbcBaseInfo: DbcBaseInfo? = null): DataBaseCanImp = DbcGridReader(this).read(sheetName, dbcBaseInfo)

/**
 * 从输入流的指定 sheet 中解析 DBC 数据，转换为[DataBaseCanImp]数据库。
 * @param sheetName Excel 中目标工作表的名称
 * @param dbcBaseInfo DBC 基础信息，可选
 * @return 解析后的[DataBaseCanImp]数据库实例
 */
fun InputStream.getDbc(sheetName: String, dbcBaseInfo: DbcBaseInfo? = null) : DataBaseCanImp = DbcGridReader(this).read(sheetName, dbcBaseInfo)

/**
 * 从输入流提供者的指定 sheet 中解析 DBC 数据，转换为[DataBaseCanImp]数据库。
 * @param sheetName Excel 中目标工作表的名称
 * @param dbcBaseInfo DBC 基础信息，可选
 * @return 解析后的[DataBaseCanImp]数据库实例
 */
fun (() -> InputStream).getDbc(sheetName: String, dbcBaseInfo: DbcBaseInfo? = null): DataBaseCanImp = DbcGridReader(this).read(sheetName, dbcBaseInfo)

/**
 * 从[Workbook]的指定 sheet 中解析 DBC 数据，转换为[DataBaseCanImp]数据库。
 * @param sheetName Excel 中目标工作表的名称
 * @param dbcBaseInfo DBC 基础信息，可选
 * @return 解析后的[DataBaseCanImp]数据库实例
 */
fun Workbook.getDbc(sheetName: String, dbcBaseInfo: DbcBaseInfo? = null) : DataBaseCanImp = DbcGridReader(this).read(sheetName, dbcBaseInfo)

/**
 * 将 DBC 文件路径解析为所有 sheet 对应的[DataBaseCanImp]数据库映射。
 * @return sheet 名称到[DataBaseCanImp]数据库实例的映射
 */
fun String.getDbcMap(): MutableMap<String, DataBaseCanImp> = DbcGridReader(this).read()

/**
 * 将 DBC 文件解析为所有 sheet 对应的[DataBaseCanImp]数据库映射。
 * @return sheet 名称到[DataBaseCanImp]数据库实例的映射
 */
fun File.getDbcMap(): MutableMap<String, DataBaseCanImp>  = DbcGridReader(this).read()

/**
 * 从输入流解析所有 sheet 对应的[DataBaseCanImp]数据库映射。
 * @return sheet 名称到[DataBaseCanImp]数据库实例的映射
 */
fun InputStream.getDbcMap(): MutableMap<String, DataBaseCanImp> = DbcGridReader(this).read()

/**
 * 从输入流提供者解析所有 sheet 对应的[DataBaseCanImp]数据库映射。
 * @return sheet 名称到[DataBaseCanImp]数据库实例的映射
 */
fun (() -> InputStream).getDbcMap(): MutableMap<String, DataBaseCanImp> = DbcGridReader(this).read()

/**
 * 从[Workbook]解析所有 sheet 对应的[DataBaseCanImp]数据库映射。
 * @return sheet 名称到[DataBaseCanImp]数据库实例的映射
 */
fun Workbook.getDbcMap(): MutableMap<String, DataBaseCanImp> = DbcGridReader(this).read()
