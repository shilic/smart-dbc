package dataModel.services

/**  DBC 文件元素 接口；
 *
 * 所有 DBC 文件元素 都需要实现该接口，用于表示该对象在DBC文件当中的值 */
interface IDbcElement {
    /** 该 dbc 文件元素 在 DBC 文件中的关键字 */
    val dbcKey : String
    /** 该 dbc 文件元素 在 DBC 文件中的编码 */
    val dbcValue : String
}